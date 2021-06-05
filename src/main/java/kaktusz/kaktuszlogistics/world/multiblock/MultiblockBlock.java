package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.world.DurableBlock;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class MultiblockBlock extends DurableBlock {

	private transient final Multiblock property;

	public MultiblockBlock(Multiblock property, ItemMeta meta) {
		super(property, meta);
		this.property = property;
	}

	@Override
	public ItemStack getDrop(Block block) {
		ItemStack drop = super.getDrop(block);
		ItemMeta meta = drop.getItemMeta();
		property.setFacing(meta, null);
		drop.setItemMeta(meta);
		return drop;
	}

	@Override
	public void onPlaced(BlockPlaceEvent e) {
		setFacingFromPlaceEvent(e);
	}

	public void setFacingFromPlaceEvent(BlockPlaceEvent e) {
		BlockFace facing;
		Vector facingVector = e.getPlayer().getEyeLocation().getDirection().setY(0).multiply(-1);
		if(Math.abs(facingVector.getX()) > Math.abs(facingVector.getZ())) { //facing east/west
			facing = facingVector.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
		}
		else {
			facing = facingVector.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
		}
		getProperty().setFacing(data, facing);

		//make placed block match
		BlockData blockData = e.getBlockPlaced().getBlockData();
		if(blockData instanceof Directional) {
			((Directional) blockData).setFacing(facing);
			e.getBlockPlaced().setBlockData(blockData);
		}
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {
		e.getPlayer().sendMessage("valid: " + isStructureValid(e.getClickedBlock()) + ", facing: " + getProperty().getFacing(data));
	}

	public boolean reverifyStructure(Block block) {
		return getProperty().verifyStructure(block, this);
	}

	/**
	 * Checks if the multiblock structure is valid.
	 */
	public boolean isStructureValid(Block block) {
		return reverifyStructure(block);
	}

	/**
	 * Gets the property which is responsible for handling the multiblock data
	 */
	public Multiblock getProperty() {
		return property;
	}
}
