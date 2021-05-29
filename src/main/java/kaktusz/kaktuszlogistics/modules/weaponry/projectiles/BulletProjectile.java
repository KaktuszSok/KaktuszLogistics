package kaktusz.kaktuszlogistics.modules.weaponry.projectiles;

import kaktusz.kaktuszlogistics.modules.weaponry.KaktuszWeaponry;
import kaktusz.kaktuszlogistics.projectile.CustomProjectile;
import kaktusz.kaktuszlogistics.projectile.rendering.ProjectileRenderer_Particles;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class BulletProjectile extends CustomProjectile {

	private final static double MAX_DIST_THROUGH_BLOCK = Math.sqrt(3);

	//SETTINGS
	private float damage = 3f;
	private float maxPenetration = 1.5f;
	private String killMessage = KaktuszWeaponry.INSTANCE.getShotMessage();

	//RUNTIME
	private final double startSpeed;
	private float penetration;

	//SETUP
	public BulletProjectile(World world, Vector pos, Vector vel) {
		this(world, pos, vel, true);
	}
	public BulletProjectile(World world, Vector pos, Vector vel, boolean useDefaultRenderer) {
		super(world, pos, vel);
		if(useDefaultRenderer)
			setRenderer(new ProjectileRenderer_Particles(this));

		startSpeed = vel.length();
		penetration = maxPenetration;
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}

	public void setMaxPenetration(float pen) {
		penetration = maxPenetration = pen;
	}

	public void setKillMessage(String killMessage) {
		this.killMessage = killMessage;
	}

	//EVENTS
	@Override
	protected boolean onCollideBlock(RayTraceResult hit, Block block) {
		float penLost = block.getType().getBlastResistance();

		//raytrace in the opposite direction, so that we know at which point we exit the block
		Vector vel = getVel();
		Vector hitPos = hit.getHitPosition(); //where we enter the block
		Vector endPos = hitPos.clone().add(vel);
		double distanceThroughBlock;
		//params: from where we will end up at the end of the tick, in the opposite direction that we are travelling, for length that we travel
		RayTraceResult otherSideHit = block.getBoundingBox().rayTrace(endPos, new Vector(0,0,0).subtract(vel), vel.length());
		if(otherSideHit != null) {
			distanceThroughBlock = hitPos.distance(otherSideHit.getHitPosition());
		}
		else { //didnt emerge from block
			distanceThroughBlock = hitPos.distance(endPos);
		}

		penLost *= Math.min(distanceThroughBlock, MAX_DIST_THROUGH_BLOCK);

		return losePenetration(penLost);
	}

	@Override
	protected boolean onCollideEntity(RayTraceResult hit, Entity entity) {
		if(entity instanceof LivingEntity) {
			damageEntity((LivingEntity)entity);
		}

		return losePenetration(1.0f);
	}

	//ACTIONS
	private void damageEntity(LivingEntity entity) {
		if(entity.getHealth() <= 0)
			return;

		float effectiveDamage = damage;

		effectiveDamage *= (penetration / maxPenetration); //reduce damage as pen drops
		effectiveDamage *= getVel().length() / startSpeed; //reduce damage as velocity drops

		if(getOwner() == null)
			entity.damage(effectiveDamage);
		else
			VanillaUtils.damageEntity(entity, getOwner(), Math.round(effectiveDamage), killMessage);
		entity.setNoDamageTicks(0);
	}

	/**
	 * @return True if the bullet is destroyed
	 */
	private boolean losePenetration(float amountLost) {
		penetration -= amountLost;
		if(penetration <= 0) {
			destroy();
			return true;
		}
		return false;
	}
}
