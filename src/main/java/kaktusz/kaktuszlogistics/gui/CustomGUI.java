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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("SameParameterValue")
public abstract class CustomGUI {

	protected static final int INVENTORY_WIDTH = 9;
	protected final Inventory inventory;

	private boolean needsSetup = true;
	private final ConcurrentMap<HumanEntity, CustomGUI> childGUI = new ConcurrentHashMap<>();
	private final ConcurrentMap<HumanEntity, CustomGUI> parentGUI = new ConcurrentHashMap<>();

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
	 * @param parent If the parent GUI is forcibly closed, so will be this one
	 */
	public void open(HumanEntity viewer, CustomGUI parent) {
		if(parent != null) {
			//keep track of parent (for this viewer)
			parentGUI.put(viewer, parent);
			//set self as parent's child (for this viewer)
			parent.childGUI.put(viewer, this);
		}

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
	 * Kicks out all viewers from viewing this inventory (and its children)
	 */
	public void forceClose() {
		//close the child guis recursively
		for(Map.Entry<HumanEntity, CustomGUI> entry : childGUI.entrySet()) {
			entry.getValue().forceClose();
		}

		//close self for all viewers
		GUIListener.deregisterGUI(inventory);
		new ArrayList<>(inventory.getViewers()).forEach(v -> {
				close(v);
				onClosedForcefully(v);
		});
	}

	public boolean hasViewers() {
		return !inventory.getViewers().isEmpty();
	}

	//EVENTS
	public void onClick(ClickType type, int slot, HumanEntity player) {
		
	}

	/**
	 * Called when the player quits out of this inventory, but not if it was caused by forceClose()
	 */
	public void onClosed(HumanEntity viewer) {
		if(inventory.getViewers().size() <= 1) //last viewer closing inventory
			GUIListener.deregisterGUI(inventory);

		deregisterFromParent(viewer);
	}

	/**
	 * Called when the player quits out of this inventory due to forceClose() being called
	 */
	protected void onClosedForcefully(HumanEntity viewer) {
		deregisterFromParent(viewer);
	}

	private void deregisterFromParent(HumanEntity viewer) {
		CustomGUI parent = parentGUI.get(viewer);
		if(parent != null) {
			parent.childGUI.remove(viewer);
			parentGUI.remove(viewer);
		}
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
		if(stack == null)
			return;
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
