package kaktusz.kaktuszlogistics.modules.weaponry.blocks;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.modules.weaponry.KaktuszWeaponry;
import kaktusz.kaktuszlogistics.modules.weaponry.items.properties.PlaceableProximityMine;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.ExplodableBlock;
import kaktusz.kaktuszlogistics.world.KLWorld;
import kaktusz.kaktuszlogistics.world.TickingBlock;
import kaktusz.kaktuszlogistics.world.multiblock.CustomSupportedBlock;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;

public class ProximityMineBlock extends CustomSupportedBlock implements TickingBlock, ExplodableBlock {
	private static final long serialVersionUID = 100L;

	/**
	 * How often the mine checks for nearby entities, in ticks
	 */
	private static final int checkFrequency = 7;
	private int offset;

	public ProximityMineBlock(PlaceableProximityMine prop, Location location, ItemMeta meta) {
		super(prop, location, meta);
	}

	@Override
	public void onTick() {
		if(VanillaUtils.getTickTime() % checkFrequency == offset) {
			if(checkForNearbyEntities())
				detonate();
		}
	}

	@Override
	public void onSet(KLWorld world, int x, int y, int z) {
		super.onSet(world, x, y, z);
		offset = (int) ((VanillaUtils.getTickTime() - 1) % checkFrequency);
	}

	@Override
	public void onExploded(float yield) {
		breakBlock(false);
		Bukkit.getScheduler().runTaskLater(KaktuszLogistics.INSTANCE, this::createExplosion, 4);
	}

	@SuppressWarnings("ConstantConditions")
	private boolean checkForNearbyEntities() {
		Collection<Entity> nearbyEntities = getLocation().getWorld().getNearbyEntities(
				getLocation().clone().add(0.5d, 0.5d, 0.5d), 0.5d, 0.5d, 0.5d,
				entity -> entity instanceof LivingEntity
						&& !entity.isInvulnerable()
						&& (
							entity instanceof Player && ((Player) entity).getGameMode() != GameMode.SPECTATOR
							|| KaktuszWeaponry.MOBS_TRIGGER_LANDMINES.getValue() && ((LivingEntity) entity).hasAI()
						));

		return !nearbyEntities.isEmpty();
	}

	public void detonate() {
		KLWorld world = KLWorld.get(getLocation().getWorld());
		world.runAtEndOfTick(() -> {
			if(!update())
				return;
			breakBlock(false);
			createExplosion();
		});
	}

	@SuppressWarnings("ConstantConditions")
	private void createExplosion() {
		getLocation().getWorld().createExplosion(getLocation().clone().add(0.5d, 0.5d, 0.5d),
				getType().getExplosionPower());
	}

	@Override
	public PlaceableProximityMine getType() {
		return (PlaceableProximityMine) super.getType();
	}

	@Override
	public BlockFace getSupportedFace() {
		return getFacing().getOppositeFace();
	}
}
