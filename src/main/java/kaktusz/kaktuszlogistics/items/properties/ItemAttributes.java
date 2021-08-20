package kaktusz.kaktuszlogistics.items.properties;

import com.google.common.collect.Multimap;
import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.util.ArrayUtils;
import kaktusz.kaktuszlogistics.util.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Gives certain attributes to all items with this property
 */
public class ItemAttributes extends ItemProperty {

	private static class AttributeTuple {
		public final Attribute attribute;
		public final AttributeModifier modifier;
		private AttributeTuple(Attribute attribute, AttributeModifier modifier) {
			this.attribute = attribute;
			this.modifier = modifier;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AttributeTuple that = (AttributeTuple) o;
			return attribute == that.attribute && modifier.equals(that.modifier) && modifier.getSlot() == that.modifier.getSlot();
		}

		@Override
		public int hashCode() {
			return Objects.hash(attribute, modifier.getOperation(), modifier.getSlot());
		}
	}
	public static NamespacedKey DEFAULT_ATTRIBS_KEY;
	private static final int BASE_DAMAGE = 1;
	private static final double BASE_ATTACK_SPEED = 4;

	private final Set<AttributeTuple> attributes = new HashSet<>();
	private boolean hideAttributes = false;
	private EquipmentSlot defaultEquipmentSlot = EquipmentSlot.HAND;

	//SETUP
	public ItemAttributes(CustomItem item) {
		super(item);
	}

	/**
	 * Set the equipment slot that attribute modifiers will be added to if not specified.
	 * By default, the default equipment slot is EquipmentSlot.HAND
	 */
	public ItemAttributes setDefaultEquipmentSlot(EquipmentSlot newDefaultEquipmentSlot) {
		defaultEquipmentSlot = newDefaultEquipmentSlot;

		return this;
	}

	/**
	 * Adds an attribute modifier for a given attribute on the default equipment slot. Overwrites modifiers with the same operation on the same attribute and equipment slot.
	 */
	public ItemAttributes addAttribute(Attribute attribute, double amount, AttributeModifier.Operation operation) {
		return addAttribute(attribute, amount, operation, defaultEquipmentSlot);
	}
	/**
	 * Adds an attribute modifier for a given attribute. Overwrites modifiers with the same operation on the same attribute and equipment slot.
	 */
	public ItemAttributes addAttribute(Attribute attribute, double amount, AttributeModifier.Operation operation, EquipmentSlot slot) {
		String modifierName = attribute.name() + "-" + slot.name() + "-" + operation.name();
		return addAttribute(attribute, new AttributeModifier(
						UUID.nameUUIDFromBytes(modifierName.getBytes(StandardCharsets.UTF_8)),
						modifierName, amount, operation, slot));
	}

	/**
	 * Adds an attribute modifier for a given attribute. Overwrites modifiers with the same operation on the same attribute and equipment slot.
	 */
	public ItemAttributes addAttribute(Attribute attribute, AttributeModifier modifier) {
		attributes.add(new AttributeTuple(attribute, modifier));

		return this;
	}

	/**
	 * @param hideAttributes True to hide the attributes of this item in its lore, false to display them. False by default.
	 */
	public ItemAttributes setHideAttributes(boolean hideAttributes) {
		this.hideAttributes = hideAttributes;

		return this;
	}

	/**
	 * Sets the base damage of items with this property
	 */
	public ItemAttributes setDamage(int damage) {
		addAttribute(Attribute.GENERIC_ATTACK_DAMAGE, damage - BASE_DAMAGE, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
		return this;
	}
	/**
	 * Sets the base attack speed of items with this property
	 */
	public ItemAttributes setAttackSpeed(double attackSpeed) {
		addAttribute(Attribute.GENERIC_ATTACK_SPEED, attackSpeed - BASE_ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
		return this;
	}
	/**
	 * Sets the base defence of items with this property when equipped on the default equipment slot
	 */
	public ItemAttributes setDefence(double defence) {
		addAttribute(Attribute.GENERIC_ARMOR, defence, AttributeModifier.Operation.ADD_NUMBER, defaultEquipmentSlot);
		return this;
	}

	//ITEM
	@Override
	public void onUpdateStack(ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		Validate.notNull(meta);

		//remove old default attributes and apply new ones
		removeDefaultAttributes(meta, meta.getPersistentDataContainer().get(DEFAULT_ATTRIBS_KEY, PersistentDataType.STRING));
		for (AttributeTuple tuple : attributes) {
			meta.addAttributeModifier(tuple.attribute, tuple.modifier);
		}
		//save default attributes to nbt
		meta.getPersistentDataContainer().set(DEFAULT_ATTRIBS_KEY, PersistentDataType.STRING, getDefaultAttributeIdentifiers());

		//hide default attribute lore
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

		stack.setItemMeta(meta);
	}

	@Override
	public void modifyLore(List<String> lore, ItemStack item) {
		if(hideAttributes)
			return;

		ItemMeta meta = item.getItemMeta();
		Validate.notNull(meta);
		Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
		if(modifiers == null || modifiers.size() == 0)
			return;
		if(lore.size() > 0)
			lore.add("");
		lore.add(ChatColor.GRAY + "Stats:");
		List<String> statsLore = new ArrayList<>();
		List<String> priorityStatsLore = new ArrayList<>(); //appears before other stats
		for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
			addAttributeLore(statsLore, priorityStatsLore, entry.getKey(), entry.getValue());
		}
		lore.addAll(priorityStatsLore);
		lore.addAll(statsLore);
	}

	private void addAttributeLore(List<String> statsLore, List<String> priorityStatsLore, Attribute attribute, AttributeModifier modifier) {
		//PRIORITY STATS
		if(modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER && modifier.getSlot() == EquipmentSlot.HAND) {
			String amountString;
			switch (attribute) {
				case GENERIC_ATTACK_SPEED:
					amountString = StringUtils.formatDouble(modifier.getAmount() + BASE_ATTACK_SPEED);
					priorityStatsLore.add(ChatColor.GRAY + " - "
							+ ChatColor.GOLD + "Attack Speed: "+ ChatColor.GRAY + amountString + " Hits/sec");
					return;
				case GENERIC_ATTACK_DAMAGE:
					amountString = StringUtils.formatDouble((modifier.getAmount() + BASE_DAMAGE)/2);
					priorityStatsLore.add(0, ChatColor.GRAY + " - "
							+ ChatColor.GOLD + "Damage: "+ ChatColor.GRAY + amountString + " Hearts");
					return;
				default:
					break;
			}
		}

		//NORMAL STATS
		//figure out name
		String[] attributeNameWords = attribute.toString().split("_");
		attributeNameWords = ArrayUtils.subarray(attributeNameWords, 1);
		String attributeName = StringUtils.fixCapitalisation(attributeNameWords).replace("Armor", "Armour");
		String modifierSuffix = "";
		String attributePrefix = "";
		String attributeSuffix = "";
		switch (modifier.getOperation()) {
			case ADD_NUMBER:
				attributeSuffix = " Raw";
				break;
			case ADD_SCALAR:
				modifierSuffix = "%";
				break;
			case MULTIPLY_SCALAR_1:
				attributePrefix = "Final ";
				modifierSuffix = "%";
				break;
		}

		//figure out colour
		ChatColor attributeColour;
		int merit = (int)Math.signum(modifier.getAmount()*getAttributeMerit(attribute));
		switch (merit) {
			case 1:
				attributeColour = ChatColor.GREEN;
				break;
			case -1:
				attributeColour = ChatColor.RED;
				break;
			case 0:
			default: //shouldn't ever happen
				attributeColour = ChatColor.BLUE;
				break;
		}

		//figure out amount
		double amount = modifier.getAmount();
		if(modifierSuffix.equals("%")) {
			amount *= 100d; //display as percentage, not fraction
		}
		String modifierString = StringUtils.formatDouble(amount) + modifierSuffix;
		if(modifier.getAmount() >= 0)
			modifierString = "+" + modifierString;

		String line = ChatColor.GRAY + " " +
				modifierString + " " +
				attributeColour + attributePrefix + attributeName + attributeSuffix;
		statsLore.add(line);
	}

	//NBT
	/**
	 * Generates a string containing the necessary information to identify the default attributes.
	 * That is, the attribute type and the UUID of the attribute modifier.
	 */
	private String getDefaultAttributeIdentifiers() {
		StringJoiner data = new StringJoiner(" | ");
		for (AttributeTuple tuple : attributes) {
			data.add(tuple.attribute.name() + ":" + tuple.modifier.getUniqueId().toString());
		}
		return data.toString();
	}

	private void removeDefaultAttributes(ItemMeta meta, String defaultAttributeIdentifiers) {
		if(defaultAttributeIdentifiers == null)
			return;

		String[] identifiers = defaultAttributeIdentifiers.split(Pattern.quote(" | "));
		for (String identifier : identifiers) {
			String[] attrib_and_uuid = identifier.split(Pattern.quote(":"));
			if(attrib_and_uuid.length != 2) {
				KaktuszLogistics.LOGGER.warning("Failed reading default attribute on item. Invalid formatting for identifier: " + identifier);
				continue;
			}

			Attribute attribute;
			UUID uuid;
			try {
				attribute = Attribute.valueOf(attrib_and_uuid[0]);
			}
			catch (IllegalArgumentException e) {
				KaktuszLogistics.LOGGER.warning("Failed reading default attribute on item. Invalid attribute name: " + attrib_and_uuid[0]);
				continue;
			}

			try {
				uuid = UUID.fromString(attrib_and_uuid[1]);
			}
			catch (IllegalArgumentException e) {
				KaktuszLogistics.LOGGER.warning("Failed reading default attribute on item. Invalid UUID: " + attrib_and_uuid[1]);
				continue;
			}

			meta.removeAttributeModifier(attribute, new AttributeModifier(uuid, "x", 0, AttributeModifier.Operation.ADD_NUMBER));
		}
	}

	//HELPER

	/**
	 * Get the merit value of a given attribute. 1 = good, 0 = neutral, -1 = bad
	 */
	public static int getAttributeMerit(Attribute attribute) {
		switch (attribute) {
			//GOOD
			case GENERIC_MAX_HEALTH:
			case GENERIC_KNOCKBACK_RESISTANCE:
			case GENERIC_MOVEMENT_SPEED:
			case GENERIC_FLYING_SPEED:
			case GENERIC_ATTACK_DAMAGE:
			case GENERIC_ATTACK_SPEED:
			case GENERIC_ARMOR:
			case GENERIC_ARMOR_TOUGHNESS:
			case GENERIC_LUCK:
			case HORSE_JUMP_STRENGTH:
			case ZOMBIE_SPAWN_REINFORCEMENTS:
				return 1;

			//BAD
			//<empty>

			//NEUTRAL
			case GENERIC_ATTACK_KNOCKBACK:
			case GENERIC_FOLLOW_RANGE:
			default:
				return 0;
		}
	}
}
