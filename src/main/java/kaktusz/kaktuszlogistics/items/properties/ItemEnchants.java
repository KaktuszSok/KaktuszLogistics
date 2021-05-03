package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.nbt.EnchantsContainer;
import kaktusz.kaktuszlogistics.items.nbt.EnchantsTupleCollection;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemEnchants extends ItemProperty {
    public static NamespacedKey ENCHANTS_KEY;

    private final Map<Enchantment, Integer> enchants = new HashMap<>();

    //SETUP
    public ItemEnchants(CustomItem item) {
        super(item);
    }

    public void addEnchantment(Enchantment enchantment, int level) {
        enchants.put(enchantment, level);
    }

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
