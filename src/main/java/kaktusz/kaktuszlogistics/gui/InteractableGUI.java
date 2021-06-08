package kaktusz.kaktuszlogistics.gui;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class InteractableGUI extends CustomGUI {

	public static class GUIButton {
		private Runnable leftClickAction;
		private Runnable rightClickAction;
		private Runnable shiftLeftClickAction;
		private Runnable shiftRightClickAction;

		public GUIButton() {

		}

		public GUIButton setLeftClickAction(Runnable leftClickAction) {
			this.leftClickAction = leftClickAction;

			return this;
		}

		public GUIButton setRightClickAction(Runnable rightClickAction) {
			this.rightClickAction = rightClickAction;

			return this;
		}

		public GUIButton setShiftLeftClickAction(Runnable shiftLeftClickAction) {
			this.shiftLeftClickAction = shiftLeftClickAction;

			return this;
		}

		public GUIButton setShiftRightClickAction(Runnable shiftRightClickAction) {
			this.shiftRightClickAction = shiftRightClickAction;

			return this;
		}

		public void onClick(ClickType type) {
			if(type.isLeftClick()) {
				if(!type.isShiftClick())
					runAction(leftClickAction);
				else
					runAction(shiftLeftClickAction);
			}
			else if(type.isRightClick()) {
				if(!type.isShiftClick())
					runAction(rightClickAction);
				else
					runAction(shiftRightClickAction);
			}
		}

		private void runAction(Runnable action) {
			if(action == null)
				return;

			action.run();
		}
	}

	private final GUIButton[] buttons;

	public InteractableGUI(int size, String title) {
		super(size, title);
		this.buttons = new GUIButton[size];
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
	public void onClick(ClickType type, int slot) {
		if(slot < inventory.getSize()) {
			if(buttons[slot] != null)
				buttons[slot].onClick(type);
		}
	}
}
