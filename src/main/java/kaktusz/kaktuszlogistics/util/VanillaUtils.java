package kaktusz.kaktuszlogistics.util;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class VanillaUtils {

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

}
