package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.items.properties.BlockDurability;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DurableBlock extends CustomBlock {

    private transient final BlockDurability dura;

    public DurableBlock(BlockDurability property, Location location, ItemMeta meta) {
        super(property, location, meta);
        this.dura = property;
    }

    @Override
    public ItemStack getDrop(Block block) {
        ItemStack drop = super.getDrop(block);
        type.item.findProperty(BlockDurability.class).setPercent(drop, 1.0f);
        return drop;
    }

    @Override
    public void onDamaged(int damage, boolean doSound) {
        if(doSound) {
            float duraBeforeHit = dura.getDurability(data);
            if (dura.damageSound != null) {
                dura.damageSound.playAll(location, (duraBeforeHit-1) / Math.max(dura.getMaxDurability()-1, 1));
            }
        }

        dura.takeDamage(data, damage);
        if(dura.getDurability(data) <= 0) {
            breakBlock(true);
        }
    }
}
