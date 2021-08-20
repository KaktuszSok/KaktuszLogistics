package kaktusz.kaktuszlogistics.modules.survival;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.commands.KLCommand;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.modules.KaktuszModule;
import kaktusz.kaktuszlogistics.modules.survival.commands.HouseSubcommand;
import kaktusz.kaktuszlogistics.modules.survival.commands.RoomSubcommand;
import kaktusz.kaktuszlogistics.modules.survival.world.multiblocks.woodworking.SawmillWood;
import kaktusz.kaktuszlogistics.modules.survival.world.housing.RoomInfo;
import kaktusz.kaktuszlogistics.modules.survival.world.housing.SignEventListener;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.recipe.ingredients.WoodIngredient;
import kaktusz.kaktuszlogistics.recipe.machine.MachineRecipe;
import kaktusz.kaktuszlogistics.recipe.machine.WoodMachineRecipe;
import kaktusz.kaktuszlogistics.recipe.outputs.ItemOutput;
import kaktusz.kaktuszlogistics.recipe.outputs.WoodOutput;
import kaktusz.kaktuszlogistics.util.minecraft.config.BooleanOption;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigOption;
import kaktusz.kaktuszlogistics.util.minecraft.config.IntegerOption;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Slab;

import java.util.ArrayList;
import java.util.List;

public class KaktuszSurvival implements KaktuszModule {
	@SuppressWarnings("unused")
	public static KaktuszSurvival INSTANCE;

	//config
	private static final List<ConfigOption<?>> ALL_OPTIONS = new ArrayList<>();
	/**
	 * Maximum extents of room, including walls. An enclosed area bigger than this is considered outside.
	 */
	public static final IntegerOption ROOM_MAX_SIZE_HORIZONTAL = new IntegerOption("survival.housing.room.maxSizeHorizontal", 33, ALL_OPTIONS);
	/**
	 * Maximum extents of room, including floor and ceiling. An enclosed area bigger than this is considered outside.
	 */
	public static final IntegerOption ROOM_MAX_SIZE_VERTICAL = new IntegerOption("survival.housing.room.maxSizeVertical", 12, ALL_OPTIONS);
	/**
	 * Maximum volume of room, including walls, floors and ceilings. An enclosed area bigger than this is considered outside.
	 */
	public static int ROOM_MAX_VOLUME = ROOM_MAX_SIZE_HORIZONTAL.getValue() * ROOM_MAX_SIZE_VERTICAL.getValue() * ROOM_MAX_SIZE_HORIZONTAL.getValue() /3;
	/**
	 * How often the house is re-checked, in seconds
	 */
	public static final IntegerOption HOUSE_RECHECK_FREQUENCY = new IntegerOption("survival.housing.houseRecheckFrequency", 20*60, ALL_OPTIONS);
	public static final BooleanOption CALC_ROOMS_ASYNC = new BooleanOption("survival.housing.room.calculateRoomsAsync", false, ALL_OPTIONS);

	public void initialise() {
		INSTANCE = this;

		//do precalculations
		precalcSlabs();

		//update config-based pseudoconstants
		ROOM_MAX_VOLUME = ROOM_MAX_SIZE_HORIZONTAL.getValue() * ROOM_MAX_SIZE_VERTICAL.getValue() * ROOM_MAX_SIZE_HORIZONTAL.getValue() /3;

		//register event listeners
		Bukkit.getPluginManager().registerEvents(new SignEventListener(), KaktuszLogistics.INSTANCE);

		//add content
		initItemsAndRecipes();

		//register commands
		KLCommand.registerSubcommand(new RoomSubcommand("room"));
		KLCommand.registerSubcommand(new HouseSubcommand("house"));
	}

	private void initItemsAndRecipes() {
		CustomItemManager.registerItem(SignEventListener.HOUSE_SIGN_PROPERTY.item);
		CustomItemManager.registerItem(SignEventListener.GOODS_SIGN_PROPERTY.item);
		CustomItemManager.registerItem(SawmillWood.createCustomItem());

		MachineRecipe<ItemOutput> sawmill_strip_log = new WoodMachineRecipe<ItemOutput>("sawmill.strip_log", "Stripping", 18)
				.addOutputs(new WoodOutput(WoodIngredient.WOOD_ITEM.STRIPPED_XXX_LOG, 1))
				.addIngredients(new WoodIngredient(WoodIngredient.WOOD_ITEM.XXX_LOG, 1))
				.setDisplayIcon(Material.STRIPPED_OAK_LOG, 1);
		RecipeManager.addMachineRecipe(sawmill_strip_log);

		MachineRecipe<ItemOutput> sawmill_planks = new WoodMachineRecipe<ItemOutput>("sawmill.planks", "Wooden Planks", 30)
				.addOutputs(new WoodOutput(WoodIngredient.WOOD_ITEM.XXX_PLANKS, 3))
				.addIngredients(new WoodIngredient(WoodIngredient.WOOD_ITEM.STRIPPED_XXX_LOG, 1))
				.setDisplayIcon(Material.OAK_PLANKS, 3);
		RecipeManager.addMachineRecipe(sawmill_planks);
	}

	@Override
	public List<ConfigOption<?>> getAllOptions() {
		return ALL_OPTIONS;
	}

	private void precalcSlabs() {
		RoomInfo.SLABS.clear();
		for (Material mat : Material.values()) {
			if(Slab.class.isAssignableFrom(mat.data)) {
				RoomInfo.SLABS.add(mat);
			}
		}
	}
}
