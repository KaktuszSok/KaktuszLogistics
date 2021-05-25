package kaktusz.kaktuszlogistics.util;

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

public class VanillaUtils {

    public static final int CHUNK_SIZE = 16;

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
        //TODO: config for any kill messages
        //TODO: config for non-player kill messages
        String killedName = entity.getCustomName();
        if(killedName == null) {
            if(entity instanceof Player)
                killedName = entity.getName();
            else
                return; //we don't want to print the name of unnamed non-player entities
        }

        String sourceName = source.getCustomName();
        if(sourceName == null)
            sourceName = source.getName();

        killMessage = killMessage.replace("%k", killedName).replace("%s", sourceName);

        Bukkit.broadcastMessage(killMessage);
    }

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

}
