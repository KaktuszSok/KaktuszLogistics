package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.recipe.ingredients.FoodIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;

import java.util.TreeMap;

import static kaktusz.kaktuszlogistics.recipe.ingredients.FoodIngredient.FoodCategory;

//TODO config
public class LabourTierRequirements {

	public static final TreeMap<Integer, ItemIngredient[]> requirements = new TreeMap<>();
	static {
		requirements.put(1, new ItemIngredient[] { //Wood:
				new FoodIngredient(FoodCategory.T1, 1)});
		requirements.put(2, new ItemIngredient[] { //Stone:
				new FoodIngredient(FoodCategory.T1, 1)});
		requirements.put(3, new ItemIngredient[] { //Brick:
				new FoodIngredient(FoodCategory.T1, 1),
				new FoodIngredient(FoodCategory.T2, 1)});
		requirements.put(4, new ItemIngredient[] { //Bronze:
				new FoodIngredient(FoodCategory.T2, 1),
				new FoodIngredient(FoodCategory.T3, 1)});
		requirements.put(5, new ItemIngredient[] { //Steel:
				new FoodIngredient(FoodCategory.T2, 2),
				new FoodIngredient(FoodCategory.T3, 1)});
		requirements.put(6, new ItemIngredient[] { //Stainless Steel:
				new FoodIngredient(FoodCategory.T2, 2),
				new FoodIngredient(FoodCategory.T3, 1),
				new FoodIngredient(FoodCategory.DELICACIES, 1)});
		requirements.put(7, new ItemIngredient[] { //Titanium 6-4
				new FoodIngredient(FoodCategory.T2, 1),
				new FoodIngredient(FoodCategory.T3, 2),
				new FoodIngredient(FoodCategory.DELICACIES, 2)
		});
		requirements.put(8, new ItemIngredient[] { //Tungsten Alloy
				new FoodIngredient(FoodCategory.T3, 1),
				new FoodIngredient(FoodCategory.T4, 1),
				new FoodIngredient(FoodCategory.DELICACIES, 2)
		});
		requirements.put(9, new ItemIngredient[] { //Superalloy
				new FoodIngredient(FoodCategory.T3, 1),
				new FoodIngredient(FoodCategory.T4, 1),
				new FoodIngredient(FoodCategory.DELICACIES, 2),
		});
	}
}
