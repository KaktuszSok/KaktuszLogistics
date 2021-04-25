package kaktusz.kaktuszlogistics.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.TreeMap;

public class MetalItem extends TieredItem {

    public enum MetalTiers implements QualityTier {
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

    public MetalItem(String type, String displayName, Material material) {
        super(type, displayName, material);
    }

    @Override
    public MetalItem loadDefaultTiers() {
        addTier(0.0F, MetalTiers.POOR);
        addTier(0.25F, MetalTiers.COMMON);
        addTier(0.85F, MetalTiers.GOOD);
        addTier(0.95F, MetalTiers.RARE);
        addTier(0.99F, MetalTiers.FLAWLESS);

        return this;
    }
}
