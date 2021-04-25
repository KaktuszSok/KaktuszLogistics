package kaktusz.kaktuszlogistics.items;

import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class ItemBlock extends CustomItem {

    public ItemBlock(String type, String displayName, Material material) {
        super(type, displayName, material);
    }

    @Override
    public void onTryPlace(BlockPlaceEvent e, ItemStack stack) {
        if(e.isCancelled()) return;

        //TODO: implement block placing
        e.setCancelled(true);
    }

}
