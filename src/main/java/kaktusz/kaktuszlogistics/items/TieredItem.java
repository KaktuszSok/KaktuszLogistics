package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public abstract class TieredItem extends CustomItem {
    public static NamespacedKey QUALITY_KEY;

    interface QualityTier {

        String getName();

    }

    public TreeMap<Float, QualityTier> tiers = new TreeMap<>();

    private boolean saveQuality = false; //making this true results in (almost certainly) unstackable items, but their true quality is preserved. Otherwise, quality is rounded down to the tier's minimum quality.

    //SETUP
    public TieredItem(String type, String displayName, Material material) {
        super(type, displayName, material);
        loadDefaultTiers();
    }

    public TieredItem addTier(float minQuality, QualityTier tier) {
        tiers.put(minQuality, tier);

        return this;
    }

    public TieredItem setTiers(TreeMap<Float, QualityTier> tiers) {
        this.tiers = tiers;

        return this;
    }

    public TieredItem clearTiers() {
        this.tiers.clear();

        return this;
    }

    /**
     * Override this to set up default tiers for a newly registered TieredItem
     */
    public abstract TieredItem loadDefaultTiers();

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
        ItemStack stack = super.createStackEarly(amount);
        setQuality(stack, quality, true);
        updateStack(stack);
        return stack;
    }

    @Override
    public void updateStack(ItemStack stack) {
        if(stack == null || stack.getItemMeta() == null) {
            return;
        }

        //set quality if item didn't have it before
        if(!stack.getItemMeta().getPersistentDataContainer().has(QUALITY_KEY, PersistentDataType.FLOAT)) {
            KaktuszLogistics.LOGGER.info("TieredItem " + getUnformattedDisplayName(stack) + " did not have quality info. Updating to random quality.");
            setQuality(stack, RandomUtils.nextFloat(), true);
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

        super.updateStack(stack);
    }

    public void setQuality(ItemStack stack, float quality) {
        setQuality(stack, quality, false);
    }
    public void setQuality(ItemStack stack, float quality, boolean dontUpdateStack) {
        if(!saveQuality) {
            quality = tiers.floorKey(quality);
        }
        setNBT(stack, QUALITY_KEY, PersistentDataType.FLOAT, quality);

        if(!dontUpdateStack)
            updateStack(stack);
    }
    public float getQuality(ItemStack stack) {
        return readNBT(stack, QUALITY_KEY, PersistentDataType.FLOAT);
    }

    public QualityTier getTier(ItemStack stack) {
        return getTier(getQuality(stack));
    }
    public QualityTier getTier(float quality) {
        return tiers.floorEntry(quality).getValue();
    }

    //DISPLAY

    @Override
    public List<String> getItemLore(ItemStack stack) {
        List<String> lore = new ArrayList<>();

        QualityTier tier = getTier(stack);
        lore.add(tier.getName());

        lore.addAll(super.getItemLore(stack));
        return lore;
    }
}
