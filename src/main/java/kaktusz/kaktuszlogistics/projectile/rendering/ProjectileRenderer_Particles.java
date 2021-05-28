package kaktusz.kaktuszlogistics.projectile.rendering;

import kaktusz.kaktuszlogistics.projectile.CustomProjectile;
import org.bukkit.Particle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ProjectileRenderer_Particles extends ProjectileRenderer<CustomProjectile> {

	private final Particle flightParticle = Particle.CRIT_MAGIC;
	private double spacing = 1.5f;
	private int amount = 1;
	private float randomX = 0;
	private float randomY = 0;
	private float randomZ = 0;
	private float speed = 0;

	private Particle impactParticle = Particle.CRIT;
	private int impactParticleAmount = 3;

	private double accumOffset = 0f;

	//SETUP
	public ProjectileRenderer_Particles(CustomProjectile projectile) {
		super(projectile);
		accumOffset = spacing; //so that we dont spawn one right at our shooter's eyes
	}

	public ProjectileRenderer_Particles setProperties(float spacing, int amount) {
		this.spacing = spacing;
		this.amount = amount;

		return this;
	}
	public ProjectileRenderer_Particles setProperties(float randomX, float randomY, float randomZ, float speed) {
		this.randomX = randomX;
		this.randomY = randomY;
		this.randomZ = randomZ;
		this.speed = speed;

		return this;
	}

	public ProjectileRenderer_Particles setImpactProperties(Particle particle, int amount) {
		impactParticle = particle;
		impactParticleAmount = amount;

		return this;
	}

	@Override
	public void onTick(Vector prevPosition) {
		//spawn particles every "spacing" blocks
		double distTravelled = prevPosition.distance(projectile.getPos());
		Vector unitStep = projectile.getVel().normalize();
		while(accumOffset < distTravelled) {
			//1. spawn particles
			Vector currPos = prevPosition.clone().add(unitStep.clone().multiply(accumOffset));
			spawnParticles(currPos);

			//2. add to accumulated offset
			accumOffset += spacing;
		}
		accumOffset -= distTravelled; //3. once we exceed the distance the projectile travelled this tick, we subtract it from the accumulated distance.
	}

	/**
	 * Spawns flight particles at a specified position
	 */
	public void spawnParticles(Vector pos) {
		spawnParticles(pos, flightParticle, amount);
	}
	public void spawnParticles(Vector pos, Particle particle, int amount) {
		projectile.getWorld().spawnParticle(particle, pos.getX(), pos.getY(), pos.getZ(), amount, randomX, randomY, randomZ, speed, null, true);
	}

	@Override
	public void onImpact(RayTraceResult hit) {
		super.onImpact(hit);
		Vector pos = projectile.getPos();
		spawnParticles(pos, impactParticle, impactParticleAmount);
	}

	@Override
	public void onDestroyed() {

	}

	@Override
	public void despawn() {

	}
}
