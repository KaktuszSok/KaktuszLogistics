package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomBlock {
    private static final long serialVersionUID = 100L;

    public transient final CustomItem type;
    public ItemMeta data;

    public CustomBlock(CustomItem item, ItemMeta meta) {
        this.data = meta.clone();
        this.type = item;
    }

    /**
     * Creates a CustomBlock with the correct class given some item meta
     */
    public static CustomBlock createFromMeta(ItemMeta customItemData) {
        //read type from data
        String typeStr = customItemData.getPersistentDataContainer().get(CustomItem.TYPE_KEY, PersistentDataType.STRING);
        CustomItem type = CustomItemManager.tryGetItem(typeStr);
        return type.createCustomBlock(customItemData);
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
        return block.getType() == type.material;
    }

    //GETTERS
    public ItemStack getDrop() {
        ItemStack drop = type.createStack(1);
        drop.setItemMeta(data);
        return drop;
    }

    //EVENTS (note that these are cancelled where possible and replaced with custom logic)
    public void onPlaced(BlockPlaceEvent e) { //the appropriate block is placed by the BlockItem.
        Block b = e.getBlockPlaced();
        update(KLWorld.get(b.getWorld()), b.getX(), b.getY(), b.getZ());
    }

    public void onMined(BlockBreakEvent e) {

    }

    /**
     * Called when intentionally damaged, i.e. mined or exploded.
     * @param damage damage value. 1 for vanilla means.
     * @param b physical block, to provide world and coordinates
     */
    public void onDamaged(int damage, Block b, boolean doSound) {
        breakBlock(b, true);
    }

    public void breakBlock(Block b, boolean dropItem) {
        b.setType(Material.AIR); //clear physical block
        KLWorld.get(b.getWorld()).setBlock(null, b.getX(), b.getY(), b.getZ()); //clear KLWorld block
        //drop item
        if(!dropItem)
            return;
        ItemStack drop = getDrop();
        b.getWorld().dropItemNaturally(b.getLocation(), drop);
    }
}
