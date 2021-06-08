package kaktusz.kaktuszlogistics.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	public abstract void onClick(ClickType type, int slot, HumanEntity player);

	/**
	 * Called when the player quits out of this inventory the vanilla way.
	 */
	public void onClosed(HumanEntity viewer) {
		if(inventory.getViewers().size() <= 1) //last viewer closing inventory
			GUIListener.deregisterGUI(inventory);
	}

	//HELPER
	@SuppressWarnings("ConstantConditions")
	protected static void setName(ItemStack stack, String name) {
		name = ChatColor.WHITE + name;
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		stack.setItemMeta(meta);
	}

	protected static void setLore(ItemStack stack, String... lore) {
		setLore(stack, Arrays.asList(lore));
	}
	@SuppressWarnings("ConstantConditions")
	protected static void setLore(ItemStack stack, List<String> lore) {
		ItemMeta meta = stack.getItemMeta();
		meta.setLore(lore);
		stack.setItemMeta(meta);
	}

	@SuppressWarnings("ConstantConditions")
	protected static void insertLore(ItemStack stack, int index, String... lore) {
		ItemMeta meta = stack.getItemMeta();
		List<String> currLore = meta.getLore();
		if(currLore == null)
			currLore = new ArrayList<>();
		if(index != -1)
			currLore.addAll(index, Arrays.asList(lore));
		else
			currLore.addAll(Arrays.asList(lore));
		meta.setLore(currLore);
		stack.setItemMeta(meta);
	}
}
