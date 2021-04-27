package kaktusz.kaktuszlogistics.items;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("ConstantConditions")
public class ItemEventsListener implements Listener {

    @EventHandler
    public void onItemUsed(PlayerInteractEvent e) {
        if(e.useItemInHand() == Event.Result.DENY)
            return;

        ItemStack item = e.getItem();
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem instanceof IUseListener)
            ((IUseListener)customItem).onTryUse(e, item);
    }

    @EventHandler
    public void onItemUsedOnEntity(PlayerInteractEntityEvent e) {
        if(e.isCancelled())
            return;

        ItemStack item = e.getPlayer().getInventory().getItem(e.getHand());
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem instanceof IUseListener)
            ((IUseListener)customItem).onTryUseEntity(e, item);
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent e) {
        if(e.isCancelled())
            return;

        ItemStack item = e.getItemInHand();
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem instanceof IPlacedListener)
            ((IPlacedListener)customItem).onTryPlace(e, item);
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        if(e.isCancelled())
            return;

        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem instanceof IHeldListener)
            ((IHeldListener)customItem).onHeld(e, item);
    }
}

interface IUseListener {
    void onTryUse (PlayerInteractEvent e, ItemStack stack);
    void onTryUseEntity(PlayerInteractEntityEvent e, ItemStack stack);
}

interface IPlacedListener {
    void onTryPlace(BlockPlaceEvent e, ItemStack stack);
}

interface IHeldListener {
    void onHeld(PlayerItemHeldEvent e, ItemStack stack);
}
