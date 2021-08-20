package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.nbt.EnchantsContainer;
import kaktusz.kaktuszlogistics.items.nbt.EnchantsTupleCollection;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Applies enchantments to all items with this property
 */
public class ItemEnchants extends ItemProperty {
    public static NamespacedKey ENCHANTS_KEY;

    private final Map<Enchantment, Integer> enchants = new HashMap<>();
    private boolean hideEnchants = false;

    //SETUP
    public ItemEnchants(CustomItem item) {
        super(item);
    }

    /**
     * Adds the given enchantment to the default enchantments for items with this property
     */
    public void addEnchantment(Enchantment enchantment, int level) {
        enchants.put(enchantment, level);
    }

    /**
     * Sets whether items with this property will display their enchantments in their lore or hide them.
     * By default, enchantments are not hidden.
     */
    public void setHideEnchants(boolean hide) {
        this.hideEnchants = hide;
    }

    //ITEM
    @SuppressWarnings("ConstantConditions") //ItemMeta should never be null in the calling context
    @Override
    public void onUpdateStack(ItemStack stack) {
        //clear saved default enchantments
        EnchantsTupleCollection defaultEnchants = readEnchantMarker(stack);
        if(defaultEnchants != null) {
            for(Enchantment ench : defaultEnchants.enchants.keySet()) {
                if(stack.containsEnchantment(ench))
                    stack.removeEnchantment(ench);
            }
        }
        //clear enchantments we are about to add (to avoid duplicates)
        for(Enchantment ench : enchants.keySet()) {
            if(stack.containsEnchantment(ench))
                stack.removeEnchantment(ench);
        }
        stack.addUnsafeEnchantments(enchants);
        ItemMeta meta = stack.getItemMeta();
        if(hideEnchants)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        else
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        addEnchantMarker(stack);
    }

    /**
     * Adds an NBT tag listing all the enchantments added by default to this item, so that player-added enchants don't get cleared when updating
     */
    private void addEnchantMarker(ItemStack stack) {
        CustomItem.setNBT(stack, ENCHANTS_KEY, EnchantsContainer.ENCHANTMENTS, new EnchantsTupleCollection(enchants));
    }
    private EnchantsTupleCollection readEnchantMarker(ItemStack stack) {
        return CustomItem.readNBT(stack, ENCHANTS_KEY, EnchantsContainer.ENCHANTMENTS);
    }
}
