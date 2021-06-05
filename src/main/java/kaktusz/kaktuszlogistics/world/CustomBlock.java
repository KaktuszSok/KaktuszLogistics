package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * A custom block is an instance of a custom item which is physically placed in the world.
 */
public class CustomBlock {

    public transient final ItemPlaceable type;
    public ItemMeta data;

    public CustomBlock(ItemPlaceable prop, ItemMeta meta) {
        this.data = meta.clone();
        this.type = prop;
    }

    /**
     * Creates a CustomBlock with the correct class given some item meta
     */
    public static CustomBlock createFromMeta(ItemMeta customItemData) {
        //read type from data
        String typeStr = customItemData.getPersistentDataContainer().get(CustomItem.TYPE_KEY, PersistentDataType.STRING);
        CustomItem type = CustomItemManager.tryGetItem(typeStr);
        //check if type was valid
        if(type == null)
            return null;
        //try get placeable property
        ItemPlaceable placeableProperty = type.findProperty(ItemPlaceable.class);
        if(placeableProperty == null)
            return null;
        return placeableProperty.createCustomBlock(customItemData);
    }

    /**
     * @return true if verify passes, false if it fails and the block removes itself from existence
     */
    public boolean update(KLWorld world, int x, int y, int z) {
        if(!verify(world, x, y, z)) {
            world.setBlock(null, x, y, z);
            KaktuszLogistics.LOGGER.info("Removing CustomBlock at " + x + "," + y + "," + z + " as verification failed.");
            return false;
        }
        return true;
    }

    public boolean verify(KLWorld world, int x, int y, int z) {
        return verify(world.world.getBlockAt(x, y, z));
    }
    public boolean verify(Block block) {
        return type.verify(block);
    }

    //GETTERS
    public ItemStack getDrop(Block block) {
        ItemStack drop = type.item.createStack(1);
        drop.setItemMeta(data);
        return drop;
    }

    //EVENTS (note that the events are cancelled where possible and their original effects should be replaced with custom logic)
    public void onPlaced(BlockPlaceEvent e) { //the appropriate block is placed by the ItemPlaceable.
        Block b = e.getBlockPlaced();
        update(KLWorld.get(b.getWorld()), b.getX(), b.getY(), b.getZ());
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
    @SuppressWarnings("unused")
    public void onMined(BlockBreakEvent e) {

    }

    /**
     * Called when intentionally damaged, i.e. mined or exploded.
     * @param damage damage value. 1 for vanilla mining. For explosions, damage scales with blast power.
     * @param b physical block, to provide world and coordinates
     */
    public void onDamaged(int damage, Block b, boolean doSound) {
        breakBlock(b, true);
    }

    /**
     * Called when the player right-clicks this block
     */
    public void onInteracted(PlayerInteractEvent e) {

    }

    public void breakBlock(Block b, boolean dropItem) {
        //get item
        ItemStack drop = null;
        if(dropItem)
            drop = getDrop(b);

        //remove block
        b.setType(Material.AIR); //clear physical block
        KLWorld.get(b.getWorld()).setBlock(null, b.getX(), b.getY(), b.getZ()); //clear KLWorld block

        //drop item
        if(dropItem)
            b.getWorld().dropItemNaturally(b.getLocation(), drop);
    }
}
