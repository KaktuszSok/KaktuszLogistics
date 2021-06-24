package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.gui.MachineGUI;
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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;
import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.serialisablesFromBytes;
import static kaktusz.kaktuszlogistics.world.multiblock.components.DecoratorSpecialBlock.SpecialType;

/**
 * A core block of a multiblock structure, which can perform recipes
 */
public abstract class MultiblockMachine extends MultiblockBlock implements TickingBlock {

	public static NamespacedKey CHOSEN_RECIPE_KEY;
	public static NamespacedKey PROCESSING_INPUTS_KEY; //Key where the inputs that we are currently processing are stored
	public static NamespacedKey HALTED_KEY;
	public static NamespacedKey TIME_LEFT_KEY;
	public static final SFXCollection RECIPE_DONE_SOUND = new SFXCollection(
			new SoundEffect(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.4f)
	);

	private transient MachineRecipe<?> recipeCache = null;
	/**
	 * refreshes whenever gatherAllInputs() is called
	 */
	private transient IRecipeInput[] suppliesCache = null;
	/**
	 * Is there currently a recipe being performed? (regardless of halt state)
	 */
	private boolean isProcessing = false;
	private boolean halted = false; //aka paused
	private int timeLeft = -1;
	private transient MachineGUI gui;

	public MultiblockMachine(Multiblock property, Location location, ItemMeta meta) {
		super(property, location, meta);
		initGUI();
	}

	//BEHAVIOUR
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
		if(!isStructureValid()) //validate structure
			return;

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
		if(halted)
			return;

		boolean shouldUpdateGUI = gui.hasViewers();
		if(shouldUpdateGUI) {
			//occasional GUI update regardless of machine processing or not
			if(VanillaUtils.getTickTime() % 20 == 0) {
				gatherAllInputs(); //refresh the supplies cache every 1.5s, so that the recipe display updates correctly
				updateGUI();
			}
		}

		if(!isProcessing) //the rest of this function only needs to be ran if we're processing a recipe
			return;

		timeLeft--;
		if(timeLeft < 0)
			onProcessingFinished();

		//GUI
		if(shouldUpdateGUI) {
			updateGUI();
		}
	}

	@Override
	public void onSave() {
		CustomItem.setNBT(data, TIME_LEFT_KEY, PersistentDataType.INTEGER, timeLeft);
	}

	@Override
	protected void onVerificationFailed() {
		gui.forceClose();
	}

	//TODO: make processing inputs drop when block is broken

	//GUI & INFO
	public abstract Material getGUIHeader();
	public int getTimeLeft() {
		return timeLeft;
	}
	public boolean isProcessingRecipe() {
		return isProcessing;
	}
	public boolean isHalted() {
		return halted;
	}
	public IRecipeInput[] getCachedSupplies() {
		return suppliesCache;
	}
	/**
	 * Gets all recipes that this machine supports.
	 */
	public List<MachineRecipe<?>> getAllRecipes() {
		List<MachineRecipe<?>> result = new ArrayList<>();
		for (String prefix : getSupportedRecipePrefixes()) {
			result.addAll(RecipeManager.getMachineRecipesWithPrefix(prefix));
		}
		return result.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * Usually this should just return one prefix, unless the machine supports recipes of other machines.
	 */
	protected abstract List<String> getSupportedRecipePrefixes();

	protected SFXCollection getRecipeDoneSound() {
		return RECIPE_DONE_SOUND;
	}

	//ACTIONS
	protected void openGUI(HumanEntity player) {
		if(!isStructureValid())
			return;
		gui.open(player);
		updateGUI();
	}

	protected void initGUI() {
		gui = new MachineGUI(this);
	}

	private void updateGUI() {
		gui.update();
	}

	public void setRecipe(MachineRecipe<?> recipe) {
		if(recipe == getRecipe())
			return;
		abortProcessing(); //stop current recipe

		recipeCache = recipe;
		if(recipe != null)
			CustomItem.setNBT(data, CHOSEN_RECIPE_KEY, PersistentDataType.STRING, recipe.id);
		else
			CustomItem.setNBT(data, CHOSEN_RECIPE_KEY, PersistentDataType.STRING, null);

		gatherAllInputs(); //update supplies cache so that GUI is up-to-date
		updateGUI();
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
		if(!isStructureValid())
			return false;
		if(isProcessing || halted)
			return false;
		MachineRecipe<?> recipe = getRecipe();
		if(recipe == null)
			return false;

		MachineRecipe.ConsumptionAftermath consumption = recipe.consumeInputs(gatherAllInputs());
		if(consumption == null)
			return false; //recipe didnt match

		List<IRecipeInput> consumed = consumption.applyToOriginal();
		setProcessingInputs(consumed);

		isProcessing = true;
		timeLeft = recipe.time;
		return true;
	}

	protected void onProcessingFinished() {
		isProcessing = false;
		if(!isStructureValid()) {
			toggleProcessingPaused(false);
			return;
		}
		gatherAllInputs(); //refresh supplies cache so that the GUI updates as soon as it needs to

		//get the recipe we've finished
		MachineRecipe<?> recipe = getRecipe();
		if(recipe == null)
			return;

		//get the consumed inputs from when the recipe was started
		List<IRecipeInput> processingInputs = getProcessingInputs();
		CustomItem.setNBT(data, PROCESSING_INPUTS_KEY, PersistentDataType.BYTE_ARRAY, null); //clear saved processing inputs
		if (processingInputs == null) return;
		setProcessingInputs(null); //clear inputs
		List<? extends IRecipeOutput> outputs = recipe.getOutputsMatching(processingInputs.toArray(new IRecipeInput[0]));
		if(outputs == null)
			return;

		HashMap<SpecialType, Integer> outputTypesCounter = new HashMap<>();
		for(IRecipeOutput output : outputs) {
			//get type of output block we're looking for
			SpecialType type = output.getOutputBlockType();
			//see if we cached the appropriate output blocks.
			Set<BlockPosition> outputBlocksSet = specialBlocksCache.get(type);
			if(outputBlocksSet == null) {
				output.placeInWorld(location.getWorld(), new BlockPosition(location));
				continue;
			}
			BlockPosition[] outputBlocks = outputBlocksSet.toArray(new BlockPosition[0]);
			//see if we have a tally for how many outputs, that share the same output block type, we have outputted already
			Integer counter = outputTypesCounter.get(type);
			if(counter == null) {
				counter = 0;
			}
			//determine the block we will put this output into based on the tally.
			BlockPosition appropriateBlock = outputBlocks[counter % outputBlocks.length];
			output.placeInWorld(location.getWorld(), appropriateBlock);

			counter++;
			outputTypesCounter.put(type, counter);
		}
		getRecipeDoneSound().playAll(location);
	}

	/**
	 * Abort the current recipe
	 */
	public void abortProcessing() {
		isProcessing = false;
		setProcessingInputs(null);
		timeLeft = -1;
		updateGUI();
	}

	/**
	 * Pause/unpause processing of current recipe (aka halted/not halted)
	 */
	public void toggleProcessingPaused() {
		toggleProcessingPaused(!halted);
	}
	/**
	 * Pause/unpause processing of current recipe (aka halted/not halted)
	 * @param halt True to pause , false to unpause
	 */
	public void toggleProcessingPaused(boolean halt) {
		halted = halt;
		CustomItem.setNBT(data, HALTED_KEY, PersistentDataType.BYTE, halted ? (byte)1 : 0);
		gui.update();
	}

	//HELPER
	protected List<IRecipeInput> getProcessingInputs() {
		byte[] processingInputsSerialised = CustomItem.readNBT(data, PROCESSING_INPUTS_KEY, PersistentDataType.BYTE_ARRAY);
		if(processingInputsSerialised == null)
			return null;

		return serialisablesFromBytes(processingInputsSerialised);
	}

	protected void setProcessingInputs(List<IRecipeInput> consumed) {
		if(consumed == null)
			CustomItem.setNBT(data, PROCESSING_INPUTS_KEY, PersistentDataType.BYTE_ARRAY, null);
		else
			CustomItem.setNBT(data, PROCESSING_INPUTS_KEY, PersistentDataType.BYTE_ARRAY, VanillaUtils.serialisablesToBytes(consumed));
	}

	private IRecipeInput[] gatherAllInputs() {
		List<IRecipeInput> inputs = new ArrayList<>();
		//items:
		Set<BlockPosition> itemInputBlocks = specialBlocksCache.get(SpecialType.ITEM_INPUT);
		if(itemInputBlocks == null)
			return suppliesCache = new IRecipeInput[0];
		for(BlockPosition inputBlock : itemInputBlocks) {
			//noinspection ConstantConditions
			Stream<ItemInput> items = ItemInput.getInputsFromPosition(location.getWorld(), inputBlock);
			if(items != null)
				inputs.addAll(items.collect(Collectors.toList()));
		}

		return suppliesCache = inputs.toArray(new IRecipeInput[0]);
	}
}
