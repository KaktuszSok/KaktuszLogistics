package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.modules.survival.KaktuszSurvival;
import kaktusz.kaktuszlogistics.util.StringUtils;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.CustomBlock;
import kaktusz.kaktuszlogistics.world.KLChunk;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.NumberFormat;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;

/**
 * A sign that detects a house if placed next to a door
 */
public class HouseSignBlock extends CustomBlock {

	private transient Sign signCache;
	private transient HouseInfo houseInfoCache;

	public HouseSignBlock(PlaceableHouseSign prop, Location location, ItemMeta meta) {
		super(prop, location, meta);
	}

	@Override
	public ItemStack getDrop(Block block) {
		return new ItemStack(block.getDrops().iterator().next()); //drop sign
	}

	@Override
	public void onSet(KLWorld world, int x, int y, int z) {
		Bukkit.getScheduler().runTaskLater(KaktuszLogistics.INSTANCE, this::recheckHouse, 1);
	}

	@Override
	public void onRemoved(KLWorld world, int x, int y, int z) {
		KLChunk chunk = world.getChunkAt(VanillaUtils.blockToChunkCoord(x), VanillaUtils.blockToChunkCoord(z));
		if(chunk == null) return;

		chunk.removeFromExtraDataSet("houses", new BlockPosition(location));
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {
		refreshText(true);

		if(houseInfoCache == null) {
			e.getPlayer().sendMessage(ChatColor.GRAY + "Could not detect a house. Possible reasons for failure:" +
					"\n1. The entrance room is too big." +
					"\n2. The sign is not placed on a wall next to the top half of a door." +
					"\n3. The door is blocked.");
			breakBlock(true);
		}
	}

	public double getLabourPerDay() {
		if(houseInfoCache == null)
			return 0;
		return houseInfoCache.getMaxPopulation() * 8;
	}

	public int getLabourTier() {
		if(houseInfoCache == null)
			return 0;
		return houseInfoCache.getTier();
	}

	/**
	 * Checks if the house is valid and updates the houseInfoCache and sign text accordingly
	 */
	private void recheckHouse() {
		houseInfoCache = null;

		Sign state = getState();
		if(state == null) { //not valid sign
			update();
			return;
		}

		BlockPosition houseStart = checkForDoor();
		if(houseStart == null)
			return;

		if(KaktuszSurvival.CALC_ROOMS_ASYNC.value) {
			new BukkitRunnable() {
				@Override
				public void run() {
					HouseInfo result = HouseInfo.calculateHouse(location.getWorld(), houseStart); //get result async
					Bukkit.getScheduler().runTask(KaktuszLogistics.INSTANCE, () -> onHouseRecheckFinished(result)); //once it's done, sync back with main thread
				}
			}.runTaskAsynchronously(KaktuszLogistics.INSTANCE);
		}
		else {
			onHouseRecheckFinished(HouseInfo.calculateHouse(location.getWorld(), houseStart));
		}
	}

	private void onHouseRecheckFinished(HouseInfo result) {
		houseInfoCache = result;
		if(houseInfoCache == null)
			return;

		KLChunk chunk = KLWorld.get(location.getWorld()).getOrCreateChunkAt(VanillaUtils.blockToChunkCoord(location.getBlockX()), VanillaUtils.blockToChunkCoord(location.getBlockZ()));
		chunk.getOrCreateExtraDataSet("houses").add(new BlockPosition(location)); //register with chunk
		//TODO pop off interfering house signs

		refreshText(false);
	}

	/**
	 * Checks if the sign is placed on a wall next to the top part of a door
	 * @return null if no door could be found, otherwise the position of the block behind the bottom half of the door
	 */
	private BlockPosition checkForDoor() {
		WallSign wallSign = getWallSign();
		if(wallSign == null) return null;

		BlockFace dir = wallSign.getFacing().getOppositeFace(); //direction towards the wall
		if(Tag.DOORS.isTagged(location.getBlock().getRelative(dir.getModX() + dir.getModZ(), 0, dir.getModZ() - dir.getModX()).getType())) { //check block to the left of the wall
			return new BlockPosition(location.getBlockX() + dir.getModX()*2 + dir.getModZ(), location.getBlockY()-1, location.getBlockZ() + dir.getModZ()*2 - dir.getModX());
		}
		if(Tag.DOORS.isTagged(location.getBlock().getRelative(dir.getModX() - dir.getModZ(), 0, dir.getModZ() + dir.getModX()).getType())) { //check block to the right of the wall
			return new BlockPosition(location.getBlockX() + dir.getModX()*2 - dir.getModZ(), location.getBlockY()-1, location.getBlockZ() + dir.getModZ()*2 + dir.getModX());
		}

		return null;
	}

	/**
	 * Gets the state of the sign
	 * @return The state of the sign or null if something is wrong
	 */
	private Sign getState() {
		if(signCache != null)
			return signCache;

		BlockState data = location.getBlock().getState();
		if(!(data instanceof Sign) || !(data.getBlockData() instanceof WallSign))
			return null;

		return signCache = (Sign)data;
	}

	/**
	 * Gets the block data of the sign
	 * @return The block data of the sign or null if something is wrong
	 */
	private WallSign getWallSign() {
		Sign state = getState();
		if(state == null)
			return null;

		return (WallSign)state.getBlockData();
	}

	/**
	 * @param showNextPage If true, the displayed page will advance to the next page
	 */
	private void refreshText(boolean showNextPage) {
		Sign sign = getState();
		if(sign == null)
			return;

		String line0 = sign.getLine(0);
		int page = 1; //1 = page 1/3, 2 = page 2/3, 3 = page 3/3
		if(line0.contains("2/3"))
			page = 2;
		else if(line0.contains("3/3"))
			page = 3;


		showPage(showNextPage ? (page % 3) + 1 : page);
	}

	/**
	 * @param page The page to show (1, 2 or 3)
	 */
	private void showPage(int page) {
		if(houseInfoCache == null) {
			updateSignToInvalid();
			return;
		}

		Sign sign = getState();
		if(sign == null)
			return;

		int labourTier = getLabourTier();
		if(labourTier > 0)
			sign.setLine(0, "House " + ChatColor.DARK_GRAY + "[" + page + "/3]");
		else {
			sign.setLine(0, ChatColor.DARK_RED + "House [" + page + "/3]");
		}

		String formatting;
		switch (page) {
			case 1:
				int rooms = houseInfoCache.rooms.size();
				sign.setLine(1, rooms + (rooms == 1 ? " Room" : " Rooms"));

				int beds = houseInfoCache.getTotalBeds();
				formatting = beds == 0 ? ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH : "";
				sign.setLine(2, formatting + beds + (beds == 1 ? " Bed " : " Beds"));
				break;
			case 2:
				int floorArea = houseInfoCache.getTotalFloorArea();
				formatting = floorArea < 6 ? ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH : "";
				sign.setLine(1, formatting + NumberFormat.getInstance().format(floorArea) + "m^2");

				int population = houseInfoCache.getMaxPopulation();
				formatting = population == 0 ? ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH : "";
				sign.setLine(2, formatting + "Population: " + NumberFormat.getInstance().format(population));
				break;
			case 3:
				formatting = labourTier == 0 ? ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH : "";
				sign.setLine(1, formatting + "Labour: " + StringUtils.formatDouble(getLabourPerDay()) + "/d");
				sign.setLine(2, formatting + "Labour Tier: " + labourTier);
				break;
			default:
				break;
		}
		sign.setLine(3, ChatColor.DARK_GRAY + "[Right-Click]");

		sign.update(false, false);
	}

	/**
	 * Updates the sign so that it displays a message informing the reader that the house is invalid
	 */
	private void updateSignToInvalid() {
		Sign state = getState();
		if(state == null) { //very bad
			update();
			return;
		}

		state.setLine(0, ChatColor.DARK_RED + "House Invalid!");
		state.setLine(1, ChatColor.DARK_GRAY + "[Right-Click]");
		state.setLine(2, "");
		state.setLine(3, "");
		state.update(false, false);
	}
}
