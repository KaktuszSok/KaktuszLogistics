package kaktusz.kaktuszlogistics.items.properties.multiblock;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.BlockDurability;
import kaktusz.kaktuszlogistics.util.minecraft.SFXCollection;
import kaktusz.kaktuszlogistics.util.minecraft.SoundEffect;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;

/**
 * A property which defines a template for a multiblock
 */
@SuppressWarnings("UnusedReturnValue")
public abstract class MultiblockTemplate extends BlockDurability {

	/**
	 * A direction, relative to standing in front of and looking at the front face of a multiblock.
	 */
	public enum RELATIVE_DIRECTION {
		RIGHT, //facing to our right
		LEFT, //facing to our left
		UP, //facing up
		DOWN, //facing down
		TOWARDS, //facing towards the multiblock
		AWAY //facing away from the multiblock
	}
	private Class<? extends MultiblockBlock> multiblockType;

	//SETUP
	public MultiblockTemplate(CustomItem item) {
		super(item);
		setMaxDurability(1);
		setDamageSound(new SFXCollection(
				new SoundEffect(Sound.ENTITY_ITEM_BREAK, 0.7f, 0.5f),
				new SoundEffect(Sound.ENTITY_ITEM_BREAK, 0.7f, 1.4f)));
	}

	/**
	 * Set which kind of multiblock this item should represent
	 */
	public MultiblockTemplate setType(Class<? extends MultiblockBlock> type) {
		multiblockType = type;

		return this;
	}

	//INFO
	public String getName() {
		return item.displayName;
	}

	//STRUCTURE
	/**
	 * @param multiblock The custom block which holds data about this specific multiblock structure
	 * @return True if the entire multiblock structure is valid, false if not
	 */
	public abstract boolean verifyStructure(MultiblockBlock multiblock);

	/**
	 * Gets the axis-aligned bounding box which contains the entire multiblock structure
	 */
	public abstract VanillaUtils.BlockAABB getAABB(MultiblockBlock multiblock);

	/**
	 * @param position A position within the AABB
	 * @return True if this block counts as part of the multiblock
	 */
	public boolean isPosPartOfMultiblock(BlockPosition position, MultiblockBlock multiblock) {
		return true;
	}

	//HELPER
	/**
	 * Transforms a relative offset to a world offset (or vice-versa)
	 * @param multiblockFacing The direction that the multiblock's front is facing
	 * @param offset The relative offset, when looking at the front face of the multiblock, to the right (x), up (y) and behind (z).
	 */
	protected static BlockPosition transformOffset(BlockFace multiblockFacing, BlockPosition offset) {
		switch (multiblockFacing) {
			case SOUTH:
				return new BlockPosition(offset.x, offset.y, -offset.z);
			case NORTH:
				return new BlockPosition(-offset.x, offset.y, offset.z);
			case EAST:
				return new BlockPosition(-offset.z, offset.y, -offset.x);
			case WEST:
				return new BlockPosition(offset.z, offset.y, offset.x);

			default: //invalid orientation
				return null;
		}
	}

	public static RELATIVE_DIRECTION relativeDirectionFromGlobal(BlockFace multiblockFacing, BlockFace globalDirection) {
		if(globalDirection.getModY() != 0)
			return globalDirection == BlockFace.UP ? RELATIVE_DIRECTION.UP : RELATIVE_DIRECTION.DOWN;

		switch (multiblockFacing) {
			case SOUTH:
				switch (globalDirection) {
					case SOUTH:
						return RELATIVE_DIRECTION.AWAY;
					case NORTH:
						return RELATIVE_DIRECTION.TOWARDS;
					case EAST:
						return RELATIVE_DIRECTION.RIGHT;
					case WEST:
						return RELATIVE_DIRECTION.LEFT;
				}
				break;
			case NORTH:
				switch (globalDirection) {
					case SOUTH:
						return RELATIVE_DIRECTION.TOWARDS;
					case NORTH:
						return RELATIVE_DIRECTION.AWAY;
					case EAST:
						return RELATIVE_DIRECTION.LEFT;
					case WEST:
						return RELATIVE_DIRECTION.RIGHT;
				}
				break;
			case EAST:
				switch (globalDirection) {
					case SOUTH:
						return RELATIVE_DIRECTION.LEFT;
					case NORTH:
						return RELATIVE_DIRECTION.RIGHT;
					case EAST:
						return RELATIVE_DIRECTION.AWAY;
					case WEST:
						return RELATIVE_DIRECTION.TOWARDS;
				}
				break;
			case WEST:
				switch (globalDirection) {
					case SOUTH:
						return RELATIVE_DIRECTION.RIGHT;
					case NORTH:
						return RELATIVE_DIRECTION.LEFT;
					case EAST:
						return RELATIVE_DIRECTION.TOWARDS;
					case WEST:
						return RELATIVE_DIRECTION.AWAY;
				}
				break;
		}

		//invalid orientation
		return null;
	}

	/**
	 * Gets the block at the position given by the position and front face of a multiblock and an offset relative to those.
	 * @param relativeOrigin The block at offset = 0,0,0 - i.e. the multiblock's core block
	 * @param face The direction the multiblock is facing
	 */
	protected static Block getBlockAtRelativeOffset(Location relativeOrigin, BlockFace face, int offsetRight, int offsetUp, int offsetBehind) {
		BlockPosition worldSpaceOffset = transformOffset(face, new BlockPosition(offsetRight, (short)offsetUp, offsetBehind));
		if(worldSpaceOffset == null) //invalid orientation
			return relativeOrigin.getBlock();
		return relativeOrigin.getBlock().getRelative(worldSpaceOffset.x, worldSpaceOffset.y, worldSpaceOffset.z);
	}

	//CUSTOMBLOCK
	@Override
	public MultiblockBlock createCustomBlock(ItemMeta stackMeta, Location location) {
		try {
			return multiblockType.getConstructor(MultiblockTemplate.class, Location.class, ItemMeta.class).newInstance(this, location, stackMeta);
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}
}
