package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.util.CastingUtils;
import org.bukkit.Chunk;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.*;

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
    public CustomBlock setBlock(CustomBlock block, int x, int y, int z) {
        blocks.put(new LocalCoordinate(x, y, z), block);
        if(block instanceof TickingBlock)
            tickingBlocks.add((TickingBlock)block);
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
            tickingBlocks.remove(removed);
        return removed != null;
    }

    public CustomBlock getBlockAt(int x, int y, int z) {
        LocalCoordinate pos = new LocalCoordinate(x, y, z);
        return blocks.get(pos);
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
    public Serializable getExtraData(String key) {
        return extraData.get(key);
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
        onLoaded();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onChunkLoaded(ChunkLoadEvent e) {
        if(e.isNewChunk()) { //newly generated chunk - destroy any data that may have been put here before
            unload();
            //delete chunk data:
            File file = getFile(world, chunkPosX, chunkPosZ);
            if(file.exists()) {
                file.delete();
            }
        }
        onLoaded();
    }

    public void onLoaded() {

    }

    public void onChunkUnloaded(ChunkUnloadEvent e) {
        if(e.isSaveChunk()) {
            save();
        }

        unload();
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
                out.writeObject(entry.getValue().data); //ItemMeta
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
                //read block data
                ItemMeta blockData = (ItemMeta)in.readObject(); //ItemMeta
                CustomBlock block = CustomBlock.createFromMeta(blockData);
                if(block == null) {
                    String typeStr = blockData.getPersistentDataContainer().get(CustomItem.TYPE_KEY, PersistentDataType.STRING);
                    KaktuszLogistics.LOGGER.warning("Read invalid item type when loading chunk " + chunkX + "," + chunkZ + ": " + typeStr);
                }
                else {
                    result.blocks.put(coord, block);
                    if(block instanceof TickingBlock)
                        result.tickingBlocks.add((TickingBlock)block);
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
