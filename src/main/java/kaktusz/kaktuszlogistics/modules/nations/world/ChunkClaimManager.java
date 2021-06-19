package kaktusz.kaktuszlogistics.modules.nations.world;

import kaktusz.kaktuszlogistics.modules.nations.items.properties.FlagPlaceable;
import kaktusz.kaktuszlogistics.util.CastingUtils;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.KLChunk;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class ChunkClaimManager {

	/**
	 * Claims a chunk
	 * @param flagPosition Coordinates of the claiming flag
	 */
	public static void claimChunkAt(KLWorld world, int chunkX, int chunkZ, VanillaUtils.BlockPosition flagPosition) {
		KLChunk klChunk = world.getOrCreateChunkAt(chunkX, chunkZ);
		Set<VanillaUtils.BlockPosition> flagsClaimingThisChunk = klChunk.getOrCreateExtraDataSet("claimedByFlags");
		flagsClaimingThisChunk.add(flagPosition);
	}

	/**
	 * Unclaims a chunk
	 * @param flagPosition Coordinates of the flag which no longer claims this chunk
	 */
	public static void unclaimChunkAt(KLWorld world, int chunkX, int chunkZ, VanillaUtils.BlockPosition flagPosition) {
		KLChunk klChunk = world.getChunkAt(chunkX, chunkZ);
		if(klChunk == null)
			return;

		klChunk.removeFromExtraDataSet("claimedByFlags", flagPosition);
	}

	public static boolean isChunkClaimed(KLWorld world, int blockX, int blockZ) {
		return isChunkClaimed(world.getChunkAt(VanillaUtils.blockToChunkCoord(blockX), VanillaUtils.blockToChunkCoord(blockZ)));
	}
	public static boolean isChunkClaimed(KLChunk chunk) {
		if(chunk == null)
			return false;

		HashSet<VanillaUtils.BlockPosition> flagsClaimingThisChunk = CastingUtils.confidentCast(chunk.getExtraData("claimedByFlags"));
		if(flagsClaimingThisChunk == null)
			return false;

		for (VanillaUtils.BlockPosition flagPos : flagsClaimingThisChunk) {
			if(FlagPlaceable.isBanner(chunk.world.world.getBlockAt(flagPos.x, flagPos.y, flagPos.z).getType())) //verify that the block was not corrupted
				return true;
		}
		return false;
	}
}
