package kaktusz.kaktuszlogistics.world.multiblock;

import org.bukkit.block.Block;

/**
 * Matches if any of the subcomponents match
 */
public class ComponentChoice extends MultiblockComponent {
	private final MultiblockComponent[] subComponents;

	public ComponentChoice(MultiblockComponent... subComponents) {
		this.subComponents = subComponents;
	}

	@Override
	public boolean match(Block block, MultiblockBlock multiblock) {
		for(MultiblockComponent component : subComponents) {
			if(component.match(block, multiblock)) {
				return true;
			}
		}

		return false;
	}
}
