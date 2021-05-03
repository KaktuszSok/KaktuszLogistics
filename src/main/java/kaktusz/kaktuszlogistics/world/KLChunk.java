package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("UnusedReturnValue")
public class KLChunk {
    /**
     * A local coordinate in the chunk
     */
    public static class LocCoordinate {
        public byte x;
        public short y;
        public byte z;

        public LocCoordinate() {

        }
        public LocCoordinate(int worldX, int worldY, int worldZ) {
            x = (byte)(worldX % CHUNK_SIZE);
            y = (short)worldY;
            z = (byte)(worldZ % CHUNK_SIZE);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocCoordinate that = (LocCoordinate) o;
            return x == that.x && y == that.y && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    public static transient final short CHUNK_SIZE = 16;

    public transient final KLWorld world;
    public transient final int chunkPosX;
    public transient final int chunkPosZ;
    protected Map<LocCoordinate, CustomBlock> blocks = new HashMap<>();

    public KLChunk(KLWorld world, int chunkX, int chunkZ) {
        this.world = world;
        chunkPosX = chunkX;
        chunkPosZ = chunkZ;

        world.loadChunk(this);
    }

    public CustomBlock setBlock(CustomBlock block, int x, int y, int z) {
        blocks.put(new LocCoordinate(x, y, z), block);
        return block;
    }

    /**
     * Removes a custom block from the chunk
     * @return Whether there was a block at this position
     */
    public boolean removeBlock(int x, int y, int z) {
        return blocks.remove(new LocCoordinate(x, y, z)) != null;
    }

    public CustomBlock getBlockAt(int x, int y, int z) {
        LocCoordinate pos = new LocCoordinate(x, y, z);
        return blocks.get(pos);
    }

    public boolean unload() {
        return world.unloadChunk(this);
    }

    //EVENTS

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

            //write hashmap size
            out.writeInt(blocks.size()); //int
            for (Map.Entry<LocCoordinate, CustomBlock> entry : blocks.entrySet()) {
                //write location
                LocCoordinate coord = entry.getKey();
                out.writeByte(coord.x); //byte
                out.writeShort(coord.y); //short
                out.writeByte(coord.z); //byte
                //write block data
                out.writeObject(entry.getValue().data); //ItemMeta
            }

            out.close();
            return true;
        }
        catch (Exception e) {
            KaktuszLogistics.LOGGER.warning("FAILED TO SAVE CHUNK (" + chunkPosX + "," + chunkPosZ +"):" + e);
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
                LocCoordinate coord = new LocCoordinate();
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
                }
            }

            in.close();
        }
        catch (Exception e) {
            KaktuszLogistics.LOGGER.warning("FAILED TO LOAD CHUNK (" + chunkX + "," + chunkZ + "):" + e);
        }

        return result;
    }
}
