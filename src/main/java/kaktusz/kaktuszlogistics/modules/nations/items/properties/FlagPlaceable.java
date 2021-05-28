package kaktusz.kaktuszlogistics.modules.nations.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.modules.nations.world.FlagBlock;
import kaktusz.kaktuszlogistics.util.SetUtils;
import kaktusz.kaktuszlogistics.world.CustomBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Set;

public class FlagPlaceable extends ItemPlaceable {

	private static final Set<Material> BANNER_TYPES = SetUtils.setFromElements(
			Material.WHITE_BANNER,
			Material.ORANGE_BANNER,
			Material.MAGENTA_BANNER,
			Material.LIGHT_BLUE_BANNER,
			Material.YELLOW_BANNER,
			Material.LIME_BANNER,
			Material.PINK_BANNER,
			Material.GRAY_BANNER,
			Material.LIGHT_GRAY_BANNER,
			Material.CYAN_BANNER,
			Material.PURPLE_BANNER,
			Material.BLUE_BANNER,
			Material.BROWN_BANNER,
			Material.RED_BANNER,
			Material.BLACK_BANNER,

			Material.WHITE_WALL_BANNER,
			Material.ORANGE_WALL_BANNER,
			Material.MAGENTA_WALL_BANNER,
			Material.LIGHT_BLUE_WALL_BANNER,
			Material.YELLOW_WALL_BANNER,
			Material.LIME_WALL_BANNER,
			Material.PINK_WALL_BANNER,
			Material.GRAY_WALL_BANNER,
			Material.LIGHT_GRAY_WALL_BANNER,
			Material.CYAN_WALL_BANNER,
			Material.PURPLE_WALL_BANNER,
			Material.BLUE_WALL_BANNER,
			Material.BROWN_WALL_BANNER,
			Material.RED_WALL_BANNER,
			Material.BLACK_WALL_BANNER
	);

	public FlagPlaceable(CustomItem item) {
		super(item);
	}

	@Override
	public boolean verify(Block block) {
		return BANNER_TYPES.contains(block.getType());
	}

	@Override
	public CustomBlock createCustomBlock(ItemMeta stackMeta) {
		return new FlagBlock(this, stackMeta);
	}
}
