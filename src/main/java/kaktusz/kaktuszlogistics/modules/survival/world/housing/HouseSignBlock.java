package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.modules.survival.KaktuszSurvival;
import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import kaktusz.kaktuszlogistics.util.StringUtils;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.util.minecraft.config.IntegerOption;
import kaktusz.kaktuszlogistics.world.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;

/**
 * A sign that detects a house if placed next to a door
 */
public class HouseSignBlock extends CustomSignBlock implements LabourSupplier, TickingBlock {
	private static final long serialVersionUID = 100L;
	/**
	 * How often the house is re-checked, in seconds
	 */
	public static final IntegerOption HOUSE_RECHECK_FREQUENCY = new IntegerOption("survival.housing.houseRecheckFrequency", 20*60);

	private HouseInfo houseInfoCache;
	private long nextRecheckTime;
	private Map<BlockPosition, Double> labourSupplied = new HashMap<>();
	private int lastRecheckTier = 0;

	public HouseSignBlock(PlaceableHouseSign prop, Location location, ItemMeta meta) {
		super(prop, location, meta);
	}

	//BEHAVIOUR
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
	public void onLoaded() {
		if(labourSupplied == null) //bad data!
			labourSupplied = new HashMap<>();
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {
		if(e.getPlayer().isSneaking()) { //labour summary
			sendLabourSummary(e.getPlayer());
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

	@Override
	public void onTick() {
		//recheck house if it is time or if we are suspiciously far behind the recheck time (which ideally wouldn't happen)
		if(VanillaUtils.getTickTime() > nextRecheckTime || nextRecheckTime - VanillaUtils.getTickTime() > 20L*HOUSE_RECHECK_FREQUENCY.getValue()*2) {
			nextRecheckTime = VanillaUtils.getTickTime() + 20L*HOUSE_RECHECK_FREQUENCY.getValue();
			KLWorld.get(getLocation().getWorld()).runAtEndOfTick(this::recheckHouse);
		}
	}

	//VALIDATION
	/**
	 * Checks if the house is valid and updates the houseInfoCache and sign text accordingly
	 */
	private void recheckHouse() {
		nextRecheckTime = VanillaUtils.getTickTime() + 20L*HOUSE_RECHECK_FREQUENCY.getValue();
		if(!update())
			return;

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

		//(de)registration
		Location location = getLocation();
		KLWorld world = KLWorld.get(location.getWorld());
		KLChunk signChunk = world.getOrCreateChunkAt(VanillaUtils.blockToChunkCoord(location.getBlockX()), VanillaUtils.blockToChunkCoord(location.getBlockZ()));
		BlockPosition selfPos = new BlockPosition(location);
		deregisterAsLabourSupplier(signChunk, selfPos);
		if(houseInfoCache == null) {
			refreshText(false);
			return;
		}
		else {
			//consume goods based on tier
			for(lastRecheckTier = houseInfoCache.getTier(); lastRecheckTier > 0; lastRecheckTier--) {
				ItemIngredient[] goodsRequired = LabourTierRequirements.REQUIREMENTS.floorEntry(lastRecheckTier).getValue();
				if(goodsRequired == null)
					continue;

				List<ItemInput> accumulatedSupplies = new ArrayList<>();
				boolean supplied = ChunkSupplySystem.requestFromNearbyChunks(location, "goodsSuppliers",
						(Function<GoodsSupplySignBlock, Boolean>) supplier -> {
							accumulatedSupplies.addAll(Arrays.asList(supplier.getSupplies()));
							return GoodsSupplySignBlock.consumeGoodsAccumulated(goodsRequired, accumulatedSupplies, houseInfoCache.getMaxPopulation());
						}
				);

				if(supplied)
					break;
			}
		}
		registerAsLabourSupplier(signChunk, selfPos);
		refreshText(false);

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
		return Math.min(houseInfoCache.getTier(), lastRecheckTier);
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

	/**
	 * Sends a message to the player, listing the goods this house is consuming and the labour consumers this house is supplying
	 */
	private void sendLabourSummary(Player player) { //TODO colours
		double labourSupplied = 0;

		KLWorld world = KLWorld.get(getLocation().getWorld());
		StringBuilder fullMessage = new StringBuilder(ChatColor.GRAY + "-------- Labour Summary --------\n");
		//daily goods consumption
		StringBuilder goodsList = new StringBuilder();
		ItemIngredient[] goodsRequired = LabourTierRequirements.REQUIREMENTS.get(getLabourTier());
		if(goodsRequired == null)
			goodsRequired = new ItemIngredient[0];
		for(ItemIngredient req : goodsRequired) {
			goodsList.append("\n").append(ChatColor.GRAY).append(" - ").append(ChatColor.BOLD).append(req.getName())
					.append(ChatColor.GRAY).append(" x").append(req.amount*houseInfoCache.getMaxPopulation());
		}
		if(goodsRequired.length > 0) {
			fullMessage.append(ChatColor.GRAY).append("Every day, this house consumes:");
			fullMessage.append(goodsList);
			fullMessage.append("\n");
		}
		else if(houseInfoCache != null && getLabourTier() == 0 && houseInfoCache.getTier() > 0) {
			fullMessage.append(ChatColor.RED).append("This house is lacking supplies! ").append(ChatColor.GRAY).append("There must be a goods supplier with appropriate goods nearby. Place a sign with the text \"Goods\" on the first line to mark a chest as a goods supplier.");
			fullMessage.append("\n");
		}

		//labour consumers
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
				continue;
			}

			//add to summary
			consumersList.append(ChatColor.GRAY).append("\n - ")
					.append(ChatColor.BOLD).append(StringUtils.formatDouble(entry.getValue())) //labour/day
					.append(ChatColor.GRAY).append(" labour/day (T").append(consumer.getTier()) //labour tier
					.append(ChatColor.GRAY).append(") to ").append(ChatColor.BOLD).append(block.getType().item.displayName) //name
					.append(ChatColor.GRAY).append(" at ").append(entry.getKey()); //position


			//play particles
			world.world.spawnParticle(Particle.VILLAGER_HAPPY,
					new Location(world.world, pos.x+0.5f, pos.y+0.5f, pos.z+0.5f),
					15, 0.4d, 0.4d, 0.4d);
		}
		int consumerAmt = getLabourConsumers().size();
		if(consumerAmt > 0) {
			String consumersHeader =
					ChatColor.GRAY + "This house provides " + StringUtils.formatDouble(labourSupplied)
							+ " labour/day to " + consumerAmt + (consumerAmt == 1 ? " consumer:" : " consumers:");
			fullMessage.append(consumersHeader).append(consumersList);
		} else {
			fullMessage.append(ChatColor.GRAY).append("This house is not providing labour to any consumers.");
		}
		player.sendMessage(fullMessage.toString());
	}
}
