package kaktusz.kaktuszlogistics.modules.survival.multiblocks;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.recipe.machine.MachineRecipe;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.world.TickingBlock;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiblockMachine extends MultiblockBlock implements TickingBlock {

	NamespacedKey CHOSEN_RECIPE_KEY;
	/**
	 * Key where the inputs that we are currently processing are stored
	 */
	NamespacedKey PROCESSING_INPUTS_KEY;

	private MachineRecipe<?> recipeCache = null;

	public MultiblockMachine(Multiblock property, ItemMeta meta) {
		super(property, meta);
	}

	@Override
	public ItemStack getDrop(Block block) {
		ItemStack drop = super.getDrop(block);
		ItemMeta meta = drop.getItemMeta();
		@SuppressWarnings("ConstantConditions")
		PersistentDataContainer pdc = meta.getPersistentDataContainer();

		//remove data that shouldn't persist after the block is broken
		pdc.remove(CHOSEN_RECIPE_KEY);
		pdc.remove(PROCESSING_INPUTS_KEY);

		drop.setItemMeta(meta);
		return drop;
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {
		super.onInteracted(e);

		openGUI(e.getPlayer());
	}

	@Override
	public void onTick() {

	}

	//ACTIONS
	protected abstract void openGUI(HumanEntity player);

	public void setRecipe(MachineRecipe<?> recipe) {
		if(recipe == getRecipe())
			return;
		abortProcessing(); //stop current recipe

		recipeCache = recipe;
		CustomItem.setNBT(data, CHOSEN_RECIPE_KEY, PersistentDataType.STRING, recipe.id);
		Bukkit.broadcastMessage("Set recipe to " + recipe.id);
	}

	public MachineRecipe<?> getRecipe() {
		if(recipeCache != null)
			return recipeCache;

		String recipeId = CustomItem.readNBT(data, CHOSEN_RECIPE_KEY, PersistentDataType.STRING);
		if(recipeId == null)
			return null;

		return recipeCache = RecipeManager.getMachineRecipeById(recipeId); //may be null
	}

	public boolean tryStartProcessing() {
		MachineRecipe<?> recipe = getRecipe();
		if(recipe == null)
			return false;

		recipe.getOutputsMatching(gatherAllInputs());
	}

	public void abortProcessing() {
		CustomItem.setNBT(data, PROCESSING_INPUTS_KEY, PersistentDataType.BYTE_ARRAY, null);
	}

	/**
	 * Pause/unpause processing of current recipe
	 * @param halt True to pause, false to unpause
	 */
	public void toggleProcessing(boolean halt) {

	}

	private IRecipeInput[] gatherAllInputs() {
		List<IRecipeInput> inputs = new ArrayList<>();

	}

	private void consumeInputs(IRecipeInput[] consume) {

	}
}
