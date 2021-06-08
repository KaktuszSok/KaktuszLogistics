package kaktusz.kaktuszlogistics.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class CustomGUI {

	protected static final int INVENTORY_WIDTH = 9;
	protected final Inventory inventory;

	private boolean needsSetup = true;

	//SETUP
	public CustomGUI(int size, String title) {
		this.inventory = Bukkit.createInventory(null, size, title);
	}

	protected void setSlot(int row, int column, ItemStack stack) {
		int slot = row * INVENTORY_WIDTH + column;
		if(slot < inventory.getSize())
			inventory.setItem(slot, stack);
	}

	protected ItemStack getItemInSlot(int row, int column) {
		int slot = row * INVENTORY_WIDTH + column;
		if(slot < inventory.getSize())
			return inventory.getItem(slot);

		return null;
	}

	/**
	 * Resets the inventory to its cleared state.
	 * May not be completely empty, depending on implementation.
	 */
	protected void clearInventory() {
		inventory.clear();
	}

	//INTERACTION
	/**
	 * Opens the inventory for a specified viewer
	 */
	public void open(HumanEntity viewer) {
		if(needsSetup) {
			needsSetup = false;
			clearInventory();
		}
		viewer.openInventory(inventory);
		GUIListener.registerOpenGUI(inventory, this);
	}

	public void close(HumanEntity viewer) {
		viewer.closeInventory();
	}

	/**
	 * Kicks out all viewers from viewing this inventory
	 */
	public void forceClose() {
		inventory.getViewers().forEach(this::close);
	}

	public boolean hasViewers() {
		return !inventory.getViewers().isEmpty();
	}

	//EVENTS
	public abstract void onClick(ClickType type, int slot);

	/**
	 * Called when the player quits out of this inventory the vanilla way.
	 */
	public void onClosed() {
		if(inventory.getViewers().size() <= 1) //last viewer closing inventory
			GUIListener.deregisterGUI(inventory);
	}
}
