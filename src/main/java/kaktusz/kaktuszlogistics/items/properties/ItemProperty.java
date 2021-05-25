package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class ItemProperty {

    public final CustomItem item;

    public ItemProperty(CustomItem item) {
        this.item = item;
    }

    /**
     * Called right before the property is added to the CustomItem's property list
     */
    public void onAdded() {

    }

    /**
     * Called right after the stack has been created and its type tag has been added but right before it has been updated
     */
    public void onCreateStack(ItemStack stack) {

    }

    public void onUpdateStack(ItemStack stack) {

    }

    public void modifyLore(List<String> lore, ItemStack item) {

    }

    public String modifyDisplayName(String currName, ItemStack item) {
        return currName;
    }

}
