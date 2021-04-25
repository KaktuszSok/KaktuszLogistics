package kaktusz.kaktuszlogistics.items.nbt;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.StringJoiner;

public class EnchantsContainer implements PersistentDataType<String, EnchantsTupleCollection> {

    public static EnchantsContainer ENCHANTMENTS = new EnchantsContainer();

    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public Class<EnchantsTupleCollection> getComplexType() {
        return EnchantsTupleCollection.class;
    }

    @Override
    public String toPrimitive(EnchantsTupleCollection enchantsTupleCollection, PersistentDataAdapterContext persistentDataAdapterContext) {
        StringJoiner data = new StringJoiner(",");

        for (Enchantment enchant : enchantsTupleCollection.enchants.keySet()) {
            int level = enchantsTupleCollection.enchants.get(enchant);
            data.add(enchant.getKey() + " " + level);
        }

        return data.toString();
    }

    @Override
    public EnchantsTupleCollection fromPrimitive(String string, PersistentDataAdapterContext persistentDataAdapterContext) {
        EnchantsTupleCollection result = new EnchantsTupleCollection(new HashMap<>());

        String[] tuples = string.split(",");
        for(String tuple : tuples) {
            String[] elements = tuple.split(" ");
            if(elements.length != 2) {
                KaktuszLogistics.LOGGER.warning("Failed parsing enchantment tuple: " + tuple + " (invalid element count)");
                continue;
            }
            String enchantKeyStr = elements[0];
            NamespacedKey enchantKey = NamespacedKey.fromString(enchantKeyStr);
            if(enchantKey == null) {
                KaktuszLogistics.LOGGER.warning("Failed parsing enchantment key: " + enchantKeyStr + " (could not convert to NamespacedKey)");
                continue;
            }
            String enchantLevelStr = elements[1];
            int enchantLevel;
            if(!NumberUtils.isDigits(enchantLevelStr)) {
                KaktuszLogistics.LOGGER.warning("Failed parsing enchantment level: " + enchantLevelStr + " (could not convert to integer)");
                continue;
            } else {
                enchantLevel = Integer.parseInt(enchantLevelStr);
            }

            Enchantment enchantment = Enchantment.getByKey(enchantKey);
            if(enchantment == null) {
                KaktuszLogistics.LOGGER.warning("Failed parsing enchantment key: " + enchantKeyStr + " (Enchantment not found)");
                continue;
            }

            result.enchants.put(enchantment, enchantLevel);
        }

        return result;
    }
}
