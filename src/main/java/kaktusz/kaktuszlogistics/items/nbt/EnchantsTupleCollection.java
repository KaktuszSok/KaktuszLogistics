package kaktusz.kaktuszlogistics.items.nbt;

import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public class EnchantsTupleCollection {
    public final Map<Enchantment, Integer> enchants;

    public EnchantsTupleCollection(Map<Enchantment, Integer>  enchantments) {
        enchants = enchantments;
    }
}
