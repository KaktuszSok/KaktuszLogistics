package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.TreeMap;

/**
 * Property which adds a quality tag to the item
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public abstract class ItemQuality extends ItemProperty {
    public static NamespacedKey QUALITY_KEY;

    public interface QualityTier {

        String getName();

    }

    public TreeMap<Float, QualityTier> tiers = new TreeMap<>();

    private boolean saveQuality = false;

    //SETUP
    public ItemQuality(CustomItem item) {
        super(item);
        loadDefaultTiers();
    }

    public ItemQuality addTier(float minQuality, QualityTier tier) {
        tiers.put(minQuality, tier);

        return this;
    }

    public ItemQuality setTiers(TreeMap<Float, QualityTier> tiers) {
        this.tiers = tiers;

        return this;
    }

    public ItemQuality clearTiers() {
        this.tiers.clear();

        return this;
    }

    /**
     * Override this to set up default tiers for a newly registered tiered item
     */
    public abstract ItemQuality loadDefaultTiers();

    /**
     * Setting this to true will result in (almost certainly) unstackable items!
     * However, their quality will be saved precisely. Otherwise, quality is rounded down to the tier's minimum quality.
     */
    public void setSaveQuality(boolean shouldSaveQuality) {
        saveQuality = shouldSaveQuality;
    }

    //ITEMSTACK
    @Override
    public void onCreateStack(ItemStack stack) {
        setQuality(stack, RandomUtils.nextFloat(), true);
    }

    @Override
    public void onUpdateStack(ItemStack stack) {
        if(stack.getItemMeta() == null)
            return;

        //set quality if item didn't have it before
        if(!stack.getItemMeta().getPersistentDataContainer().has(QUALITY_KEY, PersistentDataType.FLOAT)) {
            fixQuality(stack);
        }

        //update quality if it no longer exactly matches a tier
        if(!saveQuality) {
            float quality = getQuality(stack);
            if (!tiers.containsKey(quality)) {
                Float tierDown = tiers.floorKey(quality);
                Float tierUp = tiers.ceilingKey(quality);
                if (tierUp == null)
                    quality = tierDown;
                else if (tierDown == null)
                    quality = tierUp;
                else { //if we are between two qualities, snap to the closer one
                    float normalisedClosenessToTierUp = MathsUtils.lerpInverse(tierDown, tierUp, quality); //0 when quality=tierDown, 1 when quality=tierUp
                    if (normalisedClosenessToTierUp >= 0.5f)
                        quality = tierUp;
                    else
                        quality = tierDown;
                }

                setQuality(stack, quality, true);
            }
        }
    }

    /**
     * Called if the itemstack does not have quality info attached (e.g. after updating, where before it was not a tiered item)
     */
    public void fixQuality(ItemStack stack) {
        if(stack.getItemMeta() == null)
            return;

        fixQuality(stack.getItemMeta());
    }

    public void setQuality(ItemStack stack, float quality) {
        setQuality(stack, quality, false);
    }
    public void setQuality(ItemStack stack, float quality, boolean dontUpdateStack) {
        ItemMeta meta = stack.getItemMeta();
        setQuality(meta, quality);
        stack.setItemMeta(meta);

        if(!dontUpdateStack)
            item.updateStack(stack);
    }
    public float getQuality(ItemStack stack) {
        if(stack.getItemMeta() == null)
            return 0;

        return getQuality(stack.getItemMeta());
    }

    public QualityTier getTier(ItemStack stack) {
        return getTier(getQuality(stack));
    }
    public QualityTier getTier(float quality) {
        return tiers.floorEntry(quality).getValue();
    }

    //for blocks:
    public void fixQuality(ItemMeta meta) {
        setQuality(meta, RandomUtils.nextFloat());
    }
    public void setQuality(ItemMeta meta, float quality) {
        if(!saveQuality) {
            quality = tiers.floorKey(quality);
        }
        CustomItem.setNBT(meta, QUALITY_KEY, PersistentDataType.FLOAT, quality);
    }
    public float getQuality(ItemMeta meta) {
        if(!meta.getPersistentDataContainer().has(QUALITY_KEY, PersistentDataType.FLOAT)) {
            fixQuality(meta);
        }
        return CustomItem.readNBT(meta, QUALITY_KEY, PersistentDataType.FLOAT);
    }

    //DISPLAY
    @Override
    public void modifyLore(List<String> lore, ItemStack item) {
        QualityTier tier = getTier(item);
        lore.add(tier.getName());
    }
}
