package kaktusz.kaktuszlogistics.modules.nations.world;

import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.world.CustomBlock;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collection;

public class FlagBlock extends CustomBlock {
	public FlagBlock(ItemPlaceable prop, ItemMeta meta) {
		super(prop, meta);
	}

	@Override
	public ItemStack getDrop(Block block) {
		ItemStack drop = super.getDrop(block);
		Collection<ItemStack> blockItems = block.getDrops();
		if(blockItems.iterator().hasNext())
			drop.setType(blockItems.iterator().next().getType()); //set to correct banner colour
		return drop;
	}
}
