package kaktusz.kaktuszlogistics.modules.survival.multiblocks.woodworking;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.PolymorphicItem;
import kaktusz.kaktuszlogistics.items.properties.MatrixMultiblock;
import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.world.multiblock.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SawmillWood extends MultiblockBlock {
	public SawmillWood(Multiblock property, Location location, ItemMeta meta) {
		super(property, location, meta);
	}

	@Override
	public ItemStack getDrop(Block block) {
		ItemStack drop = super.getDrop(block);
		drop.setType(block.getType()); //keep wood type
		return drop;
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {
		super.onInteracted(e);

		e.getPlayer().sendMessage("Sawmill Wood");
	}

	/**
	 * This should only be called once, and the result should be registered with the CustomItemManager.
	 */
	public static CustomItem createCustomItem() {
		PolymorphicItem sawmill = new PolymorphicItem("multiblockSawmill", "Sawmill", Material.OAK_FENCE_GATE);
		ComponentCompound barrelStripesAlongAxis = new ComponentCompound(
				new ComponentMaterial(Material.BARREL),
				new ComponentDirectional(Multiblock.RELATIVE_DIRECTION.RIGHT).setAllowOpposite(true)
		);
		sawmill.getOrAddProperty(MatrixMultiblock.class)
				.setLayerModeHorizontal(true)
				.addLayer(new MultiblockComponent[][] {
						{new ComponentMaterial(Material.CRAFTING_TABLE), barrelStripesAlongAxis},
						{new ComponentAgnostic(), barrelStripesAlongAxis}
				})
				.addLayer(new MultiblockComponent[][] {
						{new ComponentTag(Tag.WOODEN_FENCES), ComponentCustomBlock.fromCustomItem(sawmill)},
						{null, null}
				})
				.setControllerBlockOffset(1, 0, 1)
				.setType(SawmillWood.class);

		return sawmill;
	}
}
