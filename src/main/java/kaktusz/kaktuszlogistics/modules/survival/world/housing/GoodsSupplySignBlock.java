package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import kaktusz.kaktuszlogistics.recipe.machine.MachineRecipe;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.CustomSignBlock;
import kaktusz.kaktuszlogistics.world.KLChunk;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Place on a container to mark it as a goods supplier
 */
public class GoodsSupplySignBlock extends CustomSignBlock {

	private transient Container containerCache = null;

	public GoodsSupplySignBlock(ItemPlaceable prop, Location location, ItemMeta meta) {
		super(prop, location, meta);
	}

	@Override
	public boolean verify(Block block) {
		return super.verify(block)
				&& block.getRelative(getWallSign().getFacing().getOppositeFace()).getState() instanceof Container;
	}

	@Override
	public void onSet(KLWorld world, int x, int y, int z) {
		KLChunk chunk = world.getChunkAt(VanillaUtils.blockToChunkCoord(x), VanillaUtils.blockToChunkCoord(z));
		chunk.getOrCreateExtraDataSet("goodsSuppliers").add(new VanillaUtils.BlockPosition(getLocation()));
		Bukkit.getScheduler().runTaskLater(KaktuszLogistics.INSTANCE, () -> {
			Sign state = getState();
			state.setLine(0, "Goods Supplier");
			state.setLine(1, ChatColor.DARK_GRAY + "[Right-Click]");
			state.update(false, false);
		}, 1);
	}

	@Override
	public void onRemoved(KLWorld world, int x, int y, int z) {
		KLChunk chunk = world.getChunkAt(VanillaUtils.blockToChunkCoord(x), VanillaUtils.blockToChunkCoord(z));
		chunk.removeFromExtraDataSet("goodsSuppliers", new VanillaUtils.BlockPosition(getLocation()));
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {
		//TODO house tier requirements GUI
	}

	public boolean consumeGoods(ItemIngredient[] goodsToConsume) {
		if(getContainer() == null)
			return false;
		if(goodsToConsume == null)
			return false;

		//we use the MachineRecipe's item consumption system
		MachineRecipe.ConsumptionAftermath aftermath = new MachineRecipe.ConsumptionAftermath(getSupplies());
		for (ItemIngredient goodIngredient : goodsToConsume) {
			if(!aftermath.consume(goodIngredient)) {
				return false; //ingredient missing
			}
		}

		//all ingredients present - consume them and return success
		aftermath.applyToOriginal();
		return true;
	}

	/**
	 * Tries to consume an accumulated list of inputs
	 * @param accumulatedSupplies A list of the inputs
	 * @param multiplier A multiplier to the quantity of goods that should be consumed.
	 * @return True if the accumulated inputs satisfied goodsToConsume, false otherwise
	 */
	public static boolean consumeGoodsAccumulated(ItemIngredient[] goodsToConsume, List<ItemInput> accumulatedSupplies, int multiplier) {
		if(goodsToConsume == null)
			return false;

		MachineRecipe.ConsumptionAftermath aftermath = new MachineRecipe.ConsumptionAftermath(accumulatedSupplies.toArray(new IRecipeInput[0]));
		for (ItemIngredient goodIngredient : goodsToConsume) {
			for(int i = 0; i < multiplier; i++) {
				if (!aftermath.consume(goodIngredient)) {
					return false; //ingredient missing
				}
			}
		}

		//all ingredients present - consume them and return success
		aftermath.applyToOriginal();
		return true;
	}

	public ItemInput[] getSupplies() {
		if(getContainer() == null)
			return new ItemInput[0];

		return ItemInput.getInputsFromContainer(getContainer()).toArray(ItemInput[]::new);
	}

	private Container getContainer() {
		if(containerCache != null)
			return containerCache;

		Block supportingBlock = getLocation().getBlock().getRelative(getWallSign().getFacing().getOppositeFace());
		if(supportingBlock.getState() instanceof Container)
			return containerCache = (Container) supportingBlock.getState();
		else
			return null;
	}
}
