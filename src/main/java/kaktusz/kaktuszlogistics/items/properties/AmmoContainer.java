package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import org.bukkit.NamespacedKey;

public class AmmoContainer extends ItemProperty {
	public static NamespacedKey AMMO_COUNT_KEY;

	private int maxAmmo;

	public AmmoContainer(CustomItem item, int maxAmmo) {
		super(item);
		setMaxAmmo(maxAmmo);
	}

	public AmmoContainer setMaxAmmo(int maxAmmo) {
		this.maxAmmo = maxAmmo;
		return this;
	}


}
