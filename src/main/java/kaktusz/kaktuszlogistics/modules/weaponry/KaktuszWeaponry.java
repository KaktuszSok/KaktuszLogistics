package kaktusz.kaktuszlogistics.modules.weaponry;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.modules.KaktuszModule;
import kaktusz.kaktuszlogistics.modules.weaponry.input.GunActionsManager;
import kaktusz.kaktuszlogistics.modules.weaponry.input.PlayerContinuousShootingManager;
import kaktusz.kaktuszlogistics.modules.weaponry.items.GunItem;
import kaktusz.kaktuszlogistics.modules.weaponry.items.properties.PlaceableProximityMine;
import kaktusz.kaktuszlogistics.modules.weaponry.items.properties.ammo.AmmoContainer;
import kaktusz.kaktuszlogistics.modules.weaponry.items.properties.ammo.BulletContainer;
import kaktusz.kaktuszlogistics.recipe.CraftingRecipe;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.TaggedIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.VanillaIngredient;
import kaktusz.kaktuszlogistics.recipe.outputs.ItemOutput;
import kaktusz.kaktuszlogistics.util.minecraft.config.BooleanOption;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigOption;
import kaktusz.kaktuszlogistics.util.minecraft.config.IntegerOption;
import kaktusz.kaktuszlogistics.util.minecraft.config.StringOption;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

import java.util.ArrayList;
import java.util.List;

public class KaktuszWeaponry implements KaktuszModule {
	@SuppressWarnings("unused")
	public static KaktuszWeaponry INSTANCE;

	private static final List<ConfigOption<?>> ALL_OPTIONS = new ArrayList<>();
	public static final StringOption SHOT_MESSAGE = new StringOption("weaponry.shotMessage", "%k was shot by %s", ALL_OPTIONS);
	public static final IntegerOption INPUT_EVENTS_DELAY = new IntegerOption("weaponry.inputEventsDelay", 5, ALL_OPTIONS);
	public static final BooleanOption MOBS_TRIGGER_LANDMINES = new BooleanOption("weaponry.mobsTriggerLandmines", true, ALL_OPTIONS);

	public void initialise() {
		INSTANCE = this;

		Bukkit.getPluginManager().registerEvents(new GunActionsManager(), KaktuszLogistics.INSTANCE);
		PlayerContinuousShootingManager.INPUT_EVENTS_DELAY = INPUT_EVENTS_DELAY.getValue();

		initKeys();
		initItemsAndRecipes();
	}

	private void initKeys() {
		GunItem.LAST_SHOOT_TIME_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "LastShootTime");
		GunItem.LOADED_MAG_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "LoadedMagazine");
		AmmoContainer.AMMO_CONTAINER_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "AmmoContainer");
	}

	private void initItemsAndRecipes() {
		CustomItemManager.registerItem(new GunItem("toolTestGun", "AK-47", Material.PRISMARINE_SHARD)
				.addValidAmmoType(BulletContainer.BulletType.RIFLE_7_62x39mm));
		CustomItemManager.registerItem(new CustomItem("ammoTestMag", "AK-47 Standard Magazine", Material.FLINT))
				.getOrAddProperty(BulletContainer.class)
				.setType(BulletContainer.BulletType.RIFLE_7_62x39mm)
				.setDamage(4f)
				.setMaxAmmo(30);
		CustomItem proximityMine = CustomItemManager.registerItem(new CustomItem("blockProximityMine", "Proximity Mine", Material.SPRUCE_BUTTON))
			.getOrAddProperty(PlaceableProximityMine.class).item;

		//proximity mine recipe
		ItemIngredient plate = new TaggedIngredient(Tag.PRESSURE_PLATES);
		ItemIngredient tnt = new VanillaIngredient(Material.TNT);
		ItemIngredient redstone_dust = new VanillaIngredient(Material.REDSTONE);
		RecipeManager.addCraftingRecipe(new CraftingRecipe(
				new ItemIngredient[][] {
						{plate, plate, plate},
						{tnt, redstone_dust, tnt}
				},
				new ItemOutput(proximityMine.createStack(3))
		), "proximityMine");
	}

	@Override
	public List<ConfigOption<?>> getAllOptions() {
		return ALL_OPTIONS;
	}

}
