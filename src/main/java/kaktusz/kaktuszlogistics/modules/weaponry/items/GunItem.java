package kaktusz.kaktuszlogistics.modules.weaponry.items;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.modules.weaponry.input.ITriggerHeldListener;
import kaktusz.kaktuszlogistics.modules.weaponry.input.PlayerReloadManager;
import kaktusz.kaktuszlogistics.modules.weaponry.input.PlayerContinuousShootingManager;
import kaktusz.kaktuszlogistics.modules.weaponry.input.PlayerTriggerHeldEvent;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.modules.weaponry.items.nbt.AmmoContainerNBT;
import kaktusz.kaktuszlogistics.modules.weaponry.items.nbt.AmmoContainerPDT;
import kaktusz.kaktuszlogistics.modules.weaponry.items.properties.ammo.AmmoContainer;
import kaktusz.kaktuszlogistics.modules.weaponry.items.properties.ammo.AmmoType;
import kaktusz.kaktuszlogistics.projectile.CustomProjectile;
import kaktusz.kaktuszlogistics.projectile.ProjectileManager;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import kaktusz.kaktuszlogistics.util.minecraft.SFXCollection;
import kaktusz.kaktuszlogistics.util.minecraft.SoundEffect;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class GunItem extends CustomItem implements ITriggerHeldListener {
	public static NamespacedKey LAST_SHOOT_TIME_KEY;
	public static NamespacedKey LOADED_MAG_KEY;

	public static final ChatColor LORE_COLOUR = ChatColor.GRAY;

	private int shootDelay = 2; //ticks between each shot
	private float muzzleVelocity = 5; //base muzzle velocity
	private float dispersion = 2f; //base dispersion after 50 blocks

	private int reloadTime = 30; //ticks to reload gun
	private final Set<AmmoType> validAmmo = new HashSet<>();
	private final List<String> validAmmoLore = new ArrayList<>();

	private SFXCollection shootSounds = new SFXCollection(
			new SoundEffect(Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 4, 4, 0.875f, 0.925f),
			new SoundEffect(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.6f, 0.6f, 1.75f, 1.85f),
			new SoundEffect(Sound.BLOCK_PISTON_CONTRACT, 0.6f, 1.5f));

	private SFXCollection reloadSFX = new SFXCollection(
			new SoundEffect(Sound.BLOCK_IRON_DOOR_OPEN, 0.7f, 1.85f)
	);
	private SFXCollection reloadFailedSFX = new SFXCollection(
			new SoundEffect(Sound.BLOCK_IRON_TRAPDOOR_OPEN, 0.5f, 1.5f)
	);
	private SFXCollection unloadSFX = new SFXCollection(
			new SoundEffect(Sound.BLOCK_IRON_DOOR_CLOSE, 0.45f, 1.625f)
	);


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

	public GunItem setReloadTime(int reloadTime) {
		this.reloadTime = reloadTime;
		return this;
	}

	public GunItem addValidAmmoType(AmmoType ammoType) {
		validAmmo.add(ammoType);
		addValidAmmoLore(ammoType);
		return this;
	}
	public GunItem removeValidAmmoType(AmmoType ammoType) {
		validAmmo.remove(ammoType);
		regenerateValidAmmoLore();
		return this;
	}

	public GunItem setShootSounds(SFXCollection shootSounds) {
		this.shootSounds = shootSounds;
		return this;
	}

	public GunItem setReloadSounds(SFXCollection reloadSFX, SFXCollection reloadFailedSFX, SFXCollection unloadSFX) {
		this.reloadSFX = reloadSFX;
		this.reloadFailedSFX = reloadFailedSFX;
		this.unloadSFX = unloadSFX;
		return this;
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
			AmmoContainerNBT mag = getLoadedMag(stack);
			if(mag == null)
				loadGun(e.getPlayer(), stack);
			else if(mag.ammoCount <= 0)
				reloadFailedSFX.playAll(e.getPlayer().getEyeLocation().add(e.getPlayer().getEyeLocation().getDirection()));
			else
				triggerDown(e.getPlayer(), stack);
			e.setCancelled(true);
		}
		else if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(e.getPlayer().isSneaking()) {
				reload(e, stack);
				e.setCancelled(true);
			}
		}
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
		if(PlayerReloadManager.isPlayerReloading(player))
			return;

		if(VanillaUtils.getTickTime() - getLastShootTime(stack) < shootDelay) //still in cooldown
			return;

		if(consumeLoadedMagAmmo(stack)) {
			shoot(player, stack);
		}
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
		AmmoContainerNBT mag = getLoadedMag(stack);
		CustomProjectile proj = mag.getAmmoContainerProperty().spawnProjectile(mag, player.getWorld(), pos, vel.multiply(getMuzzleVelocity(stack)));
		proj.setIgnoreEntity(player, 2);
		proj.setOwner(player);
		ProjectileManager.spawnProjectile(proj);

		//play sound 1m in front of player
		shootSounds.playAll(player.getEyeLocation().add(forward));

		setLastShootTime(stack, VanillaUtils.getTickTime());
	}

	/**
	 * Loads/unloads the gun appropriately
	 */
	private void reload(PlayerInteractEvent e, ItemStack stack) {
		if(PlayerReloadManager.isPlayerReloading(e.getPlayer()))
			return;

		Player p = e.getPlayer();
		if(getLoadedMag(stack) != null)
			unloadGun(p, stack);
		else
			loadGun(p, stack);
	}

	private void loadGun(Player p, ItemStack stack) {
		if(PlayerReloadManager.isPlayerReloading(p))
			return;

		//search player hotbar for valid magazine
		ItemStack[] inv = p.getInventory().getContents();
		for(int i = 8; i >= 0; i--) { //look through hotbar, back to front
			ItemStack item = inv[i];
			CustomItem ci = CustomItem.getFromStack(item);
			if(ci == null) //not a CustomItem
				continue;
			AmmoContainer ac = ci.findProperty(AmmoContainer.class);
			if(ac == null) //not an AmmoContainer
				continue;
			if(!validAmmo.contains(ac.getType())) //not a valid AmmoType
				continue;
			AmmoContainerNBT mag = ac.getAmmoContainerData(item.getItemMeta());
			if(mag.ammoCount <= 0) //empty magazine
				continue;

			//else, successfully found valid magazine!
			setLoadedMag(stack, mag); //load new magazine into weapon
			PlayerReloadManager.startReload(p, reloadTime); //mark player as reloading
			//do slowness effect & audio:
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, reloadTime, 1, true, false, false));
			playReloadSounds(p);
			//consume magazine item
			item.setAmount(item.getAmount() - 1);
			return;
		}
		reloadFailedSFX.playAll(p.getEyeLocation().add(p.getEyeLocation().getDirection()));
	}
	private void unloadGun(Player p, ItemStack stack) {
		if(PlayerReloadManager.isPlayerReloading(p))
			return;

		removeLoadedMag(stack, p.getInventory());
		unloadSFX.playAll(p.getEyeLocation().add(p.getEyeLocation().getDirection()));
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

	//DISPLAY
	@Override
	public String getDisplayName(ItemStack stack) {
		AmmoContainerNBT mag = getLoadedMag(stack);
		if(mag == null || mag.ammoCount <= 0) {
			return super.getDisplayName(stack) + ChatColor.GRAY + " (Empty)";
		}
		return super.getDisplayName(stack);
	}

	@Override
	protected void modifyLore(List<String> baseLore, ItemStack stack) {
		AmmoContainerNBT mag = getLoadedMag(stack);
		if(mag == null) {
			baseLore.add(LORE_COLOUR + "No ammunition loaded. Valid types:");
			baseLore.addAll(validAmmoLore);
		}
		else {
			baseLore.add(LORE_COLOUR + "[" + mag.containerItem.displayName + "]");
			mag.getAmmoContainerProperty().modifyLore(baseLore, mag);
		}
	}

	/**
	 * Adds an ammunition type to the cached valid ammo lore
	 */
	private void addValidAmmoLore(AmmoType newValidAmmoType) {
		validAmmoLore.add(LORE_COLOUR + " - " + newValidAmmoType.getName());
	}
	/**
	 * Re-generates lore listing all the valid ammunition and caches it.
	 */
	private void regenerateValidAmmoLore() {
		validAmmoLore.clear();
		for(AmmoType validAmmoType : validAmmo) {
			addValidAmmoLore(validAmmoType);
		}
	}

	//LOADED MAGAZINE
	private AmmoContainerNBT getLoadedMag(ItemStack stack) {
		return readNBT(stack, LOADED_MAG_KEY, AmmoContainerPDT.AMMO_CONTAINER_DATA);
	}

	private void setLoadedMag(ItemStack stack, AmmoContainerNBT ammoContainerData) {
		setNBT(stack, LOADED_MAG_KEY, AmmoContainerPDT.AMMO_CONTAINER_DATA, ammoContainerData);
		updateItemLore(stack);
		updateDisplayName(stack);
	}

	private void removeLoadedMag(ItemStack stack, Inventory returnInventory) {
		//take mag out of gun
		AmmoContainerNBT mag = getLoadedMag(stack);
		if(mag == null)
			return;
		setLoadedMag(stack, null);

		//give mag to inventory
		ItemStack magStack = mag.createStack();
		if(magStack == null)
			return;

		VanillaUtils.addItemsOrDrop(returnInventory, magStack);
	}
	private void removeLoadedMag(ItemStack stack, Location dropLocation) {
		//take mag out of gun
		AmmoContainerNBT mag = getLoadedMag(stack);
		if(mag == null)
			return;
		setLoadedMag(stack, null);

		//drop mag on ground
		ItemStack magStack = mag.createStack();
		if(magStack == null)
			return;

		if(dropLocation.getWorld() != null)
			dropLocation.getWorld().dropItemNaturally(dropLocation, magStack);
	}

	/**
	 * Consumes 1 ammo from the given GunItem stack's loaded magazine
	 * @return True if the magazine was not empty and ammunition was successfully consumed
	 */
	private boolean consumeLoadedMagAmmo(ItemStack stack) {
		AmmoContainerNBT mag = getLoadedMag(stack);
		if(mag == null)
			return false;
		if(mag.consumeAmmo()) {
			setLoadedMag(stack, mag);
			return true;
		}
		return false;
	}

	//HELPER
	private void playReloadSounds(Player p) {
		reloadSFX.playAll(p.getEyeLocation().add(p.getEyeLocation().getDirection())); //first sound
		if(reloadTime >= 5) {
			new BukkitRunnable() { //second sound
				@Override
				public void run() {
					if (p.isOnline())
						reloadSFX.playAll(p.getEyeLocation().add(p.getEyeLocation().getDirection()));
				}
			}.runTaskLater(KaktuszLogistics.INSTANCE, reloadTime-4);
		}
		new BukkitRunnable() { //third sound
			@Override
			public void run() {
				if(p.isOnline())
					reloadSFX.playAll(p.getEyeLocation().add(p.getEyeLocation().getDirection()));
			}
		}.runTaskLater(KaktuszLogistics.INSTANCE, reloadTime);
	}
}
