package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.items.events.ITriggerHeldListener;
import kaktusz.kaktuszlogistics.items.events.input.PlayerContinuousShootingManager;
import kaktusz.kaktuszlogistics.items.events.input.PlayerTriggerHeldEvent;
import kaktusz.kaktuszlogistics.projectiles.BulletProjectile;
import kaktusz.kaktuszlogistics.projectiles.CustomProjectile;
import kaktusz.kaktuszlogistics.projectiles.ProjectileManager;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import kaktusz.kaktuszlogistics.util.SFXCollection;
import kaktusz.kaktuszlogistics.util.SoundEffect;
import kaktusz.kaktuszlogistics.util.VanillaUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

@SuppressWarnings("unused")
public class GunItem extends CustomItem implements ITriggerHeldListener {
	public static NamespacedKey LAST_SHOOT_TIME_KEY;

	private int shootDelay = 2; //ticks between each shot
	private float muzzleVelocity = 5; //base muzzle velocity
	private float dispersion = 2f; //base dispersion after 50 blocks

	private SFXCollection shootSounds = new SFXCollection(
			new SoundEffect(Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 4, 4, 0.875f, 0.925f),
			new SoundEffect(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.6f, 0.6f, 1.75f, 1.85f),
			new SoundEffect(Sound.BLOCK_PISTON_CONTRACT, 0.6f, 1.5f));

	//SETUP
	public GunItem(String type, String displayName, Material material) {
		super(type, displayName, material);
		shootSounds.setCategory(SoundCategory.PLAYERS);
	}

	public GunItem setShootDelay(int shootDelay) {
		this.shootDelay = shootDelay;
		return this;
	}

	public GunItem setMuzzleVel(float muzzleVelocity) {
		this.muzzleVelocity = muzzleVelocity;
		return this;
	}

	public GunItem setDispersion(float dispersion) {
		this.dispersion = dispersion;
		return this;
	}

	public void setShootSounds(SFXCollection shootSounds) {
		this.shootSounds = shootSounds;
	}

	//GETTERS
	/**
	 * @return Muzzle velocity in blocks per tick, calculated for the provided itemStack
	 */
	protected float getMuzzleVelocity(ItemStack stack) {
		return muzzleVelocity;
	}

	/**
	 * @return Dispersion in blocks per 50m, calculated for the provided itemStack
	 */
	protected float getDispersion(ItemStack stack) {
		return dispersion;
	}

	//EVENTS
	@Override
	public void onTryUse(PlayerInteractEvent e, ItemStack stack) {
		e.setUseItemInHand(Event.Result.DENY);

		if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getType().isInteractable() && !e.getPlayer().isSneaking()) {
			return; //dont shoot if interacting with a block
		}

		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			triggerDown(e.getPlayer(), stack);
		}
		else if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(e.getPlayer().isSneaking()) {
				toggleADS(e, stack);
			}
			else {
				reload(e, stack);
			}
		}
		e.setCancelled(true);
	}

	@Override
	public void onTryUseEntity(PlayerInteractEntityEvent e, ItemStack stack) {
		super.onTryUseEntity(e, stack);

		triggerDown(e.getPlayer(), stack);
	}

	@Override
	public void onTriggerHeld(PlayerTriggerHeldEvent e, ItemStack stack) {
		tryShoot(e.getPlayer(), stack);
	}

	//ACTIONS
	private void triggerDown(Player player, ItemStack stack) {
		PlayerContinuousShootingManager.pullTrigger(player);
	}

	private void tryShoot(Player player, ItemStack stack) {
		if(VanillaUtils.getTickTime() - getLastShootTime(stack) < shootDelay) //still in cooldown
			return;

		shoot(player, stack);
	}

	private void shoot(Player player, ItemStack stack) {
		Vector pos = player.getEyeLocation().toVector();
		Vector forward = player.getEyeLocation().getDirection();

		//calculate inaccuracy angle
		float disp = (float)Math.atan2(getDispersion(stack), 50);

		//calculate final velocity
		Vector vel = forward.clone().multiply(getMuzzleVelocity(stack));
		vel.rotateAroundX(MathsUtils.randomRange(-disp, disp));
		vel.rotateAroundY(MathsUtils.randomRange(-disp, disp));
		vel.rotateAroundZ(MathsUtils.randomRange(-disp, disp));
		vel.add(player.getVelocity());

		//spawn projectile
		CustomProjectile proj = new BulletProjectile(player.getWorld(), pos, vel.multiply(getMuzzleVelocity(stack)));
		proj.setIgnoreEntity(player, 2);
		proj.setOwner(player);
		ProjectileManager.spawnProjectile(proj);

		//play sound 1m in front of player
		shootSounds.playAll(player.getEyeLocation().add(forward));

		setLastShootTime(stack, VanillaUtils.getTickTime());
	}

	/**
	 * toggles aiming down sights
	 */
	private void toggleADS(PlayerInteractEvent e, ItemStack stack) {

	}

	private void reload(PlayerInteractEvent e, ItemStack stack) {

	}

	//ITEMSTACK
	private long getLastShootTime(ItemStack stack) {
		Long lastShootTime = readNBT(stack, LAST_SHOOT_TIME_KEY, PersistentDataType.LONG);
		if(lastShootTime == null)
			return 0;
		return lastShootTime;
	}

	private void setLastShootTime(ItemStack stack, long time) {
		setNBT(stack, LAST_SHOOT_TIME_KEY, PersistentDataType.LONG, time);
	}
}
