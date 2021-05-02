package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.DurableItem;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DurableBlock extends CustomBlock {

    Sound damageSound = Sound.BLOCK_ANVIL_LAND;
    public float damageVolume = 0.45f;
    public float damagePitchMin = 0.35f;
    public float damagePitchMax = 0.8f;

    public DurableBlock(ItemMeta customItemData) {
        super(customItemData);
    }
    public DurableBlock(DurableItem item, ItemStack stack) {
        super(item, stack);
    }

    public void setDamageSound(Sound sound, float volume, float pitchMin, float pitchMax) {
        damageSound = sound;
        damageVolume = volume;
        damagePitchMin = pitchMin;
        damagePitchMax = pitchMax;
    }

    @Override
    public ItemStack getDrop() {
        ItemStack drop = super.getDrop();
        ((DurableItem)type).setQuality(drop, 1.0f);
        return drop;
    }

    @Override
    public void onDamaged(int damage, Block b, boolean doSound) {
        DurableItem type = (DurableItem)this.type;

        if(doSound) {
            float duraBeforeHit = type.getDurability(data);
            if (damageSound != null) {
                float pitchPerStep = (damagePitchMax-damagePitchMin)/(float)type.maxDurability;
                float pitchRandom = MathsUtils.randomRange(-pitchPerStep*0.2f, pitchPerStep*0.2f);
                b.getWorld().playSound(b.getLocation(), damageSound, damageVolume, pitchRandom + MathsUtils.lerpc(damagePitchMin, damagePitchMax, duraBeforeHit / type.maxDurability));
            }
        }

        type.takeDamage(data, damage);
        if(type.getDurability(data) <= 0) {
            breakBlock(b, true);
        }
    }
}
