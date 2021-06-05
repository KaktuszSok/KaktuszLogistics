package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.util.SetUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;

public class ComponentMaterial extends MultiblockComponent {
	private final Set<Material> validMaterials;

	public ComponentMaterial(Material... validMaterials) {
		this(SetUtils.setFromElements(validMaterials));
	}
	public ComponentMaterial(Set<Material> validMaterials) {
		this.validMaterials = validMaterials;
	}

	@Override
	public boolean match(Block block, MultiblockBlock multiblock) {
		return validMaterials.contains(block.getBlockData().getMaterial());
	}
}
