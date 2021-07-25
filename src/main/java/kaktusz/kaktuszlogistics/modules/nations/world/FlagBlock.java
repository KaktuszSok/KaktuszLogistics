package kaktusz.kaktuszlogistics.modules.nations.world;

import kaktusz.kaktuszlogistics.modules.nations.KaktuszNations;
import kaktusz.kaktuszlogistics.modules.nations.items.properties.FlagPlaceable;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.KLWorld;
import kaktusz.kaktuszlogistics.world.multiblock.CustomSupportedBlock;
import org.bukkit.Location;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;

public class FlagBlock extends CustomSupportedBlock {
	private static final long serialVersionUID = 100L;

	public FlagBlock(FlagPlaceable prop, Location location, ItemMeta meta) {
		super(prop, location, meta);
	}

	@Override
	public BlockFace getSupportedFace() {
		Block selfBlock = getLocation().getBlock();
		BlockState state = selfBlock.getState();
		if(state instanceof Banner) {
			BlockData data = selfBlock.getBlockData();
			if(data instanceof Directional) { //wall banner
				return ((Directional)data).getFacing().getOppositeFace();
			}
			else return BlockFace.DOWN; //standing banner
		}
		//else, bad data
		update();
		return BlockFace.SELF;
	}

	@Override
	public void onSet(KLWorld world, int x, int y, int z) {
		super.onSet(world, x, y, z);
		VanillaUtils.BlockPosition pos = new VanillaUtils.BlockPosition(x,y,z);

		for(int dx = -KaktuszNations.CLAIM_DISTANCE.getValue(); dx <= KaktuszNations.CLAIM_DISTANCE.getValue(); dx++) {
			for(int dz = -KaktuszNations.CLAIM_DISTANCE.getValue(); dz <= KaktuszNations.CLAIM_DISTANCE.getValue(); dz++) {
				ChunkClaimManager.claimChunkAt(world, dx + VanillaUtils.blockToChunkCoord(x), dz + VanillaUtils.blockToChunkCoord(z), pos);
			}
		}
	}

	@Override
	public void onRemoved(KLWorld world, int x, int y, int z) {
		super.onRemoved(world, x, y, z);
		VanillaUtils.BlockPosition pos = new VanillaUtils.BlockPosition(x,(short)y,z);

		for(int dx = -KaktuszNations.CLAIM_DISTANCE.getValue(); dx <= KaktuszNations.CLAIM_DISTANCE.getValue(); dx++) {
			for(int dz = -KaktuszNations.CLAIM_DISTANCE.getValue(); dz <= KaktuszNations.CLAIM_DISTANCE.getValue(); dz++) {
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
