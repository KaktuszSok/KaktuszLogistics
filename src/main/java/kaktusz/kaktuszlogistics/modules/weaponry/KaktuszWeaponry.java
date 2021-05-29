package kaktusz.kaktuszlogistics.modules.weaponry;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.modules.KaktuszModule;
import kaktusz.kaktuszlogistics.modules.weaponry.items.properties.ammo.AmmoContainer;
import kaktusz.kaktuszlogistics.modules.weaponry.items.properties.ammo.BulletContainer;
import kaktusz.kaktuszlogistics.modules.weaponry.items.GunItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;

public class KaktuszWeaponry implements KaktuszModule {

	public static KaktuszWeaponry INSTANCE;

	public void initialise() {
		INSTANCE = this;

		initKeys();
		initItems();
	}

	private void initKeys() {
		GunItem.LAST_SHOOT_TIME_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "LastShootTime");
		GunItem.LOADED_MAG_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "LoadedMagazine");
		AmmoContainer.AMMO_CONTAINER_KEY = new NamespacedKey(KaktuszLogistics.INSTANCE, "AmmoContainer");
	}

	private void initItems() {
		CustomItemManager.registerItem(new GunItem("toolTestGun", "AK-47", Material.PRISMARINE_SHARD)
				.addValidAmmoType(BulletContainer.BulletType.RIFLE_7_62x39mm));
		CustomItemManager.registerItem(new CustomItem("ammoTestMag", "AK-47 Standard Magazine", Material.FLINT))
				.getOrAddProperty(BulletContainer.class)
				.setType(BulletContainer.BulletType.RIFLE_7_62x39mm)
				.setDamage(4f)
				.setMaxAmmo(30);
	}

	//CONFIG
	@Override
	public void addDefaultConfigs(FileConfiguration config) {
		KaktuszLogistics.INSTANCE.config.accessConfigDirectly().addDefault("weaponry.shotMessage", "%k was shot by %s");
	}

	/**
	 * Retrieves (from the config) the formatting-ready message of an entity being shot by another
	 */
	public String getShotMessage() {
		return KaktuszLogistics.INSTANCE.config.accessConfigDirectly().getString("weaponry.shotMessage");
	}

}
