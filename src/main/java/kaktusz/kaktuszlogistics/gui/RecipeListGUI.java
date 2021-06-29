package kaktusz.kaktuszlogistics.gui;

import kaktusz.kaktuszlogistics.recipe.machine.MachineRecipe;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockMachine;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public class RecipeListGUI extends ListGUI {

	private final MultiblockMachine machine;
	public RecipeListGUI(MachineGUI source) {
		super(6*INVENTORY_WIDTH, source.machine.getName() + " Recipes", source);

		machine = source.machine;
		for (MachineRecipe<?> recipe : machine.getAllRecipes()) {
			addElement(new ListElement(
					new GUIButton().setLeftClickAction(v -> {
						machine.setRecipe(recipe);
						close(v);
					}),
					recipe.getDisplayIcon(machine.getCachedSupplies())
			));
		}
	}

	@Override
	protected void clearInventory() {
		super.clearInventory();

		GUIButton clearButton = new GUIButton().setLeftClickAction(v -> {
			machine.setRecipe(null);
			close(v);
		});
		ItemStack clearIcon = new ItemStack(Material.PAINTING);
		setName(clearIcon, "Clear Recipe");

		int rows = inventory.getSize() / INVENTORY_WIDTH;
		addButton(rows-1, INVENTORY_WIDTH-1,
				clearButton,
				clearIcon);
	}

	@Override
	public void open(HumanEntity viewer, CustomGUI parent) {
		super.open(viewer, parent);

		renderPage(0);
	}
}
