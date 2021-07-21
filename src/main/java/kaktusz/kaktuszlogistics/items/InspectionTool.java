package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.world.CustomBlock;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InspectionTool extends CustomItem {
    public InspectionTool(String type, String displayName, Material material) {
        super(type, displayName, material);
    }

    @Override
    public void onTryUse(PlayerInteractEvent e, ItemStack stack) {
        Block b = e.getClickedBlock();
        if(b != null) {
            CustomBlock cb = KLWorld.get(e.getClickedBlock().getWorld()).getBlockAt(b.getX(), b.getY(), b.getZ());
            e.getPlayer().sendMessage("");
            if(cb == null) {
                e.getPlayer().sendMessage("This is " + b.getType().name());
            } else {
                ItemStack blockItem = cb.getType().item.createStack(1);
                blockItem.setItemMeta(cb.data);
                e.getPlayer().sendMessage("This is " + cb.getType().item.getFullDisplayName(blockItem));
                //lore:
                List<String> lore = cb.getType().item.getItemLore(blockItem);
                for(String line : lore) {
                    e.getPlayer().sendMessage(line);
                }
            }
        }

        e.setUseInteractedBlock(Event.Result.DENY);
        super.onTryUse(e, stack);
    }

    @Override
    public void onTryUseEntity(PlayerInteractEntityEvent e, ItemStack stack) {
        e.getPlayer().sendMessage("This is " + e.getRightClicked().getName());

        super.onTryUseEntity(e, stack);
    }
}
