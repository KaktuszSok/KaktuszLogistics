package kaktusz.kaktuszlogistics.projectile;

import kaktusz.kaktuszlogistics.projectile.rendering.ProjectileRenderer;
import kaktusz.kaktuszlogistics.util.minecraft.DDABlockIterator;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class CustomProjectile {

	private static class PrioritisedRayHit implements Comparable<PrioritisedRayHit> {
		private final double distanceSqr;
		public final RayTraceResult hit;

		public PrioritisedRayHit(Vector startPos, RayTraceResult hit) {
			this.hit = hit;
			distanceSqr = startPos.distanceSquared(hit.getHitPosition());
		}


		@Override
		public int compareTo(PrioritisedRayHit o) {
			if(this.distanceSqr == o.distanceSqr)
				return 0;
			return this.distanceSqr < o.distanceSqr ? -1 : 1; //if we are closer, we want to be nearer to the head of the priority queue
		}
	}

	private static final int Y_MINIMUM = -64;
	private static final int Y_MAXIMUM = 320;

	private Entity owner;
	//POSITION
	private final World world;
	private Vector pos;
	@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"}) //kinda misleading to label it final
	private Vector vel;

	//STATE
	private int lifetime = 5*20; //lifetime in ticks
	private Entity ignoredEntity;
	private int ignoreEntityTicks = 0;
	private final Set<Entity> hitEntities = new HashSet<>();

	//SETTINGS
	@SuppressWarnings("FieldCanBeLocal")
	private final float drag = 0.99f; //per tick
	private final Vector gravity = new Vector(0, -0.05f, 0); //per tick
	private ProjectileRenderer<?> renderer;

	//SETUP
	public CustomProjectile(World world, Vector pos, Vector vel) {
		this.world = world;
		this.pos = pos;
		this.vel = vel;
	}

	//GETTERS & SETTERS
	public Entity getOwner() {
		return owner;
	}
	public void setOwner(Entity owner) {
		this.owner = owner;
	}

	public World getWorld() {
		return world;
	}

	/**
	 * @return A clone of the current position
	 */
	public Vector getPos() {
		return pos.clone();
	}

	/**
	 * @return A clone of the current velocity
	 */
	public Vector getVel() {
		return vel.clone();
	}

	public int getLifetime() {
		return lifetime;
	}

	/**
	 * Sets the ignored entity and how long it will be ignored for
	 */
	public void setIgnoreEntity(Entity entityToIgnore, int ticks) {
		ignoredEntity = entityToIgnore;
		ignoreEntityTicks = ticks;
	}

	public ProjectileRenderer<?> getRenderer() {
		return renderer;
	}

	public void setRenderer(ProjectileRenderer<?> newRenderer) {
		if(renderer != null)
			renderer.despawn();
		renderer = newRenderer;
	}

	//UPDATE
	public void onTick() {
		Vector prevPos = pos.clone();
		physicsStep();
		if(getRenderer() != null)
			getRenderer().onTick(prevPos);
		lifetime--;
	}

	private void physicsStep() {
		if(pos.getBlockY() < Y_MINIMUM) {
			despawn();
			return;
		}
		Vector nextPos = pos.clone().add(vel);
		if(pos.getBlockY() < Y_MAXIMUM || nextPos.getBlockY() < Y_MAXIMUM) {
			if(!world.isChunkLoaded(VanillaUtils.blockToChunkCoord(pos.getBlockX()), VanillaUtils.blockToChunkCoord(pos.getBlockZ()))) { //entered unloaded chunk
				despawn();
				return;
			}

			//COLLISION CHECK
			PriorityQueue<PrioritisedRayHit> rayHits = new PriorityQueue<>(); //blocks and entities which we hit, ordered by distance
			double velMagnitude = vel.length();

			//1. get all blocks we hit
			Iterator<Block> blockIterator = new DDABlockIterator(world, pos, nextPos, true, true);
			while (blockIterator.hasNext()) {
				Block b = blockIterator.next();

				//raytrace block
				RayTraceResult blockHit = b.getBoundingBox().rayTrace(pos, vel, velMagnitude);
				if (blockHit != null) { //hit the block
					blockHit = new RayTraceResult(blockHit.getHitPosition(), b, blockHit.getHitBlockFace()); //for whatever reason, we must assign block manually
					rayHits.add(new PrioritisedRayHit(pos, blockHit)); //add block to things we've hit
				}
			}

			//2. get all entities we hit
			Collection<Entity> entities = world.getNearbyEntities(BoundingBox.of(pos, nextPos)); //all entities between our start and end positions for this tick
			for (Entity e : entities) {
				if (hitEntities.contains(e)) {
					continue; //already hit this entity - skip
				}
				if (e == ignoredEntity) {
					continue; //we are currently ignoring this entity
				}
				RayTraceResult entityHit = e.getBoundingBox().rayTrace(pos, vel, velMagnitude);
				if (entityHit != null) { //hit the entity
					entityHit = new RayTraceResult(entityHit.getHitPosition(), e); //for whatever reason, we must assign entity manually
					rayHits.add(new PrioritisedRayHit(pos, entityHit)); //add entity to things we've hit
				}
			}
			if (ignoreEntityTicks > 0) { //reduce ignore entity timer, so that we do not ignore it forever
				ignoreEntityTicks--;
				if (ignoreEntityTicks == 0) {
					ignoredEntity = null;
				}
			}

			//3. go through everything we've hit in this step, from closest to furthest
			while (!rayHits.isEmpty()) {
				RayTraceResult hit = rayHits.poll().hit;

				if (hit.getHitEntity() != null) { //its an entity hit
					hitEntities.add(hit.getHitEntity());
					onCollideAnything(hit);
					if (onCollideEntity(hit, hit.getHitEntity())) { //Do entity collision logic. Returns true if we should terminate the physics step
						return;
					}
				} else if (hit.getHitBlock() != null) { //its a block hit
					onCollideAnything(hit);
					if (onCollideBlock(hit, hit.getHitBlock())) { //Do block collision logic. Returns true if we should terminate the physics step
						return;
					}
				}
			}
		}

		//POSITION & VELOCITY UPDATE
		pos = nextPos;
		vel.add(gravity);
		vel.multiply(drag);
	}

	//EVENTS
	protected void onCollideAnything(RayTraceResult hit) {
		pos = hit.getHitPosition(); //update position
		if(getRenderer() != null)
			getRenderer().onImpact(hit);
	}

	/**
	 * @return True if the physicsStep should cease
	 */
	protected boolean onCollideBlock(RayTraceResult hit, Block block) {
		destroy();
		return true;
	}

	/**
	 * @return True if the physicsStep should cease
	 */
	@SuppressWarnings("unused")
	protected boolean onCollideEntity(RayTraceResult hit, Entity entity) {
		destroy();
		return true;
	}

	//ACTIONS
	/**
	 * Called when the projectile should be destroyed
	 */
	public void destroy() {
		if(getRenderer() != null)
			getRenderer().onDestroyed();
		lifetime = 0;
	}

	/**
	 * Call to despawn the projectile (by setting its lifetime to zero)
	 */
	public final void despawn() {
		lifetime = 0;
	}

	/**
	 * Called when the projectile is removed from the active projectiles list
	 */
	public void onDespawned() {
		if(getRenderer() != null)
			getRenderer().despawn();
	}
}
