package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.properties.*;
import kaktusz.kaktuszlogistics.recipe.CraftingRecipe;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.recipe.ingredients.CustomItemIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.VanillaIngredient;
import kaktusz.kaktuszlogistics.recipe.outputs.ItemOutput;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public class CustomItemManager {
    public static Map<String, CustomItem> CUSTOM_ITEMS = new HashMap<>();

    public static void initialise() {
        CustomItem.TYPE_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "CustomItemType");
        ItemEnchants.ENCHANTS_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "DefaultEnchants");
        ItemQuality.QUALITY_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "Quality");
        BlockDurability.DURA_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "Durability");
        GunItem.LAST_SHOOT_TIME_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "LastShootTime");

        //register items
        registerItem(new GunItem("toolTestGun", "Gun", Material.PRISMARINE_SHARD));

        CustomItem ingotSteel = registerItem(new CustomItem("ingotSteel", "Steel Ingot", Material.NETHERITE_INGOT))
                .getOrAddProperty(TieredMetallic.class).item;
        CustomItem dustSteel = registerItem(new CustomItem("dustSteel", "Steel Dust", Material.GUNPOWDER))
                .getOrAddProperty(TieredMetallic.class).item;
        CustomItem blockSteel = registerItem(new CustomItem("blockSteel", "Steel Block", Material.NETHERITE_BLOCK))
                .getOrAddProperty(TieredMetallic.class).item
                .getOrAddProperty(BlockDurability.class).setMaxDurability(6).item;
        RecipeManager.addBlockRecipe(ingotSteel, blockSteel);
        RecipeManager.addQualitySmeltingRecipe(new CustomItemIngredient(dustSteel), new ItemOutput(ingotSteel.createStack(1)), "dustSteel_1xingotSteel", 1f, 200);

        CustomItem ingotSilver = registerItem(new CustomItem("ingotSilver", "Silver Ingot", Material.IRON_INGOT))
                .getOrAddProperty(TieredMetallic.class).item
                .addEnchantment(Enchantment.DAMAGE_UNDEAD, 1);
        CustomItem blockSilver = registerItem(new CustomItem("blockSilver", "Block of Silver", Material.IRON_BLOCK))
                .getOrAddProperty(TieredMetallic.class).item
                .getOrAddProperty(ItemPlaceable.class).item
                .addEnchantment(Enchantment.DAMAGE_UNDEAD, 3);
        RecipeManager.addBlockRecipe(ingotSilver, blockSilver);

        CustomItem fluidInput = registerItem(new FluidInputBarrel("fluidInput", "Fluid Input"));
        ItemIngredient bucket = new VanillaIngredient(Material.BUCKET);
        ItemIngredient barrel = new VanillaIngredient(Material.BARREL);
        RecipeManager.addCraftingRecipe(new CraftingRecipe(new ItemIngredient[][] {{bucket},{barrel}}, new ItemOutput(fluidInput.createStack(1))));

        registerItem(new InspectionTool("inspectionTool", "Inspection Tool", Material.OAK_SIGN))
                .addEnchantment(Enchantment.LOYALTY, 1)
                .addEnchantment(Enchantment.KNOCKBACK, 10)
                .setLore(ChatColor.GRAY + "A debugging tool. " + ChatColor.BOLD + "Right click" + ChatColor.GRAY + " to use.");

        registerItem(new CustomItem("blockPralka", ChatColor.GOLD + "Pralka Wibrująco-Bisująca", Material.FURNACE))
                .getOrAddProperty(BlockDurability.class).setMaxDurability(4).setDamageSound(Sound.ENTITY_IRON_GOLEM_HURT, 0.6f, 0.8f, 1.8f).item
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
