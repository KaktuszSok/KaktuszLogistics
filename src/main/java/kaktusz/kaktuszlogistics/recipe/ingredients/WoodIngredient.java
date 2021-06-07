package kaktusz.kaktuszlogistics.recipe.ingredients;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

public class WoodIngredient extends ItemIngredient {

	private static final String[] WOOD_TYPES = new String[] { //not a set, because order matters (due to dark oak)
			"DARK_OAK_",
			"OAK_",
			"SPRUCE_",
			"BIRCH_",
			"JUNGLE_",
			"ACACIA_",
			"CRIMSON_",
			"WARPED_"
	};
	@SuppressWarnings("unused")
	public enum WOOD_ITEM {
			XXX_SIGN,
			XXX_DOOR,
			XXX_WOOD,
			XXX_BOAT,
			XXX_BUTTON,
			XXX_FENCE,
			XXX_FENCE_GATE,
			XXX_LEAVES,
			XXX_LOG,
			XXX_PLANKS,
			XXX_PRESSURE_PLATE,
			XXX_SAPLING,
			XXX_SLAB,
			XXX_STAIRS,
			XXX_TRAPDOOR,
			XXX_WALLSIGN,
			STRIPPED_XXX_LOG,
			STRIPPED_XXX_WOOD
	}

	private final Set<Material> validInputMaterials = new HashSet<>();
	private WOOD_ITEM item;
	private String scannedWoodType = null;

	public WoodIngredient(WOOD_ITEM item, int amount) {
		super(amount);
		this.item = item;
		for (String wood : WOOD_TYPES) {
			Material mat = generateMaterial(item, wood);
			if(mat != null)
				validInputMaterials.add(mat);
		}
	}

	@Override
	protected boolean matchStack(ItemStack stack) {
		scannedWoodType = null;
		if (stack.getAmount() >= amount && validInputMaterials.contains(stack.getType())) {
			String woodType = getWoodType(stack.getType());
			if(scannedWoodType == null) {
				scannedWoodType = woodType;
				return true;
			}
			else
				return scannedWoodType.equals(woodType); //fail if we have conflicting wood types
		}
		return false;
	}

	public String getLastScannedWoodType() {
		return scannedWoodType;
	}

	@Override
	public Set<Material> getValidInputMaterials() {
		return validInputMaterials;
	}

	public static String getWoodType(Material material) {
		String name = material.name();
		for (String wood : WOOD_TYPES) {
			if(name.contains(wood))
				return wood;
		}

		return null;
	}

	public static Material generateMaterial(WOOD_ITEM itemType, String woodType) {
		if(woodType.equals("CRIMSON_") || woodType.equals("WARPED_")) {
			return Material.getMaterial(itemType.name().replace("XXX_", woodType)
					.replace("LOG", "STEM")
					.replace("WOOD", "HYPHAE"));
		}
		return Material.getMaterial(itemType.name().replace("XXX_", woodType));
	}

	@Override
	public String getName() {
		String[] words = item.name().split("_");
		StringJoiner name = new StringJoiner(" ");
		for (String word : words) {
			String capitalisedWord = word.substring(0, 1);
			if(word.length() > 1)
				capitalisedWord += word.substring(1).toLowerCase();
			name.add(capitalisedWord);
		}

		return name.toString();
	}
}
