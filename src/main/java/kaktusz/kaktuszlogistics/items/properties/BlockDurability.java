package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import kaktusz.kaktuszlogistics.util.minecraft.SFXCollection;
import kaktusz.kaktuszlogistics.util.minecraft.SoundEffect;
import kaktusz.kaktuszlogistics.world.DurableBlock;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.TreeMap;

@SuppressWarnings("unused")
public class BlockDurability extends ItemPlaceable {
	public static NamespacedKey DURA_KEY;

	private enum DurabilityState {
		LOW (0.0f, ChatColor.RED.toString()),
		MID (0.3f, ChatColor.YELLOW.toString()),
		HIGH (0.75f, ChatColor.WHITE.toString());

		public final String colour;
		public final float minPercent;
		DurabilityState(float minPercent, String colour) {
			this.minPercent = minPercent;
			this.colour = colour;
		}
	}
	private static final TreeMap<Float, DurabilityState> STATE_MAP = new TreeMap<>();

	private int maxDurability = 3;
	public SFXCollection damageSound = new SFXCollection(
			new SoundEffect(Sound.BLOCK_ANVIL_LAND, 0.225f, 0.225f, 0.5f, 0.8f)
	);

	//SETUP
	public BlockDurability(CustomItem item) {
		super(item);

		STATE_MAP.put(DurabilityState.LOW.minPercent, DurabilityState.LOW);
		STATE_MAP.put(DurabilityState.MID.minPercent, DurabilityState.MID);
		STATE_MAP.put(DurabilityState.HIGH.minPercent, DurabilityState.HIGH);

		damageSound.setCategory(SoundCategory.BLOCKS);
	}

	public BlockDurability setMaxDurability(int maxDurability) {
		this.maxDurability = maxDurability;

		return this;
	}
	public int getMaxDurability() {
		return maxDurability;
	}

	public BlockDurability setDamageSound(SFXCollection sound) {
		damageSound = sound;
		damageSound.setCategory(SoundCategory.BLOCKS);
		return this;
	}

	@Override
	public void onAdded() {
		item.removeProperty(ItemPlaceable.class);
	}

	//ITEMSTACK
	@Override
	public void onCreateStack(ItemStack stack) {
		setPercent(stack, 1.0f, true);
	}

	@Override
	public void onUpdateStack(ItemStack stack) {
		if(stack.getItemMeta() == null)
			return;

		//set quality if item didn't have it before
		if(!stack.getItemMeta().getPersistentDataContainer().has(DURA_KEY, PersistentDataType.FLOAT)) {
			fixPercent(stack);
		}
	}

	//durability is stored as a percentage
	/**
	 * Called if the itemstack does not have durability info attached (e.g. after updating, where before it was not a durable item)
	 */
	private void fixPercent(ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		fixPercent(meta);
		stack.setItemMeta(meta);
	}

	public void setPercent(ItemStack stack, float percent) {
		setPercent(stack, percent, false);
	}
	public void setPercent(ItemStack stack, float percent, boolean dontUpdateStack) {
		ItemMeta meta = stack.getItemMeta();
		setPercent(meta, percent);
		stack.setItemMeta(meta);

		if(!dontUpdateStack)
			item.updateStack(stack);
	}
	public float getPercent(ItemStack stack) {
		if(stack.getItemMeta() == null)
			return 0;

		return getPercent(stack.getItemMeta());
	}

	public DurabilityState getState(ItemStack stack) {
		return getState(getPercent(stack));
	}
	public DurabilityState getState(float percent) {
		return STATE_MAP.floorEntry(percent).getValue();
	}

	//for blocks:
	public void fixPercent(ItemMeta meta) {
		setPercent(meta, 1.0f);
	}
	public void setPercent(ItemMeta meta, float percent) {
		CustomItem.setNBT(meta, DURA_KEY, PersistentDataType.FLOAT, percent);
	}
	public float getPercent(ItemMeta meta) {
		if(!meta.getPersistentDataContainer().has(DURA_KEY, PersistentDataType.FLOAT)) {
			fixPercent(meta);
		}
		return CustomItem.readNBT(meta, DURA_KEY, PersistentDataType.FLOAT);
	}

	//converting percent to durability
	public int getDurability(ItemStack stack) {
		return getDurability(stack.getItemMeta());
	}
	public int getDurability(ItemMeta meta) {
		float percent = getPercent(meta);
		return (int)Math.ceil(MathsUtils.lerp(0, maxDurability, percent));
	}

	public void takeDamage(ItemStack stack, int dmg) {
		takeDamage(stack.getItemMeta(), dmg);
	}
	public void takeDamage(ItemMeta meta, int dmg) {
		int dura = getDurability(meta);
		float targetPercent = MathsUtils.lerpInverseC(0, maxDurability, dura-dmg);
		setPercent(meta, targetPercent);
	}

	//DISPLAY
	@Override
	public void modifyLore(List<String> lore, ItemStack item) {
		DurabilityState state = getState(item);
		lore.add(state.colour + "Durability: " + getDurability(item));
	}

	//BLOCK
	@Override
	public DurableBlock createCustomBlock(ItemMeta stackMeta, Location location) {
		return new DurableBlock(this, location, stackMeta);
	}
}
