package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.*;

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
    //TODO: multithreading?
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
        KLWorld world = KLWorld.get(b.getWorld());
        //check if inside a multiblock
        CustomBlock multi = getMultiblockFromBlock(b, world);
        if(multi != null)
            return multi;

        return world.getLoadedBlockAt(b.getX(), b.getY(), b.getZ());
    }
    private static CustomBlock getCustomBlockFromLocation(Location loc) {
        KLWorld world = KLWorld.get(loc.getWorld());
        //check if inside a multiblock
        CustomBlock multi = getMultiblockFromLocation(loc, world);
        if(multi != null)
            return multi;

        return world.getLoadedBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private static CustomBlock getCustomBlockFromEvent_NoMultiblocks(BlockEvent e) {
        Block b = e.getBlock();
        return KLWorld.get(b.getWorld()).getLoadedBlockAt(b.getX(), b.getY(), b.getZ());
    }
    private static CustomBlock getCustomBlockFromLocation_NoMultiblocks(Location loc) {
        return KLWorld.get(loc.getWorld()).getLoadedBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private static MultiblockBlock getMultiblockFromBlock(Block b, KLWorld world) {
        KLChunk chunk = world.getLoadedChunkAt(blockToChunkCoord(b.getX()), blockToChunkCoord(b.getZ()));
        if(chunk != null) {
            Set<BlockPosition> multiblocks = chunk.getExtraData("multiblocks");
            if(multiblocks != null) {
                for (BlockPosition pos : multiblocks) {
                    CustomBlock multiblock = world.getBlockAt(pos.x, pos.y, pos.z);
                    if(multiblock instanceof MultiblockBlock
                            && ((MultiblockBlock)multiblock).isBlockPartOfMultiblock(b)) {
                        return (MultiblockBlock)multiblock;
                    }
                }
            }
        }

        return null;
    }
    private static MultiblockBlock getMultiblockFromLocation(Location loc, KLWorld world) {
        KLChunk chunk = world.getChunkAt(blockToChunkCoord(loc.getBlockX()), blockToChunkCoord(loc.getBlockZ()));
        if(chunk != null) {
            Set<BlockPosition> multiblocks = chunk.getExtraData("multiblocks");
            if(multiblocks != null) {
                for (BlockPosition pos : multiblocks) {
                    CustomBlock multiblock = chunk.world.getBlockAt(pos.x, pos.y, pos.z);
                    if(multiblock instanceof MultiblockBlock
                            && ((MultiblockBlock)multiblock).isPosPartOfMultiblock(new BlockPosition(loc))) {
                        return (MultiblockBlock)multiblock;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Cancels an event if the provided custom block is not null
     * @return True if the event was cancelled, false otherwise
     */
    @SuppressWarnings("UnusedReturnValue")
    private static boolean cancelCustomBlockEvent(CustomBlock block, Cancellable e) {
        if(block == null) return false;
        e.setCancelled(true);
        return true;
    }
    private static void cancelEventIfCBlockInList(List<Block> blocks, Cancellable e) {
        for(Block b : blocks) {
            if(getCustomBlockFromLocation(b.getLocation()) != null) {
                e.setCancelled(true);
                return;
            }
        }
    }
    private static void cancelEventIfCBlockInList(List<Block> blocks, Cancellable e, BlockFace offset) {
        for(Block b : blocks) {
            if(getCustomBlockFromLocation(b.getLocation().add(offset.getModX(), offset.getModY(), offset.getModZ())) != null) {
                e.setCancelled(true);
                return;
            }
        }
    }

    private static void updateMultiblockValidity(MultiblockBlock multiblock, Block newBlock) {
        if(multiblock != null) {
            multiblock.reverifyStructure();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockMined(BlockBreakEvent e) {
        CustomBlock block = getCustomBlockFromEvent(e);
        if(block == null)
            return;
        KLWorld world = KLWorld.get(block.getLocation().getWorld());
        if(!block.update()) { //something went wrong!
            return;
        }

        e.setCancelled(true); //we need to handle block breaking manually

        //call block events
        block.onMined(e);
        block.onDamaged(1, true, e.getPlayer(), true);

        //damage tool
        ItemStack held = e.getPlayer().getInventory().getItemInMainHand();
        damageTool(held, e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockInteracted(PlayerInteractEvent e) {
        //noinspection ConstantConditions
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.getClickedBlock().getType().isInteractable()) //ignore event if we're not interacting with the block
            return;

        //also not interacting if we are sneaking and don't have an empty handy
        if(e.getPlayer().isSneaking()) {
            ItemStack usedItem = e.getItem();
            if(usedItem != null && usedItem.getAmount() != 0 && usedItem.getType() != Material.AIR) {
                return;
            }
        }

        Block b = e.getClickedBlock();
        if(b == null)
            return;
        CustomBlock cb = getCustomBlockFromLocation_NoMultiblocks(b.getLocation());
        if(cb == null)
            return;

        e.setUseInteractedBlock(Event.Result.DENY);
        cb.onInteracted(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaced(BlockPlaceEvent e) {
        updateMultiblockValidity(getMultiblockFromBlock(e.getBlockPlaced(), KLWorld.get(e.getBlockAgainst().getWorld())), e.getBlockPlaced());
    }

    /**
     * Called when a hopper etc. puts an item into an inventory
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockReceivedItem(InventoryMoveItemEvent e) {
        //block custom blocks from receiving items
        if(e.getDestination().getLocation() != null)
            cancelCustomBlockEvent(getCustomBlockFromLocation(e.getDestination().getLocation()), e);
        //block extracting items from custom blocks
        if(!e.isCancelled() && e.getSource().getLocation() != null)
            cancelCustomBlockEvent(getCustomBlockFromLocation(e.getSource().getLocation()), e);
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
            CustomBlock cb = getCustomBlockFromLocation(b.getLocation());
            if(cb != null) {
                e.blockList().remove(b);
                cb.onDamaged((int)(1 + (2.5f/e.getYield())), !didSound, null, false); //yield is inverted?
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
        if(!e.isCancelled())
            cancelEventIfCBlockInList(e.getBlocks(), e, e.getDirection());
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        cancelEventIfCBlockInList(e.getBlocks(), e);
        if(!e.isCancelled())
            cancelEventIfCBlockInList(e.getBlocks(), e, e.getDirection());
    }
    @EventHandler(ignoreCancelled = true)
    public void onFurnaceBurn(FurnaceBurnEvent e) { //don't allow custom block furnaces to function as normal furnaces
        cancelCustomBlockEvent(getCustomBlockFromEvent(e), e);
    }
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        cancelCustomBlockEvent(getCustomBlockFromLocation(e.getBlock().getLocation()), e);
    }

}
