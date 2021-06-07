package kaktusz.kaktuszlogistics.recipe.outputs;

import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.World;

public interface IRecipeOutput {
	String getName();
	void placeInWorld(World world, VanillaUtils.BlockPosition position);
	/**
	 * @return The class which defines which blocks this output should be outputted into
	 */
	Class<? extends IRecipeOutput> getOutputBlockType();
}
