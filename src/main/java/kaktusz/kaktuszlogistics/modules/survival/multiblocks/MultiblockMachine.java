package kaktusz.kaktuszlogistics.modules.survival.multiblocks;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import kaktusz.kaktuszlogistics.recipe.machine.MachineRecipe;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;
import kaktusz.kaktuszlogistics.util.minecraft.SFXCollection;
import kaktusz.kaktuszlogistics.util.minecraft.SoundEffect;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.TickingBlock;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;
import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.serialisablesFromBytes;

public abstract class MultiblockMachine extends MultiblockBlock implements TickingBlock {

	public static NamespacedKey CHOSEN_RECIPE_KEY;
	public static NamespacedKey PROCESSING_INPUTS_KEY; //Key where the inputs that we are currently processing are stored
	public static NamespacedKey HALTED_KEY;
	public static NamespacedKey TIME_LEFT_KEY;
	public static final SFXCollection RECIPE_DONE_SOUND = new SFXCollection(
			new SoundEffect(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.4f)
	);

	private transient MachineRecipe<?> recipeCache = null;
	private transient boolean isProcessing = false;
	private transient boolean halted = false;
	private transient int timeLeft = -1;

	public MultiblockMachine(Multiblock property, Location location, ItemMeta meta) {
		super(property, location, meta);
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
		pdc.remove(HALTED_KEY);
		pdc.remove(TIME_LEFT_KEY);

		drop.setItemMeta(meta);
		return drop;
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {
		super.onInteracted(e);

		openGUI(e.getPlayer());
	}

	@Override
	public void onLoaded() {
		isProcessing = data.getPersistentDataContainer().has(PROCESSING_INPUTS_KEY, PersistentDataType.BYTE_ARRAY);
		Integer timeLeft = CustomItem.readNBT(data, TIME_LEFT_KEY, PersistentDataType.INTEGER);
		if(timeLeft == null)
			this.timeLeft = -1;
		else
			this.timeLeft = timeLeft;
		Byte halted = CustomItem.readNBT(data, HALTED_KEY, PersistentDataType.BYTE);
		if(halted == null)
			this.halted = false;
		else
			this.halted = halted == 1;
	}

	@Override
	public void onTick() {
		if(!isProcessing || halted)
			return;

		timeLeft--;
		if(timeLeft < 0)
			onProcessingFinished();
	}

	@Override
	public void onSave() {
		CustomItem.setNBT(data, TIME_LEFT_KEY, PersistentDataType.INTEGER, timeLeft);
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

		MachineRecipe.ConsumptionAftermath consumption = recipe.consumeInputs(gatherAllInputs());
		if(consumption == null)
			return false; //recipe didnt match

		List<IRecipeInput> consumed = consumption.applyToOriginal();
		CustomItem.setNBT(data, PROCESSING_INPUTS_KEY, PersistentDataType.BYTE_ARRAY, VanillaUtils.serialiseToBytes(consumed));

		isProcessing = true;
		timeLeft = recipe.time;
		return true;
	}

	public void onProcessingFinished() {
		isProcessing = false;

		//get the recipe we've finished
		MachineRecipe<?> recipe = getRecipe();
		if(recipe == null)
			return;

		//get the consumed inputs from when the recipe was started
		byte[] processingInputsSerialised = CustomItem.readNBT(data, PROCESSING_INPUTS_KEY, PersistentDataType.BYTE_ARRAY);
		if(processingInputsSerialised == null)
			return;

		List<IRecipeInput> processingInputs = serialisablesFromBytes(processingInputsSerialised);
		List<? extends IRecipeOutput> outputs = recipe.getOutputsMatching(processingInputs.toArray(new IRecipeInput[0]));
		if(outputs == null)
			return;

		HashMap<Class<? extends IRecipeOutput>, BlockPosition[]> outputBlocksPerType = new HashMap<>();
		HashMap<Class<? extends IRecipeOutput>, Integer> outputTypesCounter = new HashMap<>();
		for(IRecipeOutput output : outputs) {
			//get type of output block we're looking for
			Class<? extends IRecipeOutput> type = output.getOutputBlockType();
			//see if we cached the appropriate output blocks.
			BlockPosition[] outputBlocks = outputBlocksPerType.get(type);
			if(outputBlocks == null) {
				outputBlocks = getProperty().getOutputs(this, type).toArray(new BlockPosition[0]);
				outputBlocksPerType.put(type, outputBlocks);
			}
			//see if we have a tally for how many outputs, that share the same output block type, we outputted
			Integer counter = outputTypesCounter.get(type);
			if(counter == null) {
				counter = 0;
			}
			//determine the block we will put this output into based on the tally.
			BlockPosition appropriateBlock = outputBlocks[counter % outputBlocks.length];
			output.placeInWorld(location.getWorld(), appropriateBlock);

			counter++;
			outputTypesCounter.put(type, counter);

			RECIPE_DONE_SOUND.playAll(location);
		}


	}

	public void abortProcessing() {
		CustomItem.setNBT(data, PROCESSING_INPUTS_KEY, PersistentDataType.BYTE_ARRAY, null);
	}

	/**
	 * Pause/unpause processing of current recipe
	 * @param halt True to pause, false to unpause
	 */
	public void toggleProcessing(boolean halt) {
		halted = halt;
		CustomItem.setNBT(data, HALTED_KEY, PersistentDataType.BYTE, halted ? (byte)1 : 0);
	}

	//TODO: make processing inputs drop when block is broken

	//HELPER
	private IRecipeInput[] gatherAllInputs() {
		List<IRecipeInput> inputs = new ArrayList<>();
		//items:
		Set<BlockPosition> itemInputBlocks = getProperty().getInputs(this, ItemInput.class);
		for(BlockPosition inputBlock : itemInputBlocks) {
			//noinspection ConstantConditions
			Stream<ItemInput> items = ItemInput.getInputsFromPosition(location.getWorld(), inputBlock);
			if(items != null)
				inputs.addAll(items.collect(Collectors.toList()));
		}

		return inputs.toArray(new IRecipeInput[0]);
	}
}
