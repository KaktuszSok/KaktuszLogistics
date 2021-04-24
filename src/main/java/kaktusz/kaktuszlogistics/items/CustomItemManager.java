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

        //register items
        registerItem(new CustomItem("ingotSteel", "Steel Ingot", Material.NETHERITE_INGOT));
        registerItem(new CustomItem("dustSteel", "Steel Dust", Material.GUNPOWDER));
        registerItem(new CustomItem("ingotSilver", "Silver Ingot", Material.IRON_INGOT))
                .addEnchantment(Enchantment.DAMAGE_UNDEAD, 1);
        registerItem(new ItemBlock("blockSilver", "Block of Silver", Material.IRON_BLOCK, false))
                .addEnchantment(Enchantment.DAMAGE_UNDEAD, 3);;
        registerItem(new InspectionTool("inspectionTool", "Inspection Tool", Material.OAK_SIGN))
                .addEnchantment(Enchantment.LOYALTY, 1)
                .addEnchantment(Enchantment.KNOCKBACK, 10)
                .setLore(ChatColor.GRAY + "A debugging tool. " + ChatColor.BOLD + "Right click" + ChatColor.GRAY + " to use.");
    }

    public static CustomItem registerItem(CustomItem item) {
        CUSTOM_ITEMS.put(item.type, item);

        return item;
    }

    /**
     * Returns the matching CustomItem or null if it does not exist
     */
    public static CustomItem tryGetItem(String type) {
        return CUSTOM_ITEMS.getOrDefault(type, null);
    }
}
