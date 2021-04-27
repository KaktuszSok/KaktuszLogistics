package kaktusz.kaktuszlogistics.items;

import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockItem extends CustomItem implements IPlacedListener {

    public BlockItem(String type, String displayName, Material material) {
        super(type, displayName, material);
    }

    @Override
    public void onTryUse(PlayerInteractEvent e, ItemStack stack) {

    }

    @Override
    public void onTryPlace(BlockPlaceEvent e, ItemStack stack) {
        if(e.isCancelled()) return;

        //TODO: implement block placing
        e.setCancelled(true);
    }

}
