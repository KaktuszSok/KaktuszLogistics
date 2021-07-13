package kaktusz.kaktuszlogistics.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Consumer;

public class InteractableGUI extends CustomGUI {

	public static class GUIButton {
		private Consumer<HumanEntity> leftClickAction;
		private Consumer<HumanEntity> rightClickAction;
		private Consumer<HumanEntity> shiftLeftClickAction;
		private Consumer<HumanEntity> shiftRightClickAction;

		public GUIButton() {

		}

		public GUIButton setLeftClickAction(Consumer<HumanEntity> leftClickAction) {
			this.leftClickAction = leftClickAction;

			return this;
		}

		public GUIButton setRightClickAction(Consumer<HumanEntity> rightClickAction) {
			this.rightClickAction = rightClickAction;

			return this;
		}

		public GUIButton setShiftLeftClickAction(Consumer<HumanEntity> shiftLeftClickAction) {
			this.shiftLeftClickAction = shiftLeftClickAction;

			return this;
		}

		public GUIButton setShiftRightClickAction(Consumer<HumanEntity> shiftRightClickAction) {
			this.shiftRightClickAction = shiftRightClickAction;

			return this;
		}

		public void onClick(ClickType type, HumanEntity player) {
			if(type.isLeftClick()) {
				if(!type.isShiftClick())
					runAction(leftClickAction, player);
				else
					runAction(shiftLeftClickAction, player);
			}
			else if(type.isRightClick()) {
				if(!type.isShiftClick())
					runAction(rightClickAction, player);
				else
					runAction(shiftRightClickAction, player);
			}
		}

		private void runAction(Consumer<HumanEntity> action, HumanEntity player) {
			if(action == null)
				return;

			action.accept(player);
		}
	}

	private final GUIButton[] buttons;

	public InteractableGUI(int size, String title) {
		super(size, title);
		this.buttons = new GUIButton[size];
	}

	@Override
	protected void clearInventory() {
		super.clearInventory();
		Arrays.fill(buttons, null);
	}

	/**
	 * Sets the given slot to a certain button
	 */
	protected void addButton(int row, int column, GUIButton button, ItemStack icon) {
		int slot = row*INVENTORY_WIDTH + column;
		if(slot < inventory.getSize()) {
			buttons[row * INVENTORY_WIDTH + column] = button;
			setSlot(row, column, icon);
		}
	}

	@Override
	public void onClick(ClickType type, int slot, HumanEntity player) {
		if(slot >= 0 && slot < inventory.getSize()) {
			if(buttons[slot] != null)
				buttons[slot].onClick(type, player);
		}
	}
}
