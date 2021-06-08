package kaktusz.kaktuszlogistics.world.multiblock.components;

import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.block.Block;

/**
 * Base class for the decorator pattern in multiblock components
 */
public abstract class ComponentDecorator extends MultiblockComponent {

	private final MultiblockComponent component;

	public ComponentDecorator(MultiblockComponent component) {
		this.component = component;
	}

	@Override
	public boolean match(Block block, MultiblockBlock multiblock) {
		if(component.match(block, multiblock)) {
			onMatch(block, multiblock);
			return true;
		}
		return false;
	}

	protected abstract void onMatch(Block block, MultiblockBlock multiblock);
}
