package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.items.properties.ItemQuality;
import kaktusz.kaktuszlogistics.world.CustomBlock;
import kaktusz.kaktuszlogistics.world.DurableBlock;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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
                e.getPlayer().sendMessage("This is " + cb.getType().item.getFullDisplayName(cb.getDrop(b)));
                //extra info:
                List<String> lore = new ArrayList<>();
                ItemQuality quality = cb.getType().item.findProperty(ItemQuality.class);
                if(quality != null) {
                    quality.modifyLore(lore, cb.getDrop(b));
                }
                if(cb instanceof DurableBlock) {
                    DurableBlock db = (DurableBlock)cb;
                    ItemStack dbItem = db.getType().item.createStack(1);
                    dbItem.setItemMeta(db.data);
                    db.getType().modifyLore(lore, dbItem);
                }
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
