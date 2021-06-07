package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import org.bukkit.block.Block;

import java.util.Set;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.*;

/**
 * A decorator which adds to a given set if the component matches
 */
public class DecoratorSet extends ComponentDecorator {

	private final Set<BlockPosition> set;

	/**
	 * @param set The set to populate if we match
	 */
	public DecoratorSet(MultiblockComponent component, Set<BlockPosition> set) {
		super(component);
		this.set = set;
	}

	@Override
	protected void onMatch(Block block, MultiblockBlock multiblock) {
		set.add(new BlockPosition(block.getLocation()));
	}
}
