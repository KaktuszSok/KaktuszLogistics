package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.items.events.IHeldListener;
import kaktusz.kaktuszlogistics.items.events.IPlacedListener;
import kaktusz.kaktuszlogistics.items.events.IUseListener;
import kaktusz.kaktuszlogistics.items.properties.ItemEnchants;
import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.items.properties.ItemProperty;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class CustomItem implements IHeldListener, IUseListener, IPlacedListener {
	public static NamespacedKey TYPE_KEY;

	public final String type; //internal name of item
	public final String displayName; //base name of item seen by players
	public final Material material; //vanilla item that represents this

	private String nameFormatting = ChatColor.RESET.toString();
	private List<String> lore = new ArrayList<>();

	private final List<ItemProperty> properties = new ArrayList<>();

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

	//PROPERTIES
	/**
	 * Gets the previously added property of this type or adds one if it didn't exist
	 */
	public <P extends ItemProperty> P getOrAddProperty(Class<P> propertyType) {
		P prop = findProperty(propertyType);
		if(prop != null)
			return prop;
		//property wasn't added yet - add one
		try {
			prop = propertyType.getConstructor(CustomItem.class).newInstance(this);
			prop.onAdded();
			properties.add(prop);
			return prop;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return Whether the property existed on this CustomItem in the first place
	 */
	public <P extends ItemProperty> boolean removeProperty(Class<P> propertyType) {
		P prop = findProperty(propertyType);
		if(prop == null)
			return false;

		properties.remove(prop);
		return true;
	}

	@SuppressWarnings("unchecked") //using addProperty should guarantee the type casts are safe
	public <P extends ItemProperty> P findProperty(Class<P> propertyType) {
		for(ItemProperty p : getAllProperties()) {
			if(propertyType.isInstance(p)) {
				return (P)p;
			}
		}
		return null;
	}

	/**
	 * @return All the properties that have been added to this CustomItem
	 */
	public Collection<ItemProperty> getAllProperties() {
		return properties;
	}

	//common property shortcuts
	public CustomItem allowPlacement() {
		getOrAddProperty(ItemPlaceable.class);

		return this;
	}

	public CustomItem addEnchantment(Enchantment enchantment, int level) {
		ItemEnchants e = getOrAddProperty(ItemEnchants.class);
		e.addEnchantment(enchantment, level);

		return this;
	}

	//ITEMSTACK
	public ItemStack createStack(int amount) {
		ItemStack stack = createStackEarly(amount);
		for(ItemProperty p : getAllProperties()) {
			p.onCreateStack(stack);
		}
		updateStack(stack);
		return stack;
	}

	/**
	 * Initialise the stack but don't run updateStack()
	 */
	private ItemStack createStackEarly(int amount) {
		ItemStack stack = new ItemStack(material, amount);
		addTypeNBT(stack);

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
		//properties
		for(ItemProperty p : getAllProperties()) {
			p.onUpdateStack(stack);
		}

		//display
		updateDisplay(stack);
	}

	protected void updateDisplay(ItemStack stack) {
		if(stack.getType() != material) {
			stack.setType(material);
		}
		updateDisplayName(stack);
		updateItemLore(stack);
	}

	/**
	 * Adds an NBT tag to an ItemStack to identify it as a particular type of CustomItem
	 */
	private void addTypeNBT(ItemStack stack) {
		setNBT(stack, TYPE_KEY, PersistentDataType.STRING, type);
	}
	public boolean isStackThisType(ItemStack stack) {
		String type = readNBT(stack, TYPE_KEY, PersistentDataType.STRING);
		return type != null && type.equals(this.type);
	}

	//for items:
	public static <T,Z> void setNBT(ItemStack stack, NamespacedKey key, PersistentDataType<T, Z> dataType, Z data) {
		if(stack == null) return;

		ItemMeta meta = stack.getItemMeta();
		setNBT(meta, key, dataType, data);
		stack.setItemMeta(meta);
	}
	public static <T,Z> Z readNBT(ItemStack stack, NamespacedKey key, PersistentDataType<T,Z> dataType) {
		if(stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		return readNBT(meta, key, dataType);
	}

	//for blocks:
	public static <T,Z> void setNBT(ItemMeta meta, NamespacedKey key, PersistentDataType<T,Z> dataType, Z data) {
		if(meta == null) return;
		if(data == null)
			meta.getPersistentDataContainer().remove(key);
		else
			meta.getPersistentDataContainer().set(key, dataType, data);
	}
	public static <T,Z> Z readNBT(ItemMeta meta, NamespacedKey key, PersistentDataType<T,Z> dataType) {
		if(meta == null) return null;

		if(meta.getPersistentDataContainer().has(key, dataType)) {
			return meta.getPersistentDataContainer().get(key, dataType);
		}

		return null;
	}

	/**
	 * Updates the display name of an ItemStack so that it matches this custom item
	 */
	protected final void updateDisplayName(ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;
		meta.setDisplayName(getFullDisplayName(stack));
		stack.setItemMeta(meta);
	}

	/**
	 * Sets the lore of an ItemStack so that it matches this custom item
	 */
	protected final void updateItemLore(ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;
		meta.setLore(getItemLore(stack));
		stack.setItemMeta(meta);
	}

	//DISPLAY
	/**
	 * Returns the formatted and modified name of an ItemStack
	 */
	public final String getFullDisplayName(ItemStack stack) {
		if(!isStackThisType(stack)) {
			return stack.getType().name();
		}

		//apply property modifications
		String dispName = getDisplayName(stack);
		for(ItemProperty prop : getAllProperties()) {
			dispName = prop.modifyDisplayName(dispName, stack);
		}

		return nameFormatting + dispName;
	}

	/**
	 * @return The (optionally formatted) display name of the ItemStack, before any properties modify it
	 */
	protected String getDisplayName(ItemStack stack) {
		return displayName;
	}

	/**
	 * Changes the lore that should be given to a particular ItemStack.
	 * This is applied before any of the item's properties modify the lore.
	 * @param baseLore The current state of the lore of the item. Modify this to change the resulting lore.
	 */
	protected void modifyLore(List<String> baseLore, ItemStack stack) {

	}

	/**
	 * Returns the lore that should be given to a particular ItemStack
	 */
	public List<String> getItemLore(ItemStack stack) {
		List<String> modifiedLore = new ArrayList<>(lore);
		modifyLore(modifiedLore, stack);
		for(ItemProperty prop : getAllProperties()) {
			prop.modifyLore(modifiedLore, stack);
		}

		return modifiedLore;
	}

	//EVENTS
	@Override
	public void onHeld(PlayerItemHeldEvent e, ItemStack stack) {
		updateStack(stack);
	}

	@Override
	public void onTryUse(PlayerInteractEvent e, ItemStack stack) {
		if (e.getAction() == Action.LEFT_CLICK_BLOCK)
			return; //allow

		if(findProperty(ItemPlaceable.class) == null || e.getAction() != Action.RIGHT_CLICK_BLOCK)
			e.setUseItemInHand(Event.Result.DENY);
	}

	@Override
	public void onTryUseEntity(PlayerInteractEntityEvent e, ItemStack stack) {
		e.setCancelled(true);
	}

	@Override
	public void onTryPlace(BlockPlaceEvent e, ItemStack stack) {
		if(findProperty(ItemPlaceable.class) == null) {
			e.setCancelled(true);
		}
	}
}
