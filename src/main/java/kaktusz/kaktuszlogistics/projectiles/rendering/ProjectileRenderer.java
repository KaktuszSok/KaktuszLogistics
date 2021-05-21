package kaktusz.kaktuszlogistics.projectiles.rendering;

import kaktusz.kaktuszlogistics.projectiles.CustomProjectile;
import kaktusz.kaktuszlogistics.util.VanillaUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public abstract class ProjectileRenderer<P extends CustomProjectile> {

	protected final P projectile;

	public ProjectileRenderer(P projectile) {
		this.projectile = projectile;
	}

	/**
	 * Rendering for flight of projectile
	 * @param prevPosition The position of this projectile at the start of this tick
	 */
	public abstract void onTick(Vector prevPosition);

	/**
	 * Rendering for any impact, whether it destroys the projectile or not.
	 */
	public void onImpact(RayTraceResult hit) {
		Sound s = null;
		float vol = 1f;
		float pitch = 1f;
		if(hit.getHitBlock() != null) {
			s = VanillaUtils.getBlockSound(hit.getHitBlock(), VanillaUtils.BlockSounds.BREAK);
			vol = VanillaUtils.getBlockSFXVolume(hit.getHitBlock());
			pitch = VanillaUtils.getBlockSFXPitch(hit.getHitBlock())*1.7f;
		}
		else if(hit.getHitEntity() != null) {
			s = Sound.ENTITY_BEE_STING;
			vol = 0.85f;
			pitch = 1.35f;
		}

		if(s == null)
			return;
		projectile.getWorld().playSound(hit.getHitPosition().toLocation(projectile.getWorld()), s, vol, pitch);
	}

	/**
	 * Rendering for when the particle is physically destroyed, through an impact or otherwise.
	 */
	public abstract void onDestroyed();

	/**
	 * Cleans up the particle rendering once the projectile ceases to exist.
	 */
	public abstract void despawn();
}
