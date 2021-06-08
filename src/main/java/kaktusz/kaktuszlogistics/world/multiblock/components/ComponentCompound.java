package kaktusz.kaktuszlogistics.world.multiblock.components;

import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.block.Block;

/**
 * Component which matches only if all of its subcomponents match
 */
public class ComponentCompound extends MultiblockComponent {
	private final MultiblockComponent[] subComponents;

	public ComponentCompound(MultiblockComponent... subComponents) {
		this.subComponents = subComponents;
	}

	@Override
	public boolean match(Block block, MultiblockBlock multiblock) {
		for(MultiblockComponent component : subComponents) {
			if(!component.match(block, multiblock)) {
				return false;
			}
		}

		return true;
	}
}
