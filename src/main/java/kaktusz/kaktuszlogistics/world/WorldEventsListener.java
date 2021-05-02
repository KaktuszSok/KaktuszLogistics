package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.util.VanillaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class WorldEventsListener implements Listener {

    //WORLD
    public void loadPreloadedChunks() {
        for(World w : Bukkit.getWorlds()) {
            for (Chunk c : w.getLoadedChunks()) {
                KLChunk chunk = KLWorld.get(w).getChunkAt(c.getX(), c.getZ()); //returns null for chunks that never had KL data written to them
                if (chunk != null)
                    chunk.onChunkPreloaded(c);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldSave(WorldSaveEvent e) {
        KLWorld world = KLWorld.loadedWorlds.get(e.getWorld());
        if(world == null)
            return;

        world.save();
    }

    //CHUNK
    @EventHandler(ignoreCancelled = true)
    public void onChunkLoaded(ChunkLoadEvent e) {
        KLWorld world = KLWorld.get(e.getWorld());
        Chunk c = e.getChunk();
        KLChunk chunk = world.getChunkAt(c.getX(), c.getZ()); //returns null for chunks that never had KL data written to them
        if(chunk != null)
            chunk.onChunkLoaded(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkUnloaded(ChunkUnloadEvent e) {
        KLWorld world = KLWorld.get(e.getWorld());
        Chunk c = e.getChunk();
        KLChunk chunk = world.getChunkAt(c.getX(), c.getZ());

        if(chunk != null)
            chunk.onChunkUnloaded(e);
    }

    //BLOCK
    public static CustomBlock getCustomBlockFromEvent(BlockEvent e) {
        Block b = e.getBlock();
        return KLWorld.get(b.getWorld()).getLoadedBlockAt(b.getX(), b.getY(), b.getZ());
    }
    public static CustomBlock getCustomBlockFromLocation(Location loc) {
        return KLWorld.get(loc.getWorld()).getLoadedBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    public static void cancelCustomBlockEvent(CustomBlock block, Cancellable e) {
        if(block == null) return;
        e.setCancelled(true);
    }
    public static void cancelEventIfCBlockInList(List<Block> blocks, Cancellable e) {
        for(Block b : blocks) {
            if(KLWorld.get(b.getWorld()).getLoadedBlockAt(b.getX(), b.getY(), b.getZ()) != null) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockMined(BlockBreakEvent e) {
        CustomBlock block = getCustomBlockFromEvent(e);
        if(block == null) return;

        Block b = e.getBlock();
        KLWorld world = KLWorld.get(b.getWorld());
        if(!block.update(world, b.getX(), b.getY(), b.getZ())) { //something went wrong!
            return;
        }

        e.setCancelled(true); //we need to handle block breaking manually

        //call block events
        block.onMined(e);
        block.onDamaged(1, b, true);

        //damage tool
        ItemStack held = e.getPlayer().getInventory().getItemInMainHand();
        VanillaUtils.damageTool(held, e.getPlayer());
    }

    //General events that destroy (or otherwise mess up) our block
    @EventHandler(ignoreCancelled = true)
    public void onBlockBurned(BlockBurnEvent e) {
        cancelCustomBlockEvent(getCustomBlockFromEvent(e), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        cancelCustomBlockEvent(getCustomBlockFromEvent(e), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        List<Block> blocks = new ArrayList<>(e.blockList());
        boolean didSound = false;
        for(Block b : blocks) {
            CustomBlock cb = KLWorld.get(b.getWorld()).getLoadedBlockAt(b.getX(), b.getY(), b.getZ());
            if(cb != null) {
                e.blockList().remove(b);
                cb.onDamaged((int)(1 + e.getYield()/4f), b, !didSound);
                didSound = true;
            }
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockFaded(BlockFadeEvent e) {
        cancelCustomBlockEvent(getCustomBlockFromEvent(e), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockFertilised(BlockFertilizeEvent e) {
        cancelCustomBlockEvent(getCustomBlockFromEvent(e), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        cancelCustomBlockEvent(getCustomBlockFromEvent(e), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent e) {
        cancelCustomBlockEvent(getCustomBlockFromEvent(e), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        cancelEventIfCBlockInList(e.getBlocks(), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        cancelEventIfCBlockInList(e.getBlocks(), e);
    }
    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent e) {
        cancelCustomBlockEvent(getCustomBlockFromEvent(e), e);
    }

}
