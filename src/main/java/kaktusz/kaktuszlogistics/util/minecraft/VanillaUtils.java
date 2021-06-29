package kaktusz.kaktuszlogistics.util.minecraft;

import kaktusz.kaktuszlogistics.util.CastingUtils;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigManager;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class VanillaUtils {

    private static final int MS_PER_TICK = 50;
    private static long tickTime = 0;
    public static void initialiseTickTime() {
        tickTime = System.currentTimeMillis() * MS_PER_TICK;
    }
    public static void incrementTickTime() {
        tickTime++;
    }
    public static long getTickTime() {
        return tickTime;
    }

    //ITEMSTACKS
    public static void damageTool(ItemStack item, Player player) {
        if(player.getGameMode() == GameMode.CREATIVE) //creative?
            return;

        if(item.getItemMeta() instanceof Damageable) {
            if(item.getItemMeta().isUnbreakable()) //unbreakable?
                return;

            Damageable tool = (Damageable)item.getItemMeta();
            int damage = 1;

            int unbreaking = item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY); //unbreaking?
            float chanceToDamage = 1f / (unbreaking+1);
            if(RandomUtils.nextFloat() > chanceToDamage) //unbreaking made item not take damage
                return;

            tool.setDamage(tool.getDamage()+damage); //damage the tool
            item.setItemMeta((ItemMeta)tool);

            if(tool.getDamage() > item.getType().getMaxDurability()) {
                Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.35f)).add(0, -0.25, 0);
                player.getWorld().playSound(loc, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                player.getWorld().spawnParticle(Particle.ITEM_CRACK, loc, 5, 0.1,0.15,0.1, 0.035, item.clone());
                item.setAmount(0);
            }
        }
    }

    public static boolean canCombineStacks(ItemStack a, ItemStack b) {
        return a == null || b == null || a.isSimilar(b) && a.getMaxStackSize() >= a.getAmount() + b.getAmount();
    }

    /**
     * Tries to add items to an inventory, and drops those that didn't fit onto the ground
     */
    public static void addItemsOrDrop(Inventory inventory, ItemStack... items) {
        //add items to inventory
        HashMap<Integer, ItemStack> failedStacks = inventory.addItem(items);
        //drop items that didn't fit
        Location loc = inventory.getLocation();
        if(loc == null || loc.getWorld() == null)
            return; //inventory does not have world location - can't drop.
        for (ItemStack failedStack : failedStacks.values()) {
            loc.getWorld().dropItemNaturally(loc, failedStack);
        }
    }

    public static <T> byte[] serialiseToBytes(T serialisable) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(byteStream)) {
            bukkitStream.writeObject(serialisable); //write serialisable
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteStream.toByteArray();
    }
    public static <T> byte[] serialisablesToBytes(List<T> serialisables) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(byteStream)) {
            bukkitStream.writeInt(serialisables.size()); //write int
            for(T serialisable : serialisables) {
                bukkitStream.writeObject(serialisable); //write serialisable (size times)
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteStream.toByteArray();
    }

    public static <T> T deserialiseFromBytes(byte[] bytes) {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        try(BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(byteStream)) {
            return CastingUtils.confidentCast(bukkitStream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static <T> List<T> serialisablesFromBytes(byte[] bytes) {
        List<T> result = new ArrayList<>();

        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        try(BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(byteStream)) {
            int size = bukkitStream.readInt(); //read int
            for(int i = 0; i < size; i++) {
                result.add(CastingUtils.confidentCast(bukkitStream.readObject())); //read serialisable (size times)
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    //ENTITIES
    /**
     * Damages an entity and prints a custom message if they are killed
     * @param killMessage String that is formatted into the kill message. %k = killed entity, %s = damage source
     */
    public static void damageEntity(LivingEntity entity, Entity source, int damage, String killMessage) {
        entity.damage(damage);
        if(entity.getHealth() > 0)
            return;

        killEntity(entity, source, killMessage);
    }

    /**
     * Kills an entity and potentially prints the message in chat
     * @param entity The killed entity
     * @param source The killer
     * @param killMessage String that is formatted into the kill message. %k = killed entity, %s = damage source
     */
    private static void killEntity(LivingEntity entity, Entity source, String killMessage) {
        if(entity instanceof Player && !ConfigManager.BROADCAST_PLAYER_KILLS.value)
            return;
        else if(!ConfigManager.BROADCAST_NAMED_MOB_KILLS.value)
            return;

        String killedName = entity.getCustomName();
        if(killedName == null) {
            if(entity instanceof Player)
                killedName = entity.getName();
            else
                return; //we don't want to print the name of unnamed non-player entities (or player entities if the config is disabled)
        }

        String sourceName = source.getCustomName();
        if(sourceName == null)
            sourceName = source.getName();

        killMessage = killMessage.replace("%k", killedName).replace("%s", sourceName);

        Bukkit.broadcastMessage(killMessage);
    }

    //SOUNDS
    public enum BlockSounds {
        HIT,
        BREAK,
        STEP,
        FALL,
        PLACE
    }
    public static Sound getBlockSound(Block block, BlockSounds sound) {
        switch (sound) {
            case BREAK:
                return block.getBlockData().getSoundGroup().getBreakSound();
            case HIT:
                return block.getBlockData().getSoundGroup().getHitSound();
            case STEP:
                return block.getBlockData().getSoundGroup().getStepSound();
            case FALL:
                return block.getBlockData().getSoundGroup().getFallSound();
            case PLACE:
                return block.getBlockData().getSoundGroup().getPlaceSound();
        }
        return null;
    }
    public static float getBlockSFXVolume(Block block) {
        return block.getBlockData().getSoundGroup().getVolume();
    }
    public static float getBlockSFXPitch(Block block) {
        return block.getBlockData().getSoundGroup().getPitch();
    }
    /**
     * Plays the vanilla sound of a block breaking
     * @param ignorePlayer Player that should not hear this sound (i.e. player who mined the block, if applicable). If null, all nearby players will hear it.
     */
    public static void playVanillaBreakSound(Block block, Player ignorePlayer) {
        SoundGroup soundGroup = block.getBlockData().getSoundGroup();
        if(ignorePlayer == null) {
            block.getWorld().playSound(block.getLocation().clone().add(0.5d, 0.5d, 0.5d), soundGroup.getBreakSound(), soundGroup.getVolume(), soundGroup.getPitch());
            return;
        }

        //otherwise, play for every player other than the ignored one
        for (Player p : block.getWorld().getPlayers()) {
            if(p == ignorePlayer)
                continue;

            p.playSound(block.getLocation().clone().add(0.5d, 0.5d, 0.5d), soundGroup.getBreakSound(), 1, 0.8f);
        }
    }

    //POSITIONS
    public static final class BlockPosition implements Serializable {
        private static final long serialVersionUID = 100L;

        public final int x;
        public final short y;
        public final int z;

        public BlockPosition(Location location) {
            this(location.getBlockX(), (short)location.getBlockY(), location.getBlockZ());
        }
        public BlockPosition(int x, int y, int z) {
            this(x,(short)y,z);
        }
        public BlockPosition(int x, short y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public BlockPosition above() {
            return new BlockPosition(x,(short)(y+1),z);
        }
        public BlockPosition below() {
            return new BlockPosition(x,(short)(y-1),z);
        }
        public BlockPosition east() {
            return new BlockPosition(x+1,y,z);
        }
        public BlockPosition west() {
            return new BlockPosition(x-1,y,z);
        }
        public BlockPosition south() {
            return new BlockPosition(x,y,z+1);
        }
        public BlockPosition north() {
            return new BlockPosition(x,y,z-1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockPosition that = (BlockPosition) o;
            return x == that.x && y == that.y && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + "," + z + ")";
        }
    }

    public static class MutableBlockPosition {
        public int x;
        public short y;
        public int z;

        public MutableBlockPosition(BlockPosition base) {
            this(base.x, base.y, base.z);
        }
        public MutableBlockPosition(int x, short y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MutableBlockPosition that = (MutableBlockPosition) o;
            return x == that.x && y == that.y && z == that.z;
        }
    }

    public static class BlockAABB implements Serializable {
        private static final long serialVersionUID = 100L;

        public final BlockPosition minCorner;
        public final BlockPosition maxCorner;

        public BlockAABB(BlockPosition minCorner, BlockPosition maxCorner) {
            this.minCorner = minCorner;
            this.maxCorner = maxCorner;
        }

        public static BlockAABB fromAnyCorners(BlockPosition cornerA, BlockPosition cornerB) {
            int minX, maxX, minZ, maxZ;
            short minY, maxY;
            //could use min() and max(), but if statements yield half as many comparisons
            if(cornerA.x < cornerB.x) {
                minX = cornerA.x;
                maxX = cornerB.x;
            }
            else {
                minX = cornerB.x;
                maxX = cornerA.x;
            }
            if(cornerA.y < cornerB.y) {
                minY = cornerA.y;
                maxY = cornerB.y;
            }
            else {
                minY = cornerB.y;
                maxY = cornerA.y;
            }
            if(cornerA.z < cornerB.z) {
                minZ = cornerA.z;
                maxZ = cornerB.z;
            }
            else {
                minZ = cornerB.z;
                maxZ = cornerA.z;
            }
            return new BlockAABB(new BlockPosition(minX, minY, minZ), new BlockPosition(maxX, maxY, maxZ));
        }

        public boolean containsPosition(BlockPosition pos) {
            return pos.x >= minCorner.x && pos.x <= maxCorner.x
                    && pos.y >= minCorner.y && pos.y <= maxCorner.y
                    && pos.z >= minCorner.z && pos.z <= maxCorner.z;
        }
    }

    /**
     * @return The coordinate of the chunk this block coordinate is in
     */
    public static int blockToChunkCoord(int blockCoord) {
        return blockCoord >> 4;
    }

    /**
     * @return The coordinate of this chunk's first block along whichever axis
     */
    public static int chunkToBlockCoord(int chunkCoord) {
        return chunkCoord << 4;
    }

    /**
     * Transforms block coordinate to chunk-local coordinate. DO NOT USE FOR Y!
     * @return The coordinate of the block within its chunk
     */
    public static int blockToLocalCoord(int blockCoord) {
        return blockCoord & 0b1111;
    }
}
