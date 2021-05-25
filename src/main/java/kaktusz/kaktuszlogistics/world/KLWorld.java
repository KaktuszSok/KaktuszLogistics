package kaktusz.kaktuszlogistics.world;

import org.bukkit.World;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KLWorld {

    /**
     * Represents the position of a chunk in the world
     */
    public static class ChunkCoordinate {
        public int chunkX;
        public int chunkZ;

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
    public final Map<ChunkCoordinate, KLChunk> loadedChunks = new HashMap<>();

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
        for(KLChunk c : loadedChunks.values()) {
            c.save();
        }
    }

    /**
     * @return Whether this world was actually loaded in the first place
     */
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

        return chunk;
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
        loadedChunks.put(new ChunkCoordinate(chunk.chunkPosX, chunk.chunkPosZ), chunk);
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
        KLChunk chunk = getLoadedChunkAt(x/KLChunk.CHUNK_SIZE, z/KLChunk.CHUNK_SIZE);
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
        KLChunk chunk = getChunkAt(x/KLChunk.CHUNK_SIZE, z/KLChunk.CHUNK_SIZE);
        if(chunk == null) {
            return null;
        }

        return chunk.getBlockAt(x, y, z);
    }

    public CustomBlock setBlock(CustomBlock block, int x, int y, int z) {
        KLChunk chunk = getOrCreateChunkAt(x/KLChunk.CHUNK_SIZE, z/KLChunk.CHUNK_SIZE);
        if(block == null) {
            chunk.removeBlock(x, y, z);
            return null;
        }
        return chunk.setBlock(block, x, y, z);
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
