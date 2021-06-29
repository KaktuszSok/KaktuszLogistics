package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.items.properties.BlockDurability;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DurableBlock extends CustomBlock {

    private transient BlockDurability dura;

    public DurableBlock(BlockDurability property, Location location, ItemMeta meta) {
        super(property, location, meta);
        this.dura = property;
    }

    @Override
    protected void setUpTransients() {
        super.setUpTransients();
        dura = (BlockDurability)getType();
    }

    @Override
    public ItemStack getDrop(Block block) {
        ItemStack drop = super.getDrop(block);
        getType().item.findProperty(BlockDurability.class).setPercent(drop, 1.0f);
        return drop;
    }

    @Override
    public void onDamaged(int damage, boolean doSound, Player damager, boolean wasMined) {
        if(doSound) {
            float duraBeforeHit = dura.getDurability(data);
            if (dura.damageSound != null) {
                dura.damageSound.playAll(getLocation(), (duraBeforeHit-1) / Math.max(dura.getMaxDurability()-1, 1));
            }
        }

        dura.takeDamage(data, damage);
        if(dura.getDurability(data) <= 0) {
            super.onDamaged(damage, doSound, damager, wasMined);
        }
    }
}
