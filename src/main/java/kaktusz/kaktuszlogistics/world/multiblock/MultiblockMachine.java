package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.gui.MachineGUI;
import kaktusz.kaktuszlogistics.items.properties.multiblock.MultiblockTemplate;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import kaktusz.kaktuszlogistics.recipe.machine.MachineRecipe;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import kaktusz.kaktuszlogistics.util.StringUtils;
import kaktusz.kaktuszlogistics.util.minecraft.SFXCollection;
import kaktusz.kaktuszlogistics.util.minecraft.SoundEffect;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.KLWorld;
import kaktusz.kaktuszlogistics.world.LabourConsumer;
import kaktusz.kaktuszlogistics.world.TickingBlock;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

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
	private static final double CHILD_LABOUR_CHANCE = 0.1;

	private transient MachineGUI gui;
	/**
	 * Cache of available recipe supplies.
	 * Refreshes whenever gatherAllInputs() is called.
	 */
	private transient IRecipeInput[] suppliesCache = null;
	private transient MachineRecipe<?> currentRecipe = null;
		/**
	 * The recipe inputs which we are currently processing
	 */
	private List<IRecipeInput> processingInputs = null;
	private boolean halted = false; //aka paused
	private int timeLeft = -1;
	private boolean automationOn = false;
	/**
	 * Stores the last result of validateAndFixSupply()
	 */
	private boolean labourReqMetLastCheck = false;
	private final Set<BlockPosition> labourSuppliers = new HashSet<>();
	//labour display:
	private transient LivingEntity labourerEntity = null;
	private UUID labourerUUID = null;

	public MultiblockMachine(MultiblockTemplate property, Location location, ItemMeta meta) {
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
		if(halted || !isStructureValid_cached())
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
			if(isAutomationOn() && getRecipe() != null) {
				Location loc = getLocation();
				int tickTimeOffset = loc.getBlockX() + loc.getBlockY() + loc.getBlockZ();
				if ((VanillaUtils.getTickTime() + tickTimeOffset) % 400 == 0) {
					tryStartProcessingByAutomation();
				}
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
		deregisterFromAllSuppliers();
	}

	@Override
	public void onRemoved(KLWorld world, int x, int y, int z) {
		super.onRemoved(world, x, y, z);
		gui.forceClose();
		deregisterFromAllSuppliers();
	}

	//DISPLAY & INFO

	/**
	 * Get the material for the icon used to represent this machine in the GUI
	 */
	public abstract Material getGUIHeader();

	/**
	 * @return True if the machine is currently processing a recipe
	 */
	public boolean isProcessingRecipe() {
		return processingInputs != null;
	}

	/**
	 * @return The time until the current recipe is complete, or -1 if no recipe is being processed
	 */
	public int getTimeLeft() {
		return timeLeft;
	}

	/**
	 * @return True if the machine is halted
	 */
	public boolean isHalted() {
		return halted;
	}

	/**
	 * @return True if the machine will try to automate processing by use of nearby suppliers (i.e. labour)
	 */
	public boolean isAutomationOn() {
		return automationOn;
	}

	/**
	 * @return True if the last automation supply check was successful
	 */
	public boolean isAutomationSupplied() {
		return labourReqMetLastCheck;
	}

	/**
	 * Get the last result of available recipe supplies
	 */
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

	protected SFXCollection getRecipeDoneSound(MachineRecipe<?> recipe) {
		return DEFAULT_RECIPE_DONE_SOUND;
	}

	@SuppressWarnings("unused")
	protected BlockData getRecipeDoneParticlesData(MachineRecipe<?> recipe) {
		return getLocation().getBlock().getBlockData();
	}

	@SuppressWarnings("ConstantConditions")
	protected void playRecipeDoneEffect(MachineRecipe<?> recipe) {
		SFXCollection sfx = getRecipeDoneSound(recipe);
		if(sfx != null)
			sfx.playAll(getLocation());
		getLocation().getWorld().spawnParticle(Particle.BLOCK_CRACK, getLocation().clone().add(0.5d,0.5d,0.5d),
				5,0.25d,0.25d,0.25d, getRecipeDoneParticlesData(recipe));
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

	/**
	 * Set the recipe assigned to this machine
	 */
	public void setRecipe(MachineRecipe<?> recipe) {
		if(recipe == getRecipe())
			return;
		abortProcessing(); //stop current recipe

		currentRecipe = recipe;
		if(isAutomationOn()) {
			if(recipe != null)
				tryStartProcessingByAutomation();
			else
				deregisterFromAllSuppliers();
		}

		gatherAllInputs(); //update supplies cache so that GUI is up-to-date
		updateGUI();
	}

	/**
	 * Get the recipe that this machine was assigned
	 */
	public MachineRecipe<?> getRecipe() {
		return currentRecipe;
	}

	/**
	 * Tries to start the recipe through means of automation, registering with suppliers if needed.
	 * Checks if automation criteria are met.
	 */
	public void tryStartProcessingByAutomation() {
		if(!halted && isAutomationOn() && isStructureValid() && getRecipe() != null && validateAndFixSupply()) {
			tryStartProcessing();
		}
		gui.updateHeader();
	}

	/**
	 * Try start processing the currently assigned recipe
	 */
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

	/**
	 * Called when the current recipe completes
	 */
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
			playRecipeDoneEffect(recipe);
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
	 * @param halt True to pause, false to unpause
	 */
	public void toggleProcessingPaused(boolean halt) {
		halted = halt;
		if(halt)
			deregisterFromAllSuppliers();
		else
			tryStartProcessingByAutomation();
		gui.update();
	}

	/**
	 * Toggle whether the machine will try to use nearby suppliers (i.e. labour) to automate processing
	 */
	public void toggleAutomation() {
		toggleAutomation(!automationOn);
	}
	/**
	 * Toggle whether the machine will try to use nearby suppliers (i.e. labour) to automate processing
	 */
	public void toggleAutomation(boolean automate) {
		automationOn = automate;
		if(!automate) {
			deregisterFromAllSuppliers();
		}
		else {
			tryStartProcessingByAutomation();
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

	@Override
	public void onValidateSupplyFinished(boolean requirementsMet) {

		if(requirementsMet) {
			LivingEntity labourer = getLabourerEntity();
			if(labourer == null)
				labourer = spawnLabourerEntity();
			//TODO colours
			labourer.setCustomName(ChatColor.GRAY +
					"[Using " + StringUtils.formatDouble(getRequiredLabour()) + " labour/day]");
		}
		else
			despawnLabourerEntity();
		labourReqMetLastCheck = requirementsMet;
	}

	@Override
	public void deregisterFromAllSuppliers() {
		LabourConsumer.super.deregisterFromAllSuppliers();
		despawnLabourerEntity();
		gui.updateHeader();
	}

	/**
	 * Spawns an entity representing the labourer working this machine.
	 * Updates the labourer entity reference and UUID to the newly spawned entity.
	 * @return The new value of the labourer entity reference
	 */
	protected LivingEntity spawnLabourerEntity() {
		//TODO entity location dictated by special block decorator
		Location entityLocation = getLocation().clone().add(getFacing().getDirection());
		entityLocation.setDirection(getFacing().getOppositeFace().getDirection());
		entityLocation.add(0.5d, 0.0d, 0.5d);
		@SuppressWarnings("ConstantConditions")
		LivingEntity labourer = (LivingEntity) getLocation().getWorld().spawnEntity(entityLocation, EntityType.VILLAGER);
		labourer.setCustomNameVisible(true);
		labourer.setInvulnerable(true);
		labourer.setPersistent(true);
		labourer.setAI(false);
		if(MathsUtils.rollChance100(CHILD_LABOUR_CHANCE) && labourer instanceof Ageable) {
			((Ageable)labourer).setBaby();
		}
		//look at machine
		Vector lookDir = getLocation().clone().add(0.5d, 0.0d, 0.5d).toVector()
				.subtract(labourer.getEyeLocation().toVector());
		entityLocation.setDirection(lookDir);
		Bukkit.getScheduler().runTaskLater(KaktuszLogistics.INSTANCE, ()->labourer.teleport(entityLocation), 2);

		labourerEntity = labourer;
		labourerUUID = labourer.getUniqueId();

		return labourer;
	}

	/**
	 * Despawns the labourer entity (if it exists) and clears the cached reference and UUID
	 */
	protected void despawnLabourerEntity() {
		Entity labourer = getLabourerEntity();
		if(labourer != null)
			labourer.remove();
		labourerEntity = null;
		labourerUUID = null;
	}

	/**
	 * Returns the cached labourer entity or, if it's null, tries to find it using the saved UUID.
	 * Updates the labourer entity reference if it was null.
	 * @return The new value of the labourer entity reference
	 */
	protected LivingEntity getLabourerEntity() {
		//try use cached reference
		if(labourerEntity != null) {
			if(!labourerEntity.isValid())
				labourerEntity = null;
			else
				return labourerEntity;
		}

		//resort to UUID
		if(labourerUUID == null)
			return null;

		Entity foundEntity = Bukkit.getEntity(labourerUUID);
		if(!(foundEntity instanceof LivingEntity))
			return null;
		return labourerEntity = (LivingEntity) foundEntity;
	}

	//HELPER

	/**
	 * Get the recipe inputs that were consumed for the current recipe (null if no recipe is being processed)
	 */
	protected List<IRecipeInput> getProcessingInputs() {
		return processingInputs;
	}
	/**
	 * Set the recipe inputs for the recipe currently being processed
	 */
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
