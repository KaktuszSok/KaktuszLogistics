package kaktusz.kaktuszlogistics.modules.nations.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.multiblock.SupportedBlockProperty;
import kaktusz.kaktuszlogistics.modules.nations.world.ChunkClaimManager;
import kaktusz.kaktuszlogistics.modules.nations.world.FlagBlock;
import kaktusz.kaktuszlogistics.util.SetUtils;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

public class FlagPlaceable extends SupportedBlockProperty {

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

	//PLACEMENT
	@Override
	public void onTryPlace(BlockPlaceEvent e, ItemStack stack) {
		//TODO: disallow if the flag does not have a nation set (right-clicking the flag will give nation creation GUI)
		Block b = e.getBlockPlaced();
		if(ChunkClaimManager.isChunkClaimed(KLWorld.get(b.getWorld()), b.getX(), b.getZ())) { //disallow placing flags on already claimed land
			e.setCancelled(true);
			return;
		}
		super.onTryPlace(e, stack);
	}

	//UTILITY
	@Override
	public boolean verify(Block block) {
		return isBanner(block.getType());
	}

	/**
	 * @return True if the material is a banner
	 */
	public static boolean isBanner(Material material) {
		return BANNER_TYPES.contains(material);
	}

	@Override
	public FlagBlock createCustomBlock(ItemMeta stackMeta, Location location) {
		return new FlagBlock(this, location, stackMeta);
	}
}
