package kaktusz.kaktuszlogistics.items;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class ItemEventsListener implements Listener {

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent e) {
        if(e.isCancelled())
            return;

        ItemStack item = e.getItemInHand();
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem == null)
            return;

        customItem.onTryPlace(e, item);
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        if(e.isCancelled())
            return;

        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem == null)
            return;

        customItem.onHeld(e, item);
    }
}
