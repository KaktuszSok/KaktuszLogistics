package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.items.properties.BlockDurability;
import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DurableBlock extends CustomBlock {

    /**
     * The item that this block is based on MUST have the ItemDurability property
     */
    public DurableBlock(ItemPlaceable placeProperty, ItemMeta meta) {
        super(placeProperty, meta);
    }

    @Override
    public ItemStack getDrop(Block block) {
        ItemStack drop = super.getDrop(block);
        type.item.findProperty(BlockDurability.class).setPercent(drop, 1.0f);
        return drop;
    }

    @Override
    public void onDamaged(int damage, Block b, boolean doSound) {
        BlockDurability dura = type.item.findProperty(BlockDurability.class);

        if(doSound) {
            float duraBeforeHit = dura.getDurability(data);
            if (dura.damageSound != null) {
                float pitchPerStep = (dura.damagePitchMax-dura.damagePitchMin)/Math.max(dura.getMaxDurability()-1, 1);
                float pitchRandom = MathsUtils.randomRange(-pitchPerStep*0.2f, pitchPerStep*0.2f);
                b.getWorld().playSound(b.getLocation(), dura.damageSound, dura.damageVolume,
                        pitchRandom + MathsUtils.lerpc(dura.damagePitchMin, dura.damagePitchMax, (duraBeforeHit-1) / Math.max(dura.getMaxDurability()-1, 1)));
            }
        }

        dura.takeDamage(data, damage);
        if(dura.getDurability(data) <= 0) {
            breakBlock(b, true);
        }
    }
}
