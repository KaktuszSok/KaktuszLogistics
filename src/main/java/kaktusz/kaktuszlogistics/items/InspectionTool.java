package kaktusz.kaktuszlogistics.items;

import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class InspectionTool extends ItemBlock {
    public InspectionTool(String type, String displayName, Material material) {
        super(type, displayName, material);
    }

    @Override
    public void onTryPlace(BlockPlaceEvent e, ItemStack stack) {
        e.getPlayer().sendMessage("This is " + e.getBlockAgainst().getType().name() + " at " + e.getBlockAgainst().getLocation().toString());
        super.onTryPlace(e, stack);
    }

    @Override
    public void onHeld(PlayerItemHeldEvent e, ItemStack stack) {
        e.getPlayer().sendMessage("You are now holding the " + getFullDisplayName(stack));
        super.onHeld(e, stack);
    }
}
