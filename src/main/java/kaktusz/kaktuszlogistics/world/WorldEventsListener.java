package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Forwards events from vanilla & modules to KLWorlds, KLChunks and CustomBlocks
 */
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

    @EventHandler(ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent e) {
        KLWorld world = KLWorld.loadedWorlds.get(e.getWorld());
        if(world == null)
            return;

        world.unload();
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
    private static CustomBlock getCustomBlockFromEvent(BlockEvent e) {
        Block b = e.getBlock();
        return KLWorld.get(b.getWorld()).getLoadedBlockAt(b.getX(), b.getY(), b.getZ());
    }
    private static CustomBlock getCustomBlockFromLocation(Location loc) {
        return KLWorld.get(loc.getWorld()).getLoadedBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    private static void cancelCustomBlockEvent(CustomBlock block, Cancellable e) {
        if(block == null) return;
        e.setCancelled(true);
    }
    private static void cancelEventIfCBlockInList(List<Block> blocks, Cancellable e) {
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

    @EventHandler(ignoreCancelled = true)
    public void onBlockInteracted(PlayerInteractEvent e) {
        //noinspection ConstantConditions
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.getClickedBlock().getType().isInteractable() || e.getPlayer().isSneaking()) //ignore event if we're not interacting with the block
            return;

        Block b = e.getClickedBlock();
        if(b == null)
            return;
        CustomBlock cb = getCustomBlockFromLocation(b.getLocation());
        if(cb == null)
            return;

        e.setUseInteractedBlock(Event.Result.DENY);
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
                cb.onDamaged((int)(1 + (2.5f/e.getYield())), b, !didSound); //yield is inverted?
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
    public void onBlockPhysics(BlockPhysicsEvent e) { //apparently this is quite messy so best to avoid custom blocks w/ physics where possible
//        Bukkit.broadcastMessage(e.getBlock().getType().name() + " // source: " + e.getSourceBlock().getType().name());
//        cancelCustomBlockEvent(getCustomBlockFromEvent(e), e);
//        Bukkit.broadcastMessage("cancelled: " + e.isCancelled());
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        cancelEventIfCBlockInList(e.getBlocks(), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        cancelEventIfCBlockInList(e.getBlocks(), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onFurnaceBurn(FurnaceBurnEvent e) {
        cancelCustomBlockEvent(getCustomBlockFromEvent(e), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        cancelCustomBlockEvent(getCustomBlockFromLocation(e.getBlock().getLocation()), e);
    }

}
