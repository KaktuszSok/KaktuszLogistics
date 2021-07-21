package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.gui.CustomGUI;
import kaktusz.kaktuszlogistics.recipe.ingredients.FoodIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static kaktusz.kaktuszlogistics.recipe.ingredients.FoodIngredient.FoodCategory;

//TODO config
public class LabourTierRequirements {

	public static final TreeMap<Integer, ItemIngredient[]> REQUIREMENTS = new TreeMap<>();
	static {
		REQUIREMENTS.put(1, new ItemIngredient[] { //Wood:
				new FoodIngredient(FoodCategory.T1, 1)
		});
		REQUIREMENTS.put(2, new ItemIngredient[] { //Stone:
				new FoodIngredient(FoodCategory.T1, 1)
		});
		REQUIREMENTS.put(3, new ItemIngredient[] { //Brick:
				new FoodIngredient(FoodCategory.T1, 1),
				new FoodIngredient(FoodCategory.T2, 1)
		});
		REQUIREMENTS.put(4, new ItemIngredient[] { //Bronze:
				new FoodIngredient(FoodCategory.T2, 1),
				new FoodIngredient(FoodCategory.T3, 1)
		});
		REQUIREMENTS.put(5, new ItemIngredient[] { //Steel:
				new FoodIngredient(FoodCategory.T2, 2),
				new FoodIngredient(FoodCategory.T3, 1)
		});
		REQUIREMENTS.put(6, new ItemIngredient[] { //Stainless Steel:
				new FoodIngredient(FoodCategory.T2, 2),
				new FoodIngredient(FoodCategory.T3, 1),
				new FoodIngredient(FoodCategory.DELICACIES, 1)
		});
		REQUIREMENTS.put(7, new ItemIngredient[] { //Titanium 6-4
				new FoodIngredient(FoodCategory.T2, 1),
				new FoodIngredient(FoodCategory.T3, 2),
				new FoodIngredient(FoodCategory.DELICACIES, 2)
		});
		REQUIREMENTS.put(8, new ItemIngredient[] { //Tungsten Alloy
				new FoodIngredient(FoodCategory.T3, 1),
				new FoodIngredient(FoodCategory.T4, 1),
				new FoodIngredient(FoodCategory.DELICACIES, 2)
		});
		REQUIREMENTS.put(9, new ItemIngredient[] { //Superalloy
				new FoodIngredient(FoodCategory.T3, 1),
				new FoodIngredient(FoodCategory.T4, 1),
				new FoodIngredient(FoodCategory.DELICACIES, 2),
		});
	}

	public static final CustomGUI REQUIREMENTS_GUI = new CustomGUI(
			(int)Math.ceil(REQUIREMENTS.lastKey()/9f)*9, "House Tiers") {
		@Override
		protected void clearInventory() {
			super.clearInventory();

			int lastKey = REQUIREMENTS.lastKey();
			for(int tier = 1; tier <= lastKey; tier++) {
				ItemStack infoItem = new ItemStack(Material.OAK_SIGN, tier);
				setName(infoItem, ChatColor.BOLD + "Tier " + tier + ChatColor.RESET + " House");
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.GRAY + "Requirements (per person):");
				lore.add(ChatColor.GRAY + " - " + Math.ceil(6*Math.pow(1.4d, tier-1)) + "m^2 " + ChatColor.BLUE + "Floor Area");
				ItemIngredient[] itemReqs = REQUIREMENTS.get(tier);
				for(ItemIngredient req : itemReqs) {
					lore.add(ChatColor.GRAY + " - " + ChatColor.BLUE + req.getName() + " x" + req.amount);
				}
				setLore(infoItem, lore);
				inventory.addItem(infoItem);
			}
		}
	};
}
