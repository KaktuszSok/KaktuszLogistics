package kaktusz.kaktuszlogistics.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class GUIListener implements Listener {

	private static final Map<Inventory, CustomGUI> openGUIs = new HashMap<>();

	/**
	 * Adds the inventory-gui pair to the tracked open GUIs, allowing them to receive inputs
	 */
	public static void registerOpenGUI(Inventory inv, CustomGUI gui) {
		openGUIs.put(inv, gui);
	}

	/**
	 * Removes the inventory (and the GUI associated with it) from the tracked open GUIs.
	 */
	public static void deregisterGUI(Inventory inv) {
		openGUIs.remove(inv);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		if(openGUIs.containsKey(e.getClickedInventory())) {
			e.setCancelled(true);
			openGUIs.get(e.getClickedInventory()).onClick(e.getClick(), e.getSlot(), e.getWhoClicked());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent e) {
		if(openGUIs.containsKey(e.getInventory())) { //disallow dragging
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClosed(InventoryCloseEvent e) {
		if(openGUIs.containsKey(e.getInventory())) {
			openGUIs.get(e.getInventory()).onClosed(e.getPlayer());
		}
	}

}
