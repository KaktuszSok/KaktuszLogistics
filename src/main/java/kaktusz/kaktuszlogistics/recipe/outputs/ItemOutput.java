package kaktusz.kaktuszlogistics.recipe.outputs;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.multiblock.components.DecoratorSpecialBlock;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

public class ItemOutput implements IRecipeOutput {

	protected final ItemStack itemStack;

	public ItemOutput(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public ItemStack getStack() {
		return itemStack.clone();
	}

	@Override
	public String getName() {
		if(itemStack.getItemMeta() != null) {
			if (itemStack.getItemMeta().hasDisplayName()) //use the display name
				return itemStack.getItemMeta().getDisplayName() + " x" + itemStack.getAmount();

			//otherwise, use the localised name
			return itemStack.getItemMeta().getLocalizedName() + " x" + itemStack.getAmount();
		}
		//otherwise, it is air, which is not a valid output
		KaktuszLogistics.LOGGER.warning("Invalid output: " + itemStack.toString());
		return "[INVALID - PLEASE REPORT TO DEVELOPER]";
	}

	@Override
	public void placeInWorld(World world, VanillaUtils.BlockPosition position) {
		BlockState state = world.getBlockAt(position.x, position.y, position.z).getState();
		if(state instanceof Container) {
			VanillaUtils.addItemsOrDrop(((Container)state).getInventory(), getStack());
		}
	}

	@Override
	public DecoratorSpecialBlock.SpecialType getOutputBlockType() {
		return DecoratorSpecialBlock.SpecialType.ITEM_OUTPUT;
	}

}
