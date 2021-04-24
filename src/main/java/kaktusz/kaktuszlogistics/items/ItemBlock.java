package kaktusz.kaktuszlogistics.items;

import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class ItemBlock extends CustomItem {
    public boolean placeable;

    public ItemBlock(String type, String displayName, Material material, boolean canPlace) {
        super(type, displayName, material);
        this.placeable = canPlace;
    }

    @Override
    public void onTryPlace(BlockPlaceEvent e, ItemStack stack) {
        if(e.isCancelled()) return;

        if(!placeable)
            e.setCancelled(true);
    }


}
