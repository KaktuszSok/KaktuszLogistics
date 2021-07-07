package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.modules.survival.KaktuszSurvival;
import kaktusz.kaktuszlogistics.util.StringUtils;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.*;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;

/**
 * A sign that detects a house if placed next to a door
 */
public class HouseSignBlock extends CustomBlock implements LabourSupplier {
	private static final long serialVersionUID = 100L;

	private transient Sign signCache;
	private HouseInfo houseInfoCache;

	private final Map<BlockPosition, Double> labourSupplied = new HashMap<>();

	public HouseSignBlock(PlaceableHouseSign prop, Location location, ItemMeta meta) {
		super(prop, location, meta);
	}

	//BEHAVIOUR
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

		Location location = getLocation();
		BlockPosition selfPos = new BlockPosition(location);
		deregisterAsLabourSupplier(chunk, selfPos);

		//un-claim door:
		BlockPosition door = getDoor();
		if(door == null)
			return; //no door to unclaim. If the door changed after being marked then some leftover data will be left but this will be cleaned up eventually.
		KLChunk doorChunk = KLWorld.get(location.getWorld()).getChunkAt(VanillaUtils.blockToChunkCoord(door.x), VanillaUtils.blockToChunkCoord(door.z));
		if(doorChunk == null)
			return;

		HashMap<BlockPosition, BlockPosition> houseDoors = doorChunk.getExtraData("houseDoors");
		if(houseDoors == null)
			return;

		houseDoors.remove(door, selfPos); //door becomes unclaimed
		if(houseDoors.isEmpty())
			doorChunk.setExtraData("houseDoors", null); //clear map from chunk data if it becomes empty
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {
		if(e.getPlayer().isSneaking()) {
			double labourSupplied = 0;

			KLWorld world = KLWorld.get(getLocation().getWorld());
			StringBuilder consumersList = new StringBuilder();
			for (Map.Entry<BlockPosition, Double> entry : new HashSet<>(getLabourConsumers().entrySet())) {
				BlockPosition pos = entry.getKey();
				CustomBlock block = world.getBlockAt(pos.x, pos.y, pos.z);
				if(!(block instanceof LabourConsumer)) { //bad data
					getLabourConsumers().remove(pos);
					continue;
				}
				LabourConsumer consumer = (LabourConsumer) block;
				labourSupplied += consumer.getRequiredLabour();
				if(consumer.getTier() > getLabourTier() || labourSupplied > getLabourPerDay()) { //bad data
					getLabourConsumers().remove(pos);
					consumer.validateAndFixSupply();
					labourSupplied -= consumer.getRequiredLabour();
				}
				consumersList.append("\n - ")
						.append(StringUtils.formatDouble(entry.getValue())) //labour/day
						.append(" labour/day (T").append(consumer.getTier()) //labour tier
						.append(") to ").append(block.getType().item.displayName) //name
						.append(" at ").append(entry.getKey()); //position
			}
			int consumerAmt = getLabourConsumers().size();
			if(consumerAmt > 0) {
				String messageHeader =
						ChatColor.GRAY + "This house provides " + StringUtils.formatDouble(labourSupplied)
								+ " labour/day to " + consumerAmt + (consumerAmt == 1 ? " consumer:" : " consumers:");
				e.getPlayer().sendMessage(messageHeader + consumersList);
			} else {
				e.getPlayer().sendMessage(ChatColor.GRAY + "This house is not providing labour to any consumers.");
			}
			refreshText(false);
			return;
		}

		refreshText(true);

		if(houseInfoCache == null) {
			e.getPlayer().sendMessage(ChatColor.GRAY + "Could not detect a house. Possible reasons for failure:" +
					"\n1. The entrance room is too big." +
					"\n2. The sign is not placed on a wall next to the top half of a door." +
					"\n3. The door is blocked.");
			breakBlock(true);
		}
	}

	//VALIDATION
	/**
	 * Checks if the house is valid and updates the houseInfoCache and sign text accordingly
	 */
	private void recheckHouse() {
		Sign state = getState();
		if(state == null) { //not valid sign
			setHouseInfoCache(null);
			update();
			return;
		}

		BlockPosition houseStart = findHouseOrigin();
		if(houseStart == null) {
			onHouseRecheckFinished(null);
			return;
		}

		Location location = getLocation();
		if(KaktuszSurvival.CALC_ROOMS_ASYNC.getValue()) {
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
		setHouseInfoCache(result);
		refreshText(false);

		//(de)registration
		Location location = getLocation();
		KLWorld world = KLWorld.get(location.getWorld());
		KLChunk signChunk = world.getOrCreateChunkAt(VanillaUtils.blockToChunkCoord(location.getBlockX()), VanillaUtils.blockToChunkCoord(location.getBlockZ()));
		BlockPosition selfPos = new BlockPosition(location);
		if(houseInfoCache == null) {
			deregisterAsLabourSupplier(signChunk, selfPos);
			return;
		}
		if(getTotalLabourSupplied() > getLabourPerDay()) { //(momentarily) de-register if new labour supply is lower than demand
			deregisterAsLabourSupplier(signChunk, selfPos);
		}
		registerAsLabourSupplier(signChunk, selfPos);

		//pop off any intersecting house signs
		for (BlockPosition door : houseInfoCache.getAllDoors()) {
			KLChunk doorChunk = world.getChunkAt(VanillaUtils.blockToChunkCoord(door.x), VanillaUtils.blockToChunkCoord(door.z));
			if(doorChunk == null)
				continue;
			HashMap<BlockPosition, BlockPosition> houseDoors = doorChunk.getExtraData("houseDoors");
			if(houseDoors == null)
				continue;
			BlockPosition doorSign = houseDoors.get(door); //the position of the sign which the encountered door is assigned to
			if(doorSign == null || doorSign.equals(selfPos))
				continue;

			CustomBlock block = world.getBlockAt(doorSign.x, doorSign.y, doorSign.z);
			if(!(block instanceof HouseSignBlock) || !block.update()) { //bad data
				houseDoors.remove(door);
				continue;
			}

			block.breakBlock(true); //pop off house sign
		}

		//claim door:
		BlockPosition door = getDoor();
		if(door == null) { //shouldn't ever happen since houseInfoCache is not null
			breakBlock(true);
			KaktuszLogistics.LOGGER.warning("Could not find door for house sign w/ valid house. This should never happen.");
			return;
		}

		KLChunk doorChunk = world.getOrCreateChunkAt(VanillaUtils.blockToChunkCoord(door.x), VanillaUtils.blockToChunkCoord(door.z));
		HashMap<BlockPosition, BlockPosition> houseDoors = doorChunk.getExtraData("houseDoors");
		if(houseDoors == null) {
			houseDoors = new HashMap<>();
			doorChunk.setExtraData("houseDoors", houseDoors);
		}

		houseDoors.put(door, selfPos);
	}

	//LABOUR
	@Override
	public Map<BlockPosition, Double> getLabourConsumers() {
		return labourSupplied;
	}

	/**
	 * How many units of labour can this house supply per day
	 */
	public double getLabourPerDay() {
		if(houseInfoCache == null)
			return 0;
		return houseInfoCache.getMaxPopulation() * 8;
	}

	/**
	 * What tier of machines can the labourers of this house operate
	 */
	public int getLabourTier() {
		if(houseInfoCache == null)
			return 0;
		return houseInfoCache.getTier();
	}

	//HELPERS
	/**
	 * Checks if the sign is placed on a wall next to the top part of a door and, if so, returns the origin of the potential house
	 * @return null if no door could be found, otherwise the position of the block behind the bottom half of the door
	 */
	private BlockPosition findHouseOrigin() {
		WallSign wallSign = getWallSign();
		if(wallSign == null) return null;

		Location location = getLocation();
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
	 * Tries to find the top half of a door next to the wall that the sign is placed on.
	 * @return null if no door could be found, otherwise the position of the bottom half of the door.
	 */
	private BlockPosition getDoor() {
		WallSign wallSign = getWallSign();
		if(wallSign == null) return null;

		Location location = getLocation();
		BlockFace dir = wallSign.getFacing().getOppositeFace(); //direction towards the wall
		if(Tag.DOORS.isTagged(location.getBlock().getRelative(dir.getModX() + dir.getModZ(), 0, dir.getModZ() - dir.getModX()).getType())) { //check block to the left of the wall
			return new BlockPosition(location.getBlockX() + dir.getModX() + dir.getModZ(), location.getBlockY()-1, location.getBlockZ() + dir.getModZ() - dir.getModX());
		}
		if(Tag.DOORS.isTagged(location.getBlock().getRelative(dir.getModX() - dir.getModZ(), 0, dir.getModZ() + dir.getModX()).getType())) { //check block to the right of the wall
			return new BlockPosition(location.getBlockX() + dir.getModX() - dir.getModZ(), location.getBlockY()-1, location.getBlockZ() + dir.getModZ() + dir.getModX());
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

		BlockState data = getLocation().getBlock().getState();
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
	 * Gets the most recently calculated HouseInfo for this house
	 */
	public HouseInfo getHouseInfoCache() {
		return houseInfoCache;
	}

	private void setHouseInfoCache(HouseInfo newValue) {
		houseInfoCache = newValue;
	}

	//DISPLAY
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
