package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * A custom block is an instance of a custom item which is physically placed in the world.
 * You must call setLocation() after deserialising this object, as the location is not stored to save space.
 */
public class CustomBlock implements Serializable {
    private static final long serialVersionUID = 100L;

    private transient ItemPlaceable type;
    private transient Location location;
    @SuppressWarnings("CanBeFinal")
    public ItemMeta data;

    public CustomBlock(ItemPlaceable prop, Location location, ItemMeta meta) {
        this.location = location;
        this.data = meta.clone();
        this.type = prop;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setUpTransients();
    }

    protected void setUpTransients() {
        CustomItem ci = CustomItem.getFromMeta(data);
        if(ci != null)
            type = ci.findProperty(ItemPlaceable.class);
        else
            type = null;
    }

    /**
     * @return true if verify passes, false if it fails and the block removes itself from existence
     */
    public boolean update() {
        if(!verify()) {
            KLWorld world = KLWorld.get(location.getWorld());
            if(world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ()) != null) {
                world.setBlock(null, location.getBlockX(), location.getBlockY(), location.getBlockZ());
                KaktuszLogistics.LOGGER.info("Removing CustomBlock at " + new VanillaUtils.BlockPosition(location) + " as verification failed.");
            }
            return false;
        }
        return true;
    }

    public final boolean verify() {
        if(getType() == null)
            return false;
        return verify(Objects.requireNonNull(location.getWorld()).getBlockAt(location));
    }
    public boolean verify(Block block) {
        return type.verify(block);
    }

    //GETTERS & SETTERS
    public ItemPlaceable getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }

    public ItemStack getDrop(Block block) {
        ItemStack drop = type.item.createStack(1);
        drop.setItemMeta(data);
        return drop;
    }

    //EVENTS (note that the events are cancelled where possible and their original effects should be replaced with custom logic)
    public void onPlaced(BlockPlaceEvent e) { //the appropriate block is placed by the ItemPlaceable.
        update();
    }

    /**
     * Called when the block is set for whatever reason
     */
    public void onSet(KLWorld world, int x, int y, int z) {

    }

    /**
     * Called when the block is removed for whatever reason
     */
    public void onRemoved(KLWorld world, int x, int y, int z) {

    }

    /**
     * Called when a player mines this block
     */
    @SuppressWarnings({"unused", "EmptyMethod"})
    public void onMined(BlockBreakEvent e) {

    }

    /**
     * Called when intentionally damaged, i.e. mined or exploded.
     * @param damage damage value. 1 for vanilla mining. For explosions, damage scales with blast power.
     * @param doSound if false, no sound will be played (if a player mines a block they will hear the vanilla block break sound anyway as it is client-side)
     * @param damager the player who damaged this block (or null if not applicable)
     * @param wasMined true if the damage source was the block being mined by the damager
     */
    public void onDamaged(int damage, boolean doSound, Player damager, boolean wasMined) {
        boolean dropItem = damager == null || damager.getGameMode() != GameMode.CREATIVE || damager.isSneaking();
        breakBlock(dropItem, doSound, wasMined ? damager : null); //don't drop block if damager is in creative and not sneaking
    }

    /**
     * Called when the player right-clicks this block
     */
    public void onInteracted(PlayerInteractEvent e) {

    }

    /**
     * Removes the block from both the physical world and the KL data
     */
    public final void breakBlock(boolean dropItem) {
        breakBlock(dropItem, true, null);
    }
    /**
     * Removes the block from both the physical world and the KL data
     * @param playVanillaSound Should the vanilla sound for this block breaking be played?
     * @param playerWhoMined The player who mined this block (or null). This player will not hear an additional copy of the vanilla break sound, as it is already played client-side.
     */
    public void breakBlock(boolean dropItem, boolean playVanillaSound, Player playerWhoMined) {
        Block b = location.getBlock();
        //get item
        ItemStack drop = null;
        if(dropItem)
            drop = getDrop(b);

        //play sound
        if(playVanillaSound) {
            VanillaUtils.playVanillaBreakSound(b, playerWhoMined);
        }
        //TODO play block break particles for other players

        //remove block
        b.setType(Material.AIR); //clear physical block
        KLWorld.get(b.getWorld()).setBlock(null, b.getX(), b.getY(), b.getZ()); //clear KLWorld block

        //drop item
        if(dropItem)
            b.getWorld().dropItemNaturally(b.getLocation(), drop);
    }
}
