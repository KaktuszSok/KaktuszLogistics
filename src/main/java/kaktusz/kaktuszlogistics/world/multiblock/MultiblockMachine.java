package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.gui.MachineGUI;
import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import kaktusz.kaktuszlogistics.recipe.machine.MachineRecipe;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;
import kaktusz.kaktuszlogistics.util.minecraft.SFXCollection;
import kaktusz.kaktuszlogistics.util.minecraft.SoundEffect;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.LabourConsumer;
import kaktusz.kaktuszlogistics.world.TickingBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;
import static kaktusz.kaktuszlogistics.world.multiblock.components.DecoratorSpecialBlock.SpecialType;

/**
 * A core block of a multiblock structure, which can perform recipes
 */
public abstract class MultiblockMachine extends MultiblockBlock implements TickingBlock, LabourConsumer {
	private static final long serialVersionUID = 100L;

	private static final SFXCollection DEFAULT_RECIPE_DONE_SOUND = new SFXCollection(
			new SoundEffect(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.4f)
	);

	private transient MachineGUI gui;
	/**
	 * refreshes whenever gatherAllInputs() is called
	 */
	private transient IRecipeInput[] suppliesCache = null;
	private transient MachineRecipe<?> currentRecipe = null;
	/**
	 * The recipe inputs which we are currently processing
	 */
	private List<IRecipeInput> processingInputs = null;
	private boolean halted = false; //aka paused
	private int timeLeft = -1;
	private boolean automationOn = true;
	private final Set<BlockPosition> labourSuppliers = new HashSet<>();

	public MultiblockMachine(Multiblock property, Location location, ItemMeta meta) {
		super(property, location, meta);
		initGUI();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(currentRecipe == null ? null : currentRecipe.id); //write recipe id string
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		currentRecipe = RecipeManager.getMachineRecipeById((String)in.readObject()); //read recipe id string
		setUpTransients();
	}

	@Override
	protected void setUpTransients() {
		super.setUpTransients();
		initGUI();
	}

	//BEHAVIOUR
	@Override
	public void onInteracted(PlayerInteractEvent e) {
		super.onInteracted(e);

		openGUI(e.getPlayer());
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

		if(!isProcessingRecipe()) {
			if(isAutomationOn() && VanillaUtils.getTickTime() % 400 == 0) {
				tryStartProcessingByAutomation();
			}
			return; //the rest of this function only needs to be ran if we're processing a recipe
		}

		timeLeft--;
		if(timeLeft < 0)
			onProcessingFinished();

		//GUI
		if(shouldUpdateGUI) {
			updateGUI();
		}
	}

	@Override
	protected void onVerificationFailed() {
		gui.forceClose();
	}

	@Override
	public void breakBlock(boolean dropItem, boolean playVanillaSound, Player playerWhoMined) {
		super.breakBlock(dropItem, playVanillaSound, playerWhoMined);
		gui.forceClose();
	}

	//GUI & INFO
	public abstract Material getGUIHeader();

	public boolean isProcessingRecipe() {
		return processingInputs != null;
	}

	public int getTimeLeft() {
		return timeLeft;
	}

	public boolean isHalted() {
		return halted;
	}

	public boolean isAutomationOn() {
		return automationOn;
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
		return DEFAULT_RECIPE_DONE_SOUND;
	}

	//ACTIONS
	protected void openGUI(HumanEntity player) {
		if(!isStructureValid())
			return;
		gui.open(player, null);
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

		currentRecipe = recipe;

		gatherAllInputs(); //update supplies cache so that GUI is up-to-date
		updateGUI();
	}

	public MachineRecipe<?> getRecipe() {
		return currentRecipe;
	}

	/**
	 * Tries to start the recipe through means of automation.
	 * Checks if automation criteria are met.
	 */
	private void tryStartProcessingByAutomation() {
		if(validateAndFixSupply())
			tryStartProcessing();
		else {
			//TODO update GUI
		}
	}

	@SuppressWarnings("UnusedReturnValue")
	public boolean tryStartProcessing() {
		if(!isStructureValid())
			return false;
		if(isProcessingRecipe() || halted)
			return false;
		MachineRecipe<?> recipe = getRecipe();
		if(recipe == null)
			return false;

		MachineRecipe.ConsumptionAftermath consumption = recipe.consumeInputs(gatherAllInputs());
		if(consumption == null)
			return false; //recipe didnt match

		List<IRecipeInput> consumed = consumption.applyToOriginal();
		setProcessingInputs(consumed);

		timeLeft = recipe.time;
		return true;
	}

	protected void onProcessingFinished() {
		if(!isStructureValid()) {
			toggleProcessingPaused(true);
			setProcessingInputs(null);
			return;
		}
		gatherAllInputs(); //refresh supplies cache so that the GUI updates as soon as it needs to

		//get the recipe we've finished
		MachineRecipe<?> recipe = getRecipe();
		if(recipe == null)
			return;

		//get the consumed inputs from when the recipe was started
		List<IRecipeInput> processedInputs = getProcessingInputs();
		setProcessingInputs(null); //clear inputs
		if (processedInputs != null) {
			Location location = getLocation();
			List<? extends IRecipeOutput> outputs = recipe.getOutputsMatching(processedInputs.toArray(new IRecipeInput[0]));
			if (outputs != null) {
				HashMap<SpecialType, Integer> outputTypesCounter = new HashMap<>();
				for (IRecipeOutput output : outputs) {
					//get type of output block we're looking for
					SpecialType type = output.getOutputBlockType();
					//see if we cached the appropriate output blocks.
					Set<BlockPosition> outputBlocksSet = specialBlocksCache.get(type);
					if (outputBlocksSet == null) {
						output.placeInWorld(location.getWorld(), new BlockPosition(location));
						continue;
					}
					BlockPosition[] outputBlocks = outputBlocksSet.toArray(new BlockPosition[0]);
					//see if we have a tally for how many outputs, that share the same output block type, we have outputted already
					Integer counter = outputTypesCounter.get(type);
					if (counter == null) {
						counter = 0;
					}
					//determine the block we will put this output into based on the tally.
					BlockPosition appropriateBlock = outputBlocks[counter % outputBlocks.length];
					output.placeInWorld(location.getWorld(), appropriateBlock);

					counter++;
					outputTypesCounter.put(type, counter);
				}
			}
			getRecipeDoneSound().playAll(location);
		}

		//automation
		if(isAutomationOn())
			tryStartProcessingByAutomation();
	}


	/**
	 * Abort the current recipe
	 */
	public void abortProcessing() {
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
		gui.update();
	}

	public void toggleAutomation() {
		toggleAutomation(!automationOn);
	}
	public void toggleAutomation(boolean automate) {
		automationOn = automate;
		if(!automate) {
			deregisterFromAllSuppliers();
		}
		else {
			requestLabour();
		}
	}

	//LABOUR
	@Override
	public Set<BlockPosition> getLabourSuppliers() {
		return labourSuppliers;
	}

	@Override
	public int getTier() {
		return 1;
	}

	@Override
	public double getRequiredLabour() {
		return 8;
	}

	//HELPER
	protected List<IRecipeInput> getProcessingInputs() {
		return processingInputs;
	}

	protected void setProcessingInputs(List<IRecipeInput> consumed) {
		processingInputs = consumed;
	}

	private IRecipeInput[] gatherAllInputs() {
		List<IRecipeInput> inputs = new ArrayList<>();
		//items:
		Set<BlockPosition> itemInputBlocks = specialBlocksCache.get(SpecialType.ITEM_INPUT);
		if(itemInputBlocks == null)
			return suppliesCache = new IRecipeInput[0];
		for(BlockPosition inputBlock : itemInputBlocks) {
			//noinspection ConstantConditions
			Stream<ItemInput> items = ItemInput.getInputsFromPosition(getLocation().getWorld(), inputBlock);
			if(items != null)
				inputs.addAll(items.collect(Collectors.toList()));
		}

		return suppliesCache = inputs.toArray(new IRecipeInput[0]);
	}
}
