package kaktusz.kaktuszlogistics.recipe.outputs;

import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.multiblock.components.DecoratorSpecialBlock;
import org.bukkit.World;

public interface IRecipeOutput {
	String getName();
	void placeInWorld(World world, VanillaUtils.BlockPosition position);
	/**
	 * @return The blocks this output should be outputted into
	 */
	DecoratorSpecialBlock.SpecialType getOutputBlockType();
}
