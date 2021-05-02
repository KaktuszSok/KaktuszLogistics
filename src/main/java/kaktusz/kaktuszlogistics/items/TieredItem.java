package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@SuppressWarnings("UnusedReturnValue")
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

        super.updateStack(stack);
    }

    /**
     * Called if the itemstack does not have qualify info attached (e.g. after updating, where before it was not a tiered item)
     */
    public void fixQuality(ItemStack stack) {
        KaktuszLogistics.LOGGER.info("TieredItem " + getUnformattedDisplayName(stack) + " did not have quality info. Updating to random quality.");
        setQuality(stack, RandomUtils.nextFloat(), true);
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
        //noinspection ConstantConditions
        if(!stack.hasItemMeta() || !stack.getItemMeta().getPersistentDataContainer().has(QUALITY_KEY, PersistentDataType.FLOAT)) {
            fixQuality(stack);
        }
        return readNBT(stack, QUALITY_KEY, PersistentDataType.FLOAT);
    }

    public QualityTier getTier(ItemStack stack) {
        return getTier(getQuality(stack));
    }
    public QualityTier getTier(float quality) {
        return tiers.floorEntry(quality).getValue();
    }

    //for blocks:
    public void fixQuality(ItemMeta meta) {
        KaktuszLogistics.LOGGER.info("TieredItem meta " + meta.getDisplayName() + " did not have quality info. Updating to random quality.");
        setQuality(meta, RandomUtils.nextFloat());
    }
    public void setQuality(ItemMeta meta, float quality) {
        if(!saveQuality) {
            quality = tiers.floorKey(quality);
        }
        setNBT(meta, QUALITY_KEY, PersistentDataType.FLOAT, quality);
    }
    public float getQuality(ItemMeta meta) {
        if(!meta.getPersistentDataContainer().has(QUALITY_KEY, PersistentDataType.FLOAT)) {
            fixQuality(meta);
        }
        return readNBT(meta, QUALITY_KEY, PersistentDataType.FLOAT);
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
