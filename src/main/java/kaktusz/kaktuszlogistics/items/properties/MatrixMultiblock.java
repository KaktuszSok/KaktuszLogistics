package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.multiblock.components.ComponentAgnostic;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import kaktusz.kaktuszlogistics.world.multiblock.components.MultiblockComponent;
import org.bukkit.block.Block;

import java.util.ArrayList;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;

/**
 * A multiblock which verifies based on a non-dynamic 3D component matrix built up of layers
 */
public class MatrixMultiblock extends Multiblock {

	private boolean horizontalLayers = false;
	private final ArrayList<MultiblockComponent[][]> layers = new ArrayList<>();
	private int controllerBlockLayer;
	private int controllerBlockRow;
	private int controllerBlockColumn;

	//SETUP
	public MatrixMultiblock(CustomItem item) {
		super(item);
	}

	/**
	 * Sets the layer mode to horizontal (true) or vertical (false).
	 * In horizontal mode, each layer is a horizontal slice (XZ plane).
	 * In vertical mode, each layer is a vertical slice (XY or YZ plane, depending on multiblock direction)
	 */
	public MatrixMultiblock setLayerModeHorizontal(boolean horizontal) {
		this.horizontalLayers = horizontal;

		return this;
	}

	public MatrixMultiblock addLayer(MultiblockComponent[][] layer) {
		layers.add(layer);

		return this;
	}

	public MatrixMultiblock setControllerBlockOffset(int layer, int row, int column) {
		controllerBlockLayer = layer;
		controllerBlockRow = row;
		controllerBlockColumn = column;

		return this;
	}

	//STRUCTURE
	@Override
	public boolean verifyStructure(MultiblockBlock multiblock) {
		for (int layerIdx = 0; layerIdx < layers.size(); layerIdx++) {
			MultiblockComponent[][] layer = layers.get(layerIdx);
			for(int row = 0; row < layer.length; row++) {
				for(int col = 0; col < layer[row].length; col++) {
					Block currBlock = getBlock(layerIdx, row, col, multiblock);
					MultiblockComponent component = layer[row][col];

					//null components must be air
					if(component == null && !currBlock.isEmpty()) {
						return false;
					}

					//non-null components must match
					if(component != null && !component.match(currBlock, multiblock)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public VanillaUtils.BlockAABB getAABB(MultiblockBlock multiblock) {
		BlockPosition cornerA = getBlockPosition(0, 0, 0, multiblock);
		int maxLayer = layers.size()-1;
		int maxRow = 0;
		int maxColumn = 0;
		for(MultiblockComponent[][] layer : layers) {
			if(layer.length > maxRow) //amount of rows in layer > maxRow
				maxRow = layer.length;
			for(MultiblockComponent[] row : layer) {
				if(row.length > maxColumn) { //amount of columns in row > maxColumn
					maxColumn = row.length;
				}
			}
		}
		BlockPosition cornerB = getBlockPosition(maxLayer, maxRow-1, maxColumn-1, multiblock);

		return VanillaUtils.BlockAABB.fromAnyCorners(cornerA, cornerB);
	}

	@Override
	public boolean isPosPartOfMultiblock(BlockPosition position, MultiblockBlock multiblock) {
		return !(getComponentAtWorldPosition(position, multiblock) instanceof ComponentAgnostic);
	}

	//HELPER
	/**
	 * Transforms matrix position to block in the world for a given multiblock instance
	 */
	protected final Block getBlock(int layerIndex, int row, int column, MultiblockBlock multiblock) {
		BlockPosition offset;
		if(horizontalLayers)
			offset = new BlockPosition(column - controllerBlockColumn, (short)(layerIndex - controllerBlockLayer), controllerBlockRow - row);
		else {
			offset = new BlockPosition(column - controllerBlockColumn, (short) (row - controllerBlockRow), layerIndex - controllerBlockLayer);
		}
		return Multiblock.getBlockAtRelativeOffset(multiblock.location, multiblock.getFacing(), offset.x, offset.y, offset.z);
	}

	/**
	 * Transforms matrix position to world position for a given multiblock instance
	 */
	protected final BlockPosition getBlockPosition(int layerIndex, int row, int column, MultiblockBlock multiblock) {
		BlockPosition relativeOffset;
		if(horizontalLayers)
			relativeOffset = new BlockPosition(column - controllerBlockColumn, (short)(layerIndex - controllerBlockLayer), controllerBlockRow - row);
		else {
			relativeOffset = new BlockPosition(column - controllerBlockColumn, (short) (row - controllerBlockRow), layerIndex - controllerBlockLayer);
		}
		BlockPosition worldOffset = transformOffset(multiblock.getFacing(), relativeOffset);
		if(worldOffset == null) //invalid multiblock orientation
			return new BlockPosition(multiblock.location);

		return new BlockPosition(multiblock.location.getBlockX() + worldOffset.x, multiblock.location.getBlockY() + worldOffset.y, multiblock.location.getBlockZ() + worldOffset.z);
	}

	/**
	 * Gets the component in the matrix given a world position and a specific multiblock instance
	 */
	protected final MultiblockComponent getComponentAtWorldPosition(BlockPosition worldPos, MultiblockBlock multiblock) {
		BlockPosition worldOffset = new BlockPosition(worldPos.x - multiblock.location.getBlockX(), worldPos.y - multiblock.location.getBlockY(), worldPos.z - multiblock.location.getBlockZ());
		BlockPosition relativeOffset = transformOffset(multiblock.getFacing(), worldOffset);
		if(relativeOffset == null) //invalid controller orientation
			return null;

		int layerIdx, row, column;
		//noinspection IfStatementWithIdenticalBranches
		if(horizontalLayers) {
			layerIdx = relativeOffset.y + controllerBlockLayer;
			row = controllerBlockRow - relativeOffset.z;
			column = relativeOffset.x + controllerBlockColumn;
		} else {
			layerIdx = relativeOffset.z + controllerBlockLayer;
			row = relativeOffset.y + controllerBlockRow;
			column = relativeOffset.x + controllerBlockColumn;
		}
		if(layerIdx >= layers.size())
			return null;
		MultiblockComponent[][] layer = layers.get(layerIdx);
		if(row >= layer.length)
			return null;
		if(column >= layer[row].length)
			return null;

		return layer[row][column];
	}
}
