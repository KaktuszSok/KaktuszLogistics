package kaktusz.kaktuszlogistics.world.multiblock.components;

import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.block.Block;

/**
 * A decorator which marks the block as special if the component matches
 */
public class DecoratorSpecialBlock extends ComponentDecorator {

	public enum SpecialType {
		ITEM_INPUT,
		ITEM_OUTPUT
	}
	private final SpecialType specialType;

	/**
	 * @param specialType The set to populate if we match
	 */
	public DecoratorSpecialBlock(MultiblockComponent component, SpecialType specialType) {
		super(component);
		this.specialType = specialType;
	}

	@Override
	protected void onMatch(Block block, MultiblockBlock multiblock) {
		multiblock.markBlockSpecial(new VanillaUtils.BlockPosition(block.getLocation()), specialType);
	}
}
