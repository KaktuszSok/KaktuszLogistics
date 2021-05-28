package kaktusz.kaktuszlogistics.modules.weaponry.items.properties.ammo;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.modules.weaponry.items.nbt.AmmoContainerNBT;
import kaktusz.kaktuszlogistics.modules.weaponry.items.nbt.AmmoContainerPDT;
import kaktusz.kaktuszlogistics.items.properties.ItemProperty;
import kaktusz.kaktuszlogistics.projectile.CustomProjectile;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;

public abstract class AmmoContainer extends ItemProperty {

	public static NamespacedKey AMMO_CONTAINER_KEY;

	private AmmoType type;
	private int maxAmmo = 10;
	private boolean disappearWhenEmpty = false;

	//SETUP
	public AmmoContainer(CustomItem item) {
		super(item);
	}

	public AmmoContainer setType(AmmoType type) {
		this.type = type;
		return this;
	}

	public AmmoContainer setMaxAmmo(int maxAmmo) {
		this.maxAmmo = maxAmmo;
		return this;
	}

	public AmmoContainer setDisappearWhenEmpty(boolean disappearWhenEmpty) {
		this.disappearWhenEmpty = disappearWhenEmpty;
		return this;
	}

	//GETTERS
	public AmmoType getType() {
		return type;
	}

	public int getMaxAmmo() {
		return maxAmmo;
	}

	//ITEMSTACK
	public ItemStack createStackFromData(AmmoContainerNBT data) {
		if(disappearWhenEmpty && data.ammoCount <= 0)
			return null;
		ItemStack stack = item.createStack(1);
		ItemMeta meta = stack.getItemMeta();
		setAmmoContainerData(meta, data);
		stack.setItemMeta(meta);
		item.updateStack(stack);
		return stack;
	}

	@Override
	public void onUpdateStack(ItemStack stack) {
		if(CustomItem.readNBT(stack, AMMO_CONTAINER_KEY, AmmoContainerPDT.AMMO_CONTAINER_DATA) == null) {
			ItemMeta meta = stack.getItemMeta();
			setAmmoContainerData(meta, new AmmoContainerNBT(this));
			stack.setItemMeta(meta);
		}
	}

	//DISPLAY
	@Override
	public String modifyDisplayName(String currName, ItemStack item) {
		if(getAmmoContainerData(item.getItemMeta()).ammoCount <= 0)
			return currName + ChatColor.GRAY + " (Empty)";
		return currName;
	}

	@Override
	public void modifyLore(List<String> lore, ItemStack item) {
		modifyLore(lore, getAmmoContainerData(item.getItemMeta()));
	}

	public void modifyLore(List<String> lore, AmmoContainerNBT data) {
		lore.add(type.getName() + ChatColor.GRAY + " (" + data.ammoCount + "/" + maxAmmo + ")");
	}

	//NBT
	/**
	 * Reads the ammo container data from the ItemMeta's NBT
	 */
	public AmmoContainerNBT getAmmoContainerData(ItemMeta meta) {
		AmmoContainerNBT ammoContainerData = CustomItem.readNBT(meta, AMMO_CONTAINER_KEY, AmmoContainerPDT.AMMO_CONTAINER_DATA);
		if(ammoContainerData == null) {
			ammoContainerData = new AmmoContainerNBT(this);
			setAmmoContainerData(meta, ammoContainerData);
		}

		return ammoContainerData;
	}

	public void setAmmoContainerData(ItemMeta meta, AmmoContainerNBT ammoContainerData) {
		CustomItem.setNBT(meta, AMMO_CONTAINER_KEY, AmmoContainerPDT.AMMO_CONTAINER_DATA, ammoContainerData);
	}

	//ACTIONS
	public abstract CustomProjectile spawnProjectile(AmmoContainerNBT ammoContainerData, World world, Vector pos, Vector vel);
}
