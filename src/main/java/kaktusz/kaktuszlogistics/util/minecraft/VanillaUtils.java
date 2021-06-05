package kaktusz.kaktuszlogistics.util.minecraft;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigManager;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.Serializable;
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

    //POSITIONS
    public static class BlockPosition implements Serializable {
        private static final long serialVersionUID = 100L;

        public final int x;
        public final short y;
        public final int z;

        public BlockPosition(Location location) {
            this(location.getBlockX(), (short)location.getBlockY(), location.getBlockZ());
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

    /**
     * @return The coordinate of the chunk this block coordinate is in
     */
    public static int blockToChunkCoord(int blockCoord) {
        return blockCoord >> 4;
    }

    /**
     * Transforms block coordinate to chunk-local coordinate. DO NOT USE FOR Y!
     * @return The coordinate of the block within its chunk
     */
    public static int blockToLocalCoord(int blockCoord) {
        return blockCoord & 0b1111;
    }
}
