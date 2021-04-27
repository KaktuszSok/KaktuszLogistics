package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public class CustomItemManager {
    public static Map<String, CustomItem> CUSTOM_ITEMS = new HashMap<>();

    public static void initialise() {
        CustomItem.TYPE_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "CustomItemType");
        CustomItem.ENCHANTS_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "DefaultEnchants");
        TieredItem.QUALITY_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "Quality");

        //register items
        registerItem(new MetalItem("ingotSteel", "Steel Ingot", Material.NETHERITE_INGOT));
        registerItem(new MetalItem("dustSteel", "Steel Dust", Material.GUNPOWDER));
        registerItem(new MetalItem("ingotSilver", "Silver Ingot", Material.IRON_INGOT))
                .addEnchantment(Enchantment.DAMAGE_UNDEAD, 1);
        registerItem(new MetalItem("blockSilver", "Block of Silver", Material.IRON_BLOCK))
                .addEnchantment(Enchantment.DAMAGE_UNDEAD, 3);
        registerItem(new InspectionTool("inspectionTool", "Inspection Tool", Material.OAK_SIGN))
                .addEnchantment(Enchantment.LOYALTY, 1)
                .addEnchantment(Enchantment.KNOCKBACK, 10)
                .setLore(ChatColor.GRAY + "A debugging tool. " + ChatColor.BOLD + "Right click" + ChatColor.GRAY + " to use.");
        registerItem(new BlockItem("blockPralka", ChatColor.GOLD + "Pralka Wibrująco-Bisująca", Material.FURNACE))
                .addEnchantment(Enchantment.DIG_SPEED, 4)
                .addEnchantment(Enchantment.ARROW_DAMAGE, 3)
                .setLore(ChatColor.GRAY + "" + ChatColor.ITALIC + "Nowoczesna pralka marki " + ChatColor.DARK_RED + ChatColor.BOLD + ChatColor.ITALIC + "Mejstar");
        registerItem(new CustomItem("scrap", "Scrap", Material.BONE_MEAL));
        registerItem(new CustomItem("rubber", "Rubber", Material.BLACK_DYE));

    }

    public static CustomItem registerItem(CustomItem item) {
        CUSTOM_ITEMS.put(item.type, item);

        return item;
    }

    /**
     * Returns the matching CustomItem or null if it does not exist
     */
    public static CustomItem tryGetItem(String type) {
        if(type == null)
            return null;
        return CUSTOM_ITEMS.getOrDefault(type, null);
    }
}
