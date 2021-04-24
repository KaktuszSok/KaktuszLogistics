package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.items.nbt.EnchantsContainer;
import kaktusz.kaktuszlogistics.items.nbt.EnchantsTupleCollection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class CustomItem {
    public static NamespacedKey TYPE_KEY;
    public static NamespacedKey ENCHANTS_KEY;

    public final String type; //internal name of item
    private final String displayName; //name of item seen by players
    public final Material material; //vanilla item that represents this

    private String nameFormatting = ChatColor.RESET.toString();
    private List<String> lore = null;
    @SuppressWarnings("FieldMayBeFinal")
    private Map<Enchantment, Integer> enchants = new HashMap<>();

    //SETUP
    public CustomItem(String type, String displayName, Material material) {
        this.type = type;
        this.displayName = displayName;
        this.material = material;

        CustomItemManager.registerItem(this);
    }

    public CustomItem setNameFormatting(ChatColor... formatting) {
        StringBuilder sb = new StringBuilder();
        for(ChatColor c : formatting) {
            sb.append(c);
        }
        nameFormatting = sb.toString();

        return this;
    }

    public CustomItem setLore(String... lines) {
        lore = Arrays.asList(lines);

        return this;
    }

    public CustomItem addEnchantment(Enchantment enchantment, int level) {
        enchants.put(enchantment, level);

        return this;
    }

    //ITEMSTACK
    public ItemStack createStack(int amount) {
        ItemStack stack = new ItemStack(material, amount);
        addTypeNBT(stack);
        updateStack(stack);
        return stack;
    }

    /**
     * Returns the type of CustomItem that a stack is tagged as being, or null if it is not a custom item.
     */
    public static CustomItem getFromStack(ItemStack stack) {
        String type = readNBT(stack, TYPE_KEY, PersistentDataType.STRING);
        return CustomItemManager.tryGetItem(type);
    }

    public void updateStack(ItemStack stack) {
        //ENCHANTMENTS
        //clear saved default enchantments
        EnchantsTupleCollection defaultEnchants = readEnchantMarker(stack);
        if(defaultEnchants != null) {
            for(Enchantment ench : defaultEnchants.enchants.keySet()) {
                if(stack.containsEnchantment(ench))
                    stack.removeEnchantment(ench);
            }
        }
        //clear enchantments we are about to add (to avoid duplicates)
        for(Enchantment ench : enchants.keySet()) {
            if(stack.containsEnchantment(ench))
                stack.removeEnchantment(ench);
        }
        stack.addUnsafeEnchantments(enchants);
        addEnchantMarker(stack);

        //DISPLAY
        setDisplayName(stack);
        setItemLore(stack);
    }

    /**
     * Adds an NBT tag to an ItemStack to identify it as a particular type of CustomItem
     */
    private void addTypeNBT(ItemStack stack) {
        setNBT(stack, TYPE_KEY, PersistentDataType.STRING, type);
    }
    public boolean isStackThisType(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if(meta == null) return false;

        if(meta.getPersistentDataContainer().has(TYPE_KEY, PersistentDataType.STRING)) {
            String type = meta.getPersistentDataContainer().get(TYPE_KEY, PersistentDataType.STRING);
            return type != null && type.equals(this.type);
        }

        return false;
    }

    /**
     * Adds an NBT tag listing all the enchantments added by default to this item, so that player-added enchants don't get cleared when updating
     */
    private void addEnchantMarker(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if(meta == null) return;
        meta.getPersistentDataContainer().set(ENCHANTS_KEY, EnchantsContainer.ENCHANTMENTS, new EnchantsTupleCollection(enchants));
        stack.setItemMeta(meta);
    }
    private EnchantsTupleCollection readEnchantMarker(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if(meta == null) return null;

        if(meta.getPersistentDataContainer().has(ENCHANTS_KEY, EnchantsContainer.ENCHANTMENTS)) {
            return meta.getPersistentDataContainer().get(ENCHANTS_KEY, EnchantsContainer.ENCHANTMENTS);
        }

        return null;
    }

    protected static <T,Z> void setNBT(ItemStack stack, NamespacedKey key, PersistentDataType<T,Z> dataType, Z data) {
        if(stack == null) return;

        ItemMeta meta = stack.getItemMeta();
        if(meta == null) return;
        meta.getPersistentDataContainer().set(key, dataType, data);
        stack.setItemMeta(meta);
    }
    protected static <T,Z> Z readNBT(ItemStack stack, NamespacedKey key, PersistentDataType<T,Z> dataType) {
        if(stack == null) return null;

        ItemMeta meta = stack.getItemMeta();
        if(meta == null) return null;

        if(meta.getPersistentDataContainer().has(key, dataType)) {
            return meta.getPersistentDataContainer().get(key, dataType);
        }

        return null;
    }

    /**
     * Sets the display name of an ItemStack so that it matches this custom item
     */
    private void setDisplayName(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName(getFullDisplayName(stack));
        stack.setItemMeta(meta);
    }

    /**
     * Sets the lore of an ItemStack so that it matches this custom item
     */
    private void setItemLore(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }

    /**
     * Returns the formatted name of an ItemStack
     */
    public String getFullDisplayName(ItemStack stack) {
        if(!isStackThisType(stack)) {
            return  stack.getType().name();
        }

        return nameFormatting + getUnformattedDisplayName(stack);
    }
    public String getUnformattedDisplayName(ItemStack stack) {
        if(!isStackThisType(stack)) {
            return stack.getType().name();
        }

        return displayName;
    }

    //EVENTS
    public void onTryPlace(BlockPlaceEvent e, ItemStack stack) {}

    public void onHeld(PlayerItemHeldEvent e, ItemStack stack) {
        updateStack(stack);
    }
}
