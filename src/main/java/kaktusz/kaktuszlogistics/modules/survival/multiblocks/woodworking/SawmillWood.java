package kaktusz.kaktuszlogistics.modules.survival.multiblocks.woodworking;

import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SawmillWood extends MultiblockBlock {
	public SawmillWood(Multiblock property, ItemMeta meta) {
		super(property, meta);
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
}
