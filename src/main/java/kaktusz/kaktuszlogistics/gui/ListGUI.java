package kaktusz.kaktuszlogistics.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ListGUI extends MultiPageGUI {

	public static class ListElement {
		public final GUIButton button;
		public final ItemStack icon;

		public ListElement(GUIButton button, ItemStack icon) {
			this.button = button;
			this.icon = icon;
		}
	}

	private final List<ListElement> elements = new ArrayList<>();

	public ListGUI(int size, String title, CustomGUI previousGUI) {
		super(Math.max(size, 4*INVENTORY_WIDTH), title, previousGUI); //size must be at least 4 rows (to accommodate for back button, next page, prev page and search)
	}

	protected void addElement(ListElement e) {
		elements.add(e);
	}

	protected void addElements(List<ListElement> e) {
		elements.addAll(e);
	}

	@Override
	protected void clearInventory() {
		super.clearInventory();

		//back button
		GUIButton back = new GUIButton()
				.setLeftClickAction(this::close);
		ItemStack backIcon = new ItemStack(Material.BARRIER);
		setName(backIcon, "Back");
		addButton(0, INVENTORY_WIDTH-1, back, backIcon);

		//vertical border
		ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		setName(border, "");
		for(int r = 0; r < inventory.getSize() / INVENTORY_WIDTH; r++) {
			setSlot(r, INVENTORY_WIDTH-2, border);
		}
	}

	protected void renderPage(int page) {
		clearInventory();

		int rows = inventory.getSize() / INVENTORY_WIDTH;
		int pageCapacity = rows * (INVENTORY_WIDTH-2);
		int maxPage = (int)Math.ceil(elements.size() / (float)pageCapacity) - 1; //index of last page
		maxPage = Math.max(maxPage, 0);
		page = Math.min(page, maxPage);

		if(page != 0) {
			final int pageCopy = page;
			GUIButton next = new GUIButton()
					.setLeftClickAction(x -> renderPage(pageCopy - 1));
			addButton(1, INVENTORY_WIDTH-1, next, new ItemStack(Material.SPECTRAL_ARROW));
		}
		if(page != maxPage) {
			final int pageCopy = page;
			GUIButton prev = new GUIButton()
					.setLeftClickAction(x -> renderPage(pageCopy + 1));
			addButton(2, INVENTORY_WIDTH-1, prev, new ItemStack(Material.GOLDEN_CARROT));
		}
		//TODO: search function

		int iOffset = page*pageCapacity;
		for(int i = iOffset; i < elements.size() && i < pageCapacity; i++) {
			int row = (i-iOffset) / (INVENTORY_WIDTH-2);
			int column = (i-iOffset) % (INVENTORY_WIDTH-2);
			ListElement e = elements.get(i);
			addButton(row, column, e.button, e.icon);
		}
	}
}
