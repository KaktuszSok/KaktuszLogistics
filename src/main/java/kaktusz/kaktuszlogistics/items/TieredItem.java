package kaktusz.kaktuszlogistics.items;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.TreeMap;

public abstract class TieredItem extends CustomItem {

    interface QualityTier {}
    public TreeMap<Float, QualityTier> tiers;

    private boolean saveQuality = false; //making this true results in (almost certainly) unstackable items, but their true quality is preserved. Otherwise, quality is rounded down to the tier's minimum quality.

    //SETUP
    public TieredItem(String type, String displayName, Material material) {
        super(type, displayName, material);
    }

    public TieredItem addTier(float minQuality, QualityTier tier) {
        tiers.put(minQuality, tier);

        return this;
    }

    public TieredItem setTiers(TreeMap<Float, QualityTier> tiers) {
        this.tiers = tiers;

        return this;
    }

    /**
     * Setting this to true will result in (almost certainly) unstackable items!
     * However, their quality will be saved precisely. Otherwise, quality is rounded down to the tier's minimum quality.
     */
    public void setSaveQuality(boolean shouldSaveQuality) {
        saveQuality = shouldSaveQuality;
    }

    //ITEMSTACK
    @Override
    public ItemStack createStack(int amount) {
        return createStack(RandomUtils.nextFloat(), amount);
    }
    public ItemStack createStack(float quality, int amount) {
        ItemStack stack = super.createStack(amount);
        setQuality(stack, quality);
        return stack;
    }

    public void setQuality(ItemStack stack, float quality) {

    }
    public float getQuality(ItemStack stack) {

    }

    public QualityTier getTier(ItemStack stack) {

    }
    public QualityTier getTier(float quality) {
        return tiers.floorEntry(quality).getValue();
    }
}
