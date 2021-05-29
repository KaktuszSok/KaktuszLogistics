package kaktusz.kaktuszlogistics.modules.nations.world;

import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.modules.nations.KaktuszNations;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.CustomBlock;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;

public class FlagBlock extends CustomBlock {
	public FlagBlock(ItemPlaceable prop, ItemMeta meta) {
		super(prop, meta);
	}

	@Override
	public void onSet(KLWorld world, int x, int y, int z) {
		VanillaUtils.BlockPosition pos = new VanillaUtils.BlockPosition(x,(short)y,z);

		for(int dx = -KaktuszNations.CLAIM_DISTANCE; dx <= KaktuszNations.CLAIM_DISTANCE; dx++) {
			for(int dz = -KaktuszNations.CLAIM_DISTANCE; dz <= KaktuszNations.CLAIM_DISTANCE; dz++) {
				ChunkClaimManager.claimChunkAt(world, dx + VanillaUtils.blockToChunkCoord(x), dz + VanillaUtils.blockToChunkCoord(z), pos);
			}
		}
	}

	@Override
	public void onRemoved(KLWorld world, int x, int y, int z) {
		VanillaUtils.BlockPosition pos = new VanillaUtils.BlockPosition(x,(short)y,z);

		for(int dx = -KaktuszNations.CLAIM_DISTANCE; dx <= KaktuszNations.CLAIM_DISTANCE; dx++) {
			for(int dz = -KaktuszNations.CLAIM_DISTANCE; dz <= KaktuszNations.CLAIM_DISTANCE; dz++) {
				ChunkClaimManager.unclaimChunkAt(world, dx + VanillaUtils.blockToChunkCoord(x), dz + VanillaUtils.blockToChunkCoord(z), pos);
			}
		}
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