package kaktusz.kaktuszlogistics.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getClickedInventory() == null)
			return;

		InventoryHolder holder = e.getClickedInventory().getHolder();
		if(holder instanceof CustomGUI) {
			e.setCancelled(true);
			((CustomGUI)holder).onClick(e.getClick(), e.getSlot(), e.getWhoClicked());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent e) {
		if(e.getInventory().getHolder() instanceof CustomGUI)
			e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClosed(InventoryCloseEvent e) {
		if(e.getInventory().getHolder() instanceof CustomGUI) {
			((CustomGUI)e.getInventory().getHolder()).onClosed(e.getPlayer());
		}
	}

}
