package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import kaktusz.kaktuszlogistics.world.CustomBlock;
import kaktusz.kaktuszlogistics.world.DurableBlock;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class DurableItem extends TieredItem {

    public final int maxDurability;

    public enum DurabilityTiers implements QualityTier {
        LOW (ChatColor.RED.toString()),
        MID (ChatColor.YELLOW.toString()),
        HIGH (ChatColor.WHITE.toString());

        private final String name;
        DurabilityTiers(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    //SETUP
    public DurableItem(String type, String displayName, Material material, int maxDurability) {
        super(type, displayName, material);
        this.maxDurability = maxDurability;
        setSaveQuality(true);
    }

    @Override
    public void fixQuality(ItemStack stack) {
        KaktuszLogistics.LOGGER.info("DurableItem " + getUnformattedDisplayName(stack) + " did not have quality info. Updating to max quality.");
        setQuality(stack, 1.0f);
    }
    @Override
    public void fixQuality(ItemMeta meta) {
        KaktuszLogistics.LOGGER.info("DurableItem meta " + meta.getDisplayName() + " did not have quality info. Updating to max quality.");
        setQuality(meta, 1.0f);
    }

    @Override
    public DurableItem loadDefaultTiers() {
        addTier(0.0F, DurabilityTiers.LOW);
        addTier(0.3F, DurabilityTiers.MID);
        addTier(0.75F, DurabilityTiers.HIGH);

        return this;
    }

    //ITEMSTACK
    @Override
    public ItemStack createStack(int amount) {
        return super.createStack(1.0f, amount);
    }

    public int getDurability(ItemStack stack) {
        return getDurability(stack.getItemMeta());
    }
    public int getDurability(ItemMeta meta) {
        float quality = getQuality(meta);
        return (int)Math.ceil(MathsUtils.lerp(0, maxDurability, quality));
    }

    public void takeDamage(ItemStack stack, int dmg) {
        takeDamage(stack.getItemMeta(), dmg);
    }
    public void takeDamage(ItemMeta meta, int dmg) {
        int dura = getDurability(meta);
        float targetQ = MathsUtils.lerpInverseC(0, maxDurability, dura-dmg);
        setQuality(meta, targetQ);
    }

    @Override
    public List<String> getItemLore(ItemStack stack) {
        List<String> lore = new ArrayList<>();

        QualityTier tier = getTier(stack);
        lore.add(tier.getName() + ChatColor.BOLD + "Durability: " + getDurability(stack) + "/" + maxDurability);

        lore.addAll(super.getItemLore(stack));
        return lore;
    }

    @Override
    protected DurableBlock createCustomBlock(ItemStack stack) {
        return new DurableBlock(this, stack);
    }
}
