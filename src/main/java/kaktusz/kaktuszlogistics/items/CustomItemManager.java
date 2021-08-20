package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.properties.*;
import kaktusz.kaktuszlogistics.recipe.CraftingRecipe;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.recipe.ingredients.CustomItemIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.VanillaIngredient;
import kaktusz.kaktuszlogistics.recipe.outputs.ItemOutput;
import kaktusz.kaktuszlogistics.util.minecraft.SFXCollection;
import kaktusz.kaktuszlogistics.util.minecraft.SoundEffect;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

public class CustomItemManager {
    public static final Map<String, CustomItem> CUSTOM_ITEMS = new HashMap<>();

    @SuppressWarnings("SpellCheckingInspection")
    public static void initialise() {
        CustomItem.TYPE_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "CustomItemType");
        ItemEnchants.ENCHANTS_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "DefaultEnchants");
        ItemAttributes.DEFAULT_ATTRIBS_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "DefaultAttributes");
        ItemQuality.QUALITY_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "Quality");
        BlockDurability.DURA_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "Durability");

        //TODO split into different file and make possible to disable
        //register items
        //steel
        CustomItem ingotSteel = registerItem(new CustomItem("ingotSteel", "Steel Ingot", Material.NETHERITE_INGOT))
                .getOrAddProperty(TieredMetallic.class).item;
        CustomItem dustSteel = registerItem(new CustomItem("dustSteel", "Steel Dust", Material.GUNPOWDER))
                .getOrAddProperty(TieredMetallic.class).item;
        CustomItem blockSteel = registerItem(new CustomItem("blockSteel", "Steel Block", Material.NETHERITE_BLOCK))
                .getOrAddProperty(TieredMetallic.class).item
                .getOrAddProperty(BlockDurability.class).setMaxDurability(6).item;
        RecipeManager.addBlockRecipe(ingotSteel, blockSteel);
        RecipeManager.addQualitySmeltingRecipe(new CustomItemIngredient(dustSteel), new ItemOutput(ingotSteel.createStack(1)), "dustSteel_1xingotSteel", 1f, 200);

        //steel sword
        CustomItem swordSteel = registerItem(new CustomItem("swordSteel", "Steel Sword", Material.NETHERITE_SWORD))
                .getOrAddProperty(ItemAttributes.class)
                .setDamage(13)
                .setAttackSpeed(1.2)
                .addAttribute(Attribute.GENERIC_ARMOR, 4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
                .addAttribute(Attribute.GENERIC_MOVEMENT_SPEED, -0.30, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HAND)
                .item;
        ItemIngredient steelIngotIngredient = new CustomItemIngredient(ingotSteel);
        RecipeManager.addCraftingRecipe(new CraftingRecipe(
                new ItemIngredient[][] {
                        {steelIngotIngredient},
                        {steelIngotIngredient},
                        {new VanillaIngredient(Material.STICK)}
                }, new ItemOutput(swordSteel.createStack(1))
        ), "swordSteel");

        //fishnets
        CustomItem fishnets = registerItem(new CustomItem("armourFishnets", "Fishnets", Material.CHAINMAIL_LEGGINGS))
                .getOrAddProperty(ItemAttributes.class)
                .setDefaultEquipmentSlot(EquipmentSlot.LEGS)
                .setDefence(-3)
                .addAttribute(Attribute.GENERIC_MAX_HEALTH, -2, AttributeModifier.Operation.ADD_NUMBER)
                .addAttribute(Attribute.GENERIC_LUCK, 0.6, AttributeModifier.Operation.ADD_SCALAR)
                .addAttribute(Attribute.GENERIC_MOVEMENT_SPEED, 0.7, AttributeModifier.Operation.ADD_SCALAR)
                .addAttribute(Attribute.GENERIC_ATTACK_DAMAGE, -0.35, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
                .item;
        ItemIngredient stringIngredient = new VanillaIngredient(Material.STRING);
        RecipeManager.addCraftingRecipe(new CraftingRecipe(
                new ItemIngredient[][] {
                        {stringIngredient, stringIngredient, stringIngredient},
                        {stringIngredient, null, stringIngredient},
                        {stringIngredient, null, stringIngredient}
                }, new ItemOutput(fishnets.createStack(1))
        ), "fishnets");

        //silver
        CustomItem ingotSilver = registerItem(new CustomItem("ingotSilver", "Silver Ingot", Material.IRON_INGOT))
                .getOrAddProperty(TieredMetallic.class).item
                .addEnchantment(Enchantment.DAMAGE_UNDEAD, 1);
        CustomItem blockSilver = registerItem(new CustomItem("blockSilver", "Block of Silver", Material.IRON_BLOCK))
                .getOrAddProperty(TieredMetallic.class).item
                .getOrAddProperty(ItemPlaceable.class).item
                .addEnchantment(Enchantment.DAMAGE_UNDEAD, 3);
        RecipeManager.addBlockRecipe(ingotSilver, blockSilver);

        //silver sword
        CustomItem swordSilver = registerItem(new CustomItem("swordSilver", "Silver Sword", Material.IRON_SWORD))
                .addEnchantment(Enchantment.DAMAGE_UNDEAD, 2)
                .getOrAddProperty(ItemAttributes.class)
                .setDamage(6)
                .setAttackSpeed(2)
                .addAttribute(Attribute.GENERIC_MOVEMENT_SPEED, 0.1, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HAND)
                .item;
        ItemIngredient silverIngotIngredient = new CustomItemIngredient(ingotSilver);
        RecipeManager.addCraftingRecipe(new CraftingRecipe(
                new ItemIngredient[][] {
                        {silverIngotIngredient},
                        {silverIngotIngredient},
                        {new VanillaIngredient(Material.STICK)}
                }, new ItemOutput(swordSilver.createStack(1))
        ), "swordSilver");

        //fluid input block
        CustomItem fluidInput = registerItem(new FluidInputBarrel("fluidInput", "Fluid Input"));
        ItemIngredient bucket = new VanillaIngredient(Material.BUCKET);
        ItemIngredient barrel = new VanillaIngredient(Material.BARREL);
        RecipeManager.addCraftingRecipe(new CraftingRecipe(new ItemIngredient[][] {{bucket},{barrel}}, new ItemOutput(fluidInput.createStack(1))), "fluidInput");

        //inspection tool
        registerItem(new InspectionTool("inspectionTool", "Inspection Tool", Material.OAK_SIGN))
                .addEnchantment(Enchantment.LOYALTY, 1)
                .addEnchantment(Enchantment.KNOCKBACK, 10)
                .setLore(ChatColor.GRAY + "A debugging tool. " + ChatColor.BOLD + "Right click" + ChatColor.GRAY + " to use.");

        //pralka
        registerItem(new CustomItem("blockPralka", ChatColor.GOLD + "Pralka Wibrująco-Bisująca", Material.FURNACE))
                .getOrAddProperty(BlockDurability.class).setMaxDurability(4).setDamageSound(new SFXCollection(
                                new SoundEffect(Sound.ENTITY_IRON_GOLEM_HURT, 0.6f, 0.6f, 0.8f, 1.8f))).item
                .addEnchantment(Enchantment.DIG_SPEED, 4)
                .addEnchantment(Enchantment.ARROW_DAMAGE, 3)
                .setLore(ChatColor.GRAY + "" + ChatColor.ITALIC + "Nowoczesna pralka marki " + ChatColor.DARK_RED + ChatColor.BOLD + ChatColor.ITALIC + "Mejstar");

        //misc
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
