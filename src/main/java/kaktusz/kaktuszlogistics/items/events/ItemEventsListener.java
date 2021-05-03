package kaktusz.kaktuszlogistics.items.events;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.properties.ItemProperty;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("ConstantConditions")
public class ItemEventsListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onItemUsed(PlayerInteractEvent e) {
        if(e.useItemInHand() == Event.Result.DENY)
            return;

        ItemStack item = e.getItem();
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem == null)
            return;
        //item
        if(customItem instanceof IUseListener)
            ((IUseListener)customItem).onTryUse(e, item);
        //properties
        for(ItemProperty p : customItem.getAllProperties()) {
            if(p instanceof IUseListener) {
                ((IUseListener)p).onTryUse(e, item);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemUsedOnEntity(PlayerInteractEntityEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItem(e.getHand());
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem == null)
            return;
        //item
        if(customItem instanceof IUseListener)
            ((IUseListener)customItem).onTryUseEntity(e, item);
        //properties
        for(ItemProperty p : customItem.getAllProperties()) {
            if(p instanceof IUseListener) {
                ((IUseListener)p).onTryUseEntity(e, item);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem == null)
            return;
        //item
        if(customItem instanceof IPlacedListener)
            ((IPlacedListener)customItem).onTryPlace(e, item);
        //properties
        for(ItemProperty p : customItem.getAllProperties()) {
            if(p instanceof IPlacedListener) {
                ((IPlacedListener)p).onTryPlace(e, item);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem == null)
            return;
        //item
        if(customItem instanceof IHeldListener)
            ((IHeldListener)customItem).onHeld(e, item);
        //properties
        for(ItemProperty p : customItem.getAllProperties()) {
            if(p instanceof IHeldListener) {
                ((IHeldListener)p).onHeld(e, item);
            }
        }
    }

    //CRAFTING
    @EventHandler
    public void onTryCraft(PrepareItemCraftEvent e) {
        for (ItemStack itemStack : e.getInventory()) {
            if(CustomItem.getFromStack(itemStack) != null) {
                e.getInventory().setResult(new ItemStack(Material.AIR)); //make uncraftable
            }
        }
    }
}

