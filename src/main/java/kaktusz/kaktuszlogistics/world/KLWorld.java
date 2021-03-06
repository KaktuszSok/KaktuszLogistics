package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigManager;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.World;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.blockToChunkCoord;

public class KLWorld {

    /**
     * Represents the position of a chunk in the world
     */
    public static class ChunkCoordinate {
        public final int chunkX;
        public final int chunkZ;

        public ChunkCoordinate(int chunkX, int chunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkCoordinate that = (ChunkCoordinate) o;
            return chunkX == that.chunkX && chunkZ == that.chunkZ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(chunkX, chunkZ);
        }
    }
    public static final int REGION_SIZE = 32; //region size in chunks
    public static final Map<World, KLWorld> loadedWorlds = new HashMap<>();

    public final World world;
    private final Map<ChunkCoordinate, KLChunk> loadedChunks = new ConcurrentHashMap<>();
    private final Queue<Runnable> endOfTickQueue = new LinkedList<>();

    //WORLD
    public KLWorld(World world) {
        this.world = world;

        loadedWorlds.put(world, this);
    }

    /**
     * Gets (or creates) a KLWorld for a vanilla world
     */
    public static KLWorld get(World world) {
        KLWorld klWorld = loadedWorlds.get(world);
        if(klWorld == null) {
            return new KLWorld(world);
        }
        return klWorld;
    }

    public void save() {
        saveAllLoadedChunks();
    }

    /**
     * @return Whether this world was actually loaded in the first place
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean unload() {
        return loadedWorlds.remove(this.world) != null;
    }

    //CHUNK
    /**
     * Finds a KLChunk at some chunk position
     * @return null if chunk could not be founded in loaded chunks
     */
    public KLChunk getLoadedChunkAt(int chunkX, int chunkZ) {
        return loadedChunks.get(new ChunkCoordinate(chunkX, chunkZ));
    }
    /**
     * Finds or loads a KLChunk at some chunk position
     * @return null if chunk could not be founded in loaded chunks or on file
     */
    public KLChunk getChunkAt(int chunkX, int chunkZ) {
        ChunkCoordinate pos = new ChunkCoordinate(chunkX, chunkZ);
        //try get chunk from loaded chunks
        KLChunk chunk = loadedChunks.get(pos);
        if(chunk != null) {
            return chunk;
        }

        //chunk is not loaded - try find in files
        File chunkFile = KLChunk.getFile(this, chunkX, chunkZ);
        if(chunkFile.exists()) {
            chunk = KLChunk.deserialise(chunkFile, this, chunkX, chunkZ);
        }

        return chunk; //null if file doesn't exist
    }
    /**
     * Finds, loads or creates a KLChunk at some chunk position
     */
    public KLChunk getOrCreateChunkAt(int chunkX, int chunkZ) {
        //try find/load chunk
        KLChunk chunk = getChunkAt(chunkX, chunkZ);

        //chunk does not exist yet - create new chunk
        if(chunk == null) {
            chunk = new KLChunk(this, chunkX, chunkZ);
        }

        return chunk;
    }

    public void loadChunk(KLChunk chunk) {
        if(loadedChunks.put(new ChunkCoordinate(chunk.chunkPosX, chunk.chunkPosZ), chunk) == null)
            chunk.onLoaded();
    }

    /**
     * Removes a chunk from loaded chunks.
     * This function is not responsible for saving the chunk. For that, see KLChunk.save()
     * @return True if the chunk was loaded in the first place.
     */
    public boolean unloadChunk(KLChunk chunk) {
        return loadedChunks.remove(new ChunkCoordinate(chunk.chunkPosX, chunk.chunkPosZ), chunk);
    }

    //BLOCK
    /**
     * Find the CustomBlock at some world coordinates, only checking loaded chunks
     * @return null if there is no CustomBlock registered here
     */
    public CustomBlock getLoadedBlockAt(int x, int y, int z) {
        KLChunk chunk = getLoadedChunkAt(VanillaUtils.blockToChunkCoord(x), VanillaUtils.blockToChunkCoord(z));
        if(chunk == null) {
            return null;
        }

        return chunk.getBlockAt(x, y, z);
    }
    /**
     * Find the CustomBlock at some world coordinates
     * @return null if there is no CustomBlock registered here
     */
    public CustomBlock getBlockAt(int x, int y, int z) {
        KLChunk chunk = getChunkAt(VanillaUtils.blockToChunkCoord(x), VanillaUtils.blockToChunkCoord(z));
        if(chunk == null) {
            return null;
        }

        return chunk.getBlockAt(x, y, z);
    }

    /**
     * Gets the multiblock that the block at the given position is a part of, or null
     */
    public MultiblockBlock getMultiblockAt(int x, int y, int z) {
        KLChunk chunk = getLoadedChunkAt(blockToChunkCoord(x), blockToChunkCoord(z));
        if(chunk != null) {
            Set<VanillaUtils.BlockPosition> multiblocks = chunk.getExtraData("multiblocks");
            if(multiblocks != null) {
                for (VanillaUtils.BlockPosition pos : multiblocks) {
                    CustomBlock multiblock = getBlockAt(pos.x, pos.y, pos.z);
                    if(multiblock instanceof MultiblockBlock
                            && ((MultiblockBlock)multiblock).isPosPartOfMultiblock(new VanillaUtils.BlockPosition(x,y,z))) {
                        return (MultiblockBlock)multiblock;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the multiblock at the given position or, if the position is not part of any multiblocks,
     * the custom block at that position (or null)
     */
    public CustomBlock getBlockOrMultiblockAt(int x, int y, int z) {
        CustomBlock cb = getMultiblockAt(x,y,z);
        if(cb != null)
            return cb;
        else
            return getBlockAt(x,y,z);
    }

    /**
     * Set the block at some position to the desired custom block.
     * Does not affect the physical world! The block must be set there appropriately.
     * @return The block that was set
     */
    public CustomBlock setBlock(CustomBlock block, int x, int y, int z) {
        KLChunk chunk = getOrCreateChunkAt(VanillaUtils.blockToChunkCoord(x), VanillaUtils.blockToChunkCoord(z));
        if(block == null) {
            chunk.removeBlock(x, y, z);
            return null;
        }
        return chunk.setBlock(block, x, y, z);
    }

    //EVENT
    public static void onTick() {
        for (KLWorld world : loadedWorlds.values()) {
            world.tick();
        }
        //unload chunks/worlds which are no longer loaded
        if(VanillaUtils.getTickTime() % (20L * ConfigManager.CHUNK_UNLOAD_FREQUENCY.getValue()) == 0) {
            Set<KLWorld> worldsToUnload = new HashSet<>();
            for (KLWorld world : loadedWorlds.values()) {
                Set<KLChunk> chunksToUnload = new HashSet<>();
                for (KLChunk chunk : world.loadedChunks.values()) {
                    if(!world.world.isChunkLoaded(chunk.chunkPosX, chunk.chunkPosZ)) { //unload chunks that aren't loaded in the physical game world
                        chunksToUnload.add(chunk);
                    }
                }
                for (KLChunk chunk : chunksToUnload) {
                    world.save();
                    world.unloadChunk(chunk);
                }
                if(world.loadedChunks.isEmpty()) //unload worlds with no loaded chunks
                    worldsToUnload.add(world);
            }
            for (KLWorld world : worldsToUnload) {
                world.unload();
            }
        }
    }

    protected void tick() {
        for (KLChunk chunk : loadedChunks.values()) {
            chunk.onTick();
        }
        while (!endOfTickQueue.isEmpty()) {
            endOfTickQueue.poll().run();
        }
    }

    public void runAtEndOfTick(Runnable r) {
        endOfTickQueue.add(r);
    }

    //HELPER
    public File getRegionFolderAtChunkPos(int chunkX, int chunkZ) {
        int regX = Math.floorDiv(chunkX, REGION_SIZE);
        int regZ = Math.floorDiv(chunkZ, REGION_SIZE);
        return new File(world.getWorldFolder() + File.separator + "kldata" + File.separator + "region_" + regX + "_" + regZ);
    }

    public void saveAllLoadedChunks() {
        for(KLChunk c : loadedChunks.values()) {
            c.save();
        }
    }

    /**
     * Saves all loaded chunks in all loaded worlds
     */
    public static void saveAllLoadedWorlds() {
        for(KLWorld w : loadedWorlds.values()) {
            w.saveAllLoadedChunks();
        }
    }

}
