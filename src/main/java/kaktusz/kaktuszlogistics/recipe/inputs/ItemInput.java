package kaktusz.kaktuszlogistics.recipe.inputs;

import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemInput implements IRecipeInput {

	public final ItemStack stack;

	public ItemInput(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public boolean isNull() {
		return stack == null || stack.getAmount() == 0 || stack.getType() == Material.AIR;
	}

	public static ItemInput[] fromStackArray(ItemStack[] items) {
		ItemInput[] result = new ItemInput[items.length];
		for(int i = 0; i < items.length; i++) {
			result[i] = new ItemInput(items[i]);
		}
		return result;
	}

	@Override
	public String toString() {
		if(stack == null)
			return "null";
		return stack.getType().name();
	}

	@Override
	public Map<String, Object> serialize() {
		return stack.serialize();
	}

	@Override
	public IRecipeInput clone() {
		return new ItemInput(stack.clone());
	}

	@Override
	public IRecipeInput clone(int newAmount) {
		ItemStack cloneStack = stack.clone();
		cloneStack.setAmount(newAmount);
		return new ItemInput(cloneStack);
	}

	@Override
	public void reduce(int amount) {
		stack.setAmount(stack.getAmount()-amount);
	}

	public static Stream<ItemInput> getInputsFromPosition(World world, VanillaUtils.BlockPosition position) {
		Block block = world.getBlockAt(position.x, position.y, position.z);
		BlockState state = block.getState();
		if(state instanceof Container) {
			return Arrays.stream(((Container) state).getInventory().getStorageContents())
					.map(ItemInput::new);
		}
		return null;
	}
}
