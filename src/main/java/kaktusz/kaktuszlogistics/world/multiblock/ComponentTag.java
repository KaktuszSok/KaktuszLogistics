package kaktusz.kaktuszlogistics.world.multiblock;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;

public class ComponentTag extends MultiblockComponent {

	private final Tag<Material> tag;

	public ComponentTag(Tag<Material> tag) {
		this.tag = tag;
	}

	@Override
	public boolean match(Block block, MultiblockBlock multiblock) {
		return tag.isTagged(block.getBlockData().getMaterial());
	}
}
