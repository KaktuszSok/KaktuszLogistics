package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockComponent;
import org.bukkit.Bukkit;
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
	public boolean verifyStructure(Block multiblockCore, MultiblockBlock multiblock) {
		for (int layerIdx = 0; layerIdx < layers.size(); layerIdx++) {
			MultiblockComponent[][] layer = layers.get(layerIdx);
			for(int row = 0; row < layer.length; row++) {
				for(int col = 0; col < layer[row].length; col++) {
					Block currBlock = getBlock(layerIdx, row, col, multiblockCore, multiblock);
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

	//HELPER
	private Block getBlock(int layerIndex, int row, int column, Block multiblockCore, MultiblockBlock multiblock) {
		BlockPosition offset;
		if(horizontalLayers)
			offset = new BlockPosition(column - controllerBlockColumn, (short)(layerIndex - controllerBlockLayer), controllerBlockRow - row);
		else {
			offset = new BlockPosition(column - controllerBlockColumn, (short) (row - controllerBlockRow), layerIndex - controllerBlockLayer);
		}
		return Multiblock.getBlockAtRelativeOffset(multiblockCore, multiblock.getProperty().getFacing(multiblock.data), offset.x, offset.y, offset.z);
	}
}
