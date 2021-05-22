package kaktusz.kaktuszlogistics.projectiles;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class ProjectileManager {

	private static final List<CustomProjectile> projectiles = new ArrayList<>();

	public static CustomProjectile spawnProjectile(CustomProjectile projectile) {
		projectiles.add(projectile);
		return projectile;
	}

	public static void onTick() {
		//tick all projectiles
		for(int i = 0; i < projectiles.size(); i++) {
			CustomProjectile p = projectiles.get(i);
			p.onTick();
			if(p.getLifetime() <= 0) {
				p.despawn();
				projectiles.remove(i);
				i--;
			}
		}
	}

	public static void despawnAll() {
		while(projectiles.size() > 0) {
			projectiles.get(0).despawn();
			projectiles.remove(0);
		}
	}

}