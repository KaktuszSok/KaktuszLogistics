package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.util.CastingUtils;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.*;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;
import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.chunkToBlockCoord;

@SuppressWarnings("UnusedReturnValue")
public final class KLChunk {
    /**
     * A local coordinate in the chunk
     */
    public static class LocalCoordinate {
        public byte x;
        public short y;
        public byte z;

        public LocalCoordinate() {

        }
        public LocalCoordinate(int worldX, int worldY, int worldZ) {
            x = (byte)(worldX & 0b1111);
            y = (short)worldY;
            z = (byte)(worldZ & 0b1111);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocalCoordinate that = (LocalCoordinate) o;
            return this.x == that.x && this.y == that.y && this.z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + "," + z + ")";
        }
    }

    public transient final KLWorld world;
    public transient final int chunkPosX;
    public transient final int chunkPosZ;
    private final Map<LocalCoordinate, CustomBlock> blocks = new HashMap<>();
    private transient final Set<TickingBlock> tickingBlocks = new HashSet<>();

    private Map<String, Serializable> extraData = new HashMap<>();

    public KLChunk(KLWorld world, int chunkX, int chunkZ) {
        this.world = world;
        chunkPosX = chunkX;
        chunkPosZ = chunkZ;

        world.loadChunk(this);
    }

    //WORLD INTERACTION

    /**
     * Set the block at some position to the desired custom block.
     * Does not affect the physical world! The block must be set there appropriately.
     * @return The block that was set
     */
    public CustomBlock setBlock(CustomBlock block, int x, int y, int z) {
        blocks.put(new LocalCoordinate(x, y, z), block);
        if(block instanceof TickingBlock)
            addTickingBlock((TickingBlock)block);
        block.onSet(world, x,y,z);
        return block;
    }

    /**
     * Removes a custom block from the chunk
     * @return Whether there was a block at this position
     */
    public boolean removeBlock(int x, int y, int z) {
        CustomBlock removed = blocks.remove(new LocalCoordinate(x, y, z));
        if(removed != null)
            removed.onRemoved(world, x, y, z);
        if(removed instanceof TickingBlock)
            removeTickingBlock((TickingBlock)removed);
        return removed != null;
    }

    public CustomBlock getBlockAt(int x, int y, int z) {
        LocalCoordinate pos = new LocalCoordinate(x, y, z);
        return blocks.get(pos);
    }

    public void addTickingBlock(TickingBlock tickingBlock) {
        tickingBlocks.add(tickingBlock);
        tickingBlock.onLoaded();
    }
    private void removeTickingBlock(TickingBlock tickingBlock) {
        tickingBlocks.remove(tickingBlock);
    }

    /**
     * Allows saving arbitrary data with this chunk
     */
    public void setExtraData(String key, Serializable value) {
        if(value == null)
            extraData.remove(key);
        else
            extraData.put(key, value);
    }

    /**
     * Allows loading arbitrary data which was previously saved to this chunk using setExtraData
     * @return The saved data for this key, or null if data for this key does not exist
     */
    public <T extends Serializable> T getExtraData(String key) {
        return CastingUtils.confidentCast(extraData.get(key));
    }

    /**
     * Retrieves or creates a Set of arbitrary type from the chunk's extra data. Will error if the key already exists but the entry can not be cast to a set of this type.
     * @param <T> The type of elements in the set
     * @param key The key where the set is/should be stored
     * @return The existing set at this key or a new empty set that has been added to the chunk's extra data at the given key.
     */
    public <T extends Serializable> Set<T> getOrCreateExtraDataSet(String key) {
        if(extraData.containsKey(key))
            return CastingUtils.confidentCast(extraData.get(key));

        HashSet<T> set = new HashSet<>();
        setExtraData(key, set);
        return set;
    }

    /**
     * Removes a given object from a Set in extra data at the given key.
     * If the key has no data associated with it, nothing happens.
     * If the set becomes empty, it is removed from extra data.
     * @param key The key in extra data where the set is located. This key must not have a value of the wrong type associated with it!
     * @param objToRemove The object to be removed from the set
     * @return True if the set has been removed. Always false if the key does not have a value associated with it.
     */
    public boolean removeFromExtraDataSet(String key, Serializable objToRemove) {
        if(!extraData.containsKey(key))
            return false;

        Set<Serializable> set = getExtraData(key);
        set.remove(objToRemove);
        if(set.isEmpty()) {
            extraData.remove(key); //remove set from extra data if it is empty
            return true;
        }
        return false;
    }

    /**
     * Unloads the chunk in its world.
     * This function is not responsible for saving the chunk. For that, see save()
     * @return True if the chunk was loaded in the first place
     */
    public boolean unload() {
        return world.unloadChunk(this);
    }

    //EVENTS
    /**
     * Called if the KLChunk is loaded because its respective vanilla chunk was preloaded (relative to the plugin instance)
     * e.g. spawn chunks
     */
    @SuppressWarnings("unused")
    public void onChunkPreloaded(Chunk c) {

    }

    public void onChunkLoaded(ChunkLoadEvent e) {
        if(e.isNewChunk()) { //newly generated chunk - destroy any data that may have been put here before
            unload();
            //delete chunk data:
            File file = getFile(world, chunkPosX, chunkPosZ);
            if(file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

    /**
     * Called when this KL Chunk is loaded in
     */
    @SuppressWarnings("EmptyMethod")
    public void onLoaded() {

    }

    public void onChunkUnloaded(ChunkUnloadEvent e) {
        if(e.isSaveChunk()) {
            save();
        }

        unload();
    }

    protected void onTick() {
        for (TickingBlock tickingBlock : tickingBlocks)
            tickingBlock.onTick();
    }

    //HELPER
    public static File getFile(KLWorld world, int chunkX, int chunkZ) {
        File regionFolder = world.getRegionFolderAtChunkPos(chunkX, chunkZ);
        return new File(regionFolder + File.separator + getFileName(chunkX, chunkZ));
    }

    public static String getFileName(int chunkX, int chunkZ) {
        return "chunk_" + chunkX + "_" + chunkZ + ".klc";
    }

    //SAVE/LOAD (if file size turns out to be an issue, should try add compression step)
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean save() {
        tickingBlocks.forEach(TickingBlock::onSave);
        try {
            File chunkSaveFile = getFile(world, chunkPosX, chunkPosZ);
            chunkSaveFile.getParentFile().mkdirs();

            FileOutputStream fileOut = new FileOutputStream(chunkSaveFile);
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(fileOut);

            //write block hashmap size
            out.writeInt(blocks.size()); //int
            //write block hashmap contents
            for (Map.Entry<LocalCoordinate, CustomBlock> entry : blocks.entrySet()) {
                //write location
                LocalCoordinate coord = entry.getKey();
                out.writeByte(coord.x); //byte
                out.writeShort(coord.y); //short
                out.writeByte(coord.z); //byte
                //write block data
                out.writeObject(entry.getValue()); //CustomBlock
            }

            //write extra data
            out.writeObject(extraData);

            out.close();
            return true;
        }
        catch (Exception e) {
            KaktuszLogistics.LOGGER.warning("FAILED TO SAVE CHUNK (" + chunkPosX + "," + chunkPosZ +"): " + e);
            return false;
        }
    }

    public static KLChunk deserialise(File file, KLWorld world, int chunkX, int chunkZ) {

        KLChunk result = new KLChunk(world, chunkX, chunkZ);

        try {
            FileInputStream fileIn = new FileInputStream(file);
            BukkitObjectInputStream in = new BukkitObjectInputStream(fileIn);

            //read hashmap size
            int blocksSize = in.readInt(); //int
            for(int i = 0; i < blocksSize; i++) {
                //read location
                LocalCoordinate coord = new LocalCoordinate();
                coord.x = in.readByte(); //byte
                coord.y = in.readShort(); //short
                coord.z = in.readByte(); //byte
                try {
                    //read block data and figure out world coords
                    CustomBlock block = (CustomBlock)in.readObject(); //CustomBlock
                    block.setLocation(new Location(world.world, coord.x + chunkToBlockCoord(chunkX), coord.y, coord.z + chunkToBlockCoord(chunkZ)));
                    result.blocks.put(coord, block);
                    if (block instanceof TickingBlock) {
                        result.addTickingBlock((TickingBlock) block);
                    }
                } catch (Exception e) {
                    BlockPosition fullCoord = new BlockPosition(VanillaUtils.chunkToBlockCoord(coord.x), coord.y, VanillaUtils.chunkToBlockCoord(coord.z));
                    KaktuszLogistics.LOGGER.warning("Couldn't load block at " + fullCoord.toString() + " in chunk " + chunkX + "," + chunkZ + ":");
                    e.printStackTrace();
                }
            }

            //read extra data
            try {
                Object obj = in.readObject();
                result.extraData = CastingUtils.confidentCast(obj); //if the cast fails then the data is corrupted
            } catch (Exception e) {
                KaktuszLogistics.LOGGER.warning("Couldn't read extra data for chunk " + chunkX + "," + chunkZ + ":");
                e.printStackTrace();
            }

            in.close();
        }
        catch (Exception e) {
            KaktuszLogistics.LOGGER.warning("FAILED TO LOAD CHUNK (" + chunkX + "," + chunkZ + "):");
            e.printStackTrace();
        }

        return result;
    }
}
