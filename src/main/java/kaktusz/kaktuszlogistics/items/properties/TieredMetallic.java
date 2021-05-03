package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class TieredMetallic extends ItemQuality {

    public enum MetalTiers implements ItemQuality.QualityTier {
        POOR (ChatColor.DARK_GRAY + "Poor"),
        COMMON (ChatColor.GRAY + "Common"),
        GOOD (ChatColor.GREEN + "Good"),
        RARE (ChatColor.LIGHT_PURPLE + "Rare"),
        FLAWLESS (ChatColor.AQUA + "Flawless");

        private final String name;
        MetalTiers(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public TieredMetallic(CustomItem item) {
        super(item);
    }

    @Override
    public TieredMetallic loadDefaultTiers() {
        addTier(0.0F, MetalTiers.POOR);
        addTier(0.25F, MetalTiers.COMMON);
        addTier(0.85F, MetalTiers.GOOD);
        addTier(0.95F, MetalTiers.RARE);
        addTier(0.99F, MetalTiers.FLAWLESS);

        return this;
    }
}
