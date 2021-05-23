package kaktusz.kaktuszlogistics.items.properties.ammo;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.nbt.AmmoContainerNBT;
import kaktusz.kaktuszlogistics.projectiles.BulletProjectile;
import kaktusz.kaktuszlogistics.projectiles.CustomProjectile;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class BulletContainer extends AmmoContainer {

	private float damage = 3f;
	private float velocityMultiplier = 1f;

	public enum BulletType implements AmmoType {
		PISTOL_9mm("9mm Standard Pistol Round"),
		RIFLE_7_62x39mm("7.62x39mm Standard Rifle Round");

		private final String name;
		BulletType(String name) {
			this.name = name;
		}

		public String getName() {
			return ChatColor.GRAY + name;
		}
	}

	//SETUP
	public BulletContainer(CustomItem item) {
		super(item);
	}

	@Override
	public BulletContainer setType(AmmoType type) {
		super.setType(type);
		return this;
	}

	public BulletContainer setDamage(float damage) {
		this.damage = damage;
		return this;
	}

	public BulletContainer setVelocityMultiplier(float velocityMultiplier) {
		this.velocityMultiplier = velocityMultiplier;
		return this;
	}

	@Override
	public CustomProjectile spawnProjectile(AmmoContainerNBT ammoContainerData, World world, Vector pos, Vector vel) {
		BulletProjectile bullet = new BulletProjectile(world, pos, vel.multiply(velocityMultiplier));
		bullet.setDamage(damage);
		return bullet;
	}
}
