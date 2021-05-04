package kaktusz.kaktuszlogistics.items.events;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemProperty;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.FurnaceInventory;
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
        if(e.getRightClicked() instanceof ItemFrame) //allow placing into itemframes
            return;

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
    @EventHandler(ignoreCancelled = true)
    public void onTryCraft(PrepareItemCraftEvent e) {
        for (ItemStack itemStack : e.getInventory()) {
            if(CustomItem.getFromStack(itemStack) != null) {
                e.getInventory().setResult(new ItemStack(Material.AIR)); //make uncraftable
            }
        }
    }

    //OTHER USAGES
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getClickedInventory() instanceof FurnaceInventory) {
            if(CustomItem.getFromStack(e.getCursor()) != null) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        if(e.getInventory() instanceof FurnaceInventory) {
            if(CustomItem.getFromStack(e.getOldCursor()) != null) {
                for(int s : e.getInventorySlots()) {
                    if(s == 0) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent e) {
        if(e.getDestination() instanceof FurnaceInventory) {
            if(CustomItem.getFromStack(e.getItem()) != null) {
                e.setCancelled(true);
            }
        }
    }
}

