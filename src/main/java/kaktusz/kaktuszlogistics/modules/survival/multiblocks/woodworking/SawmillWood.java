package kaktusz.kaktuszlogistics.modules.survival.multiblocks.woodworking;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.PolymorphicItem;
import kaktusz.kaktuszlogistics.items.properties.MatrixMultiblock;
import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.modules.survival.multiblocks.MultiblockMachine;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.world.multiblock.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SawmillWood extends MultiblockMachine {
	public SawmillWood(Multiblock property, Location location, ItemMeta meta) {
		super(property, location, meta);
	}

	@Override
	public ItemStack getDrop(Block block) {
		ItemStack drop = super.getDrop(block);
		drop.setType(block.getType()); //keep wood type
		return drop;
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {
		super.onInteracted(e);
	}

	@Override
	protected void openGUI(HumanEntity player) {
		if(!isStructureValid())
			return;
		//testing:
		setRecipe(RecipeManager.getMachineRecipeById("sawmill_planks"));
		tryStartProcessing();
	}

	/**
	 * This should only be called once, and the result should be registered with the CustomItemManager.
	 */
	public static CustomItem createCustomItem() {
		PolymorphicItem sawmill = new PolymorphicItem("multiblockSawmill", "Sawmill", Material.OAK_FENCE_GATE);
		ComponentCompound barrelStripesAlongAxis = new ComponentCompound(
				new ComponentMaterial(Material.BARREL),
				new ComponentDirectional(Multiblock.RELATIVE_DIRECTION.RIGHT).setAllowOpposite(true)
		);

		DecoratorSpecialBlock inputBarrel = new DecoratorSpecialBlock(barrelStripesAlongAxis, DecoratorSpecialBlock.SpecialType.ITEM_INPUT);
		DecoratorSpecialBlock outputBarrel = new DecoratorSpecialBlock(barrelStripesAlongAxis, DecoratorSpecialBlock.SpecialType.ITEM_OUTPUT);
		sawmill.getOrAddProperty(MatrixMultiblock.class)
				.setLayerModeHorizontal(true)
				.addLayer(new MultiblockComponent[][] {
						{new ComponentMaterial(Material.CRAFTING_TABLE), outputBarrel},
						{new ComponentAgnostic(), inputBarrel}
				})
				.addLayer(new MultiblockComponent[][] {
						{new ComponentTag(Tag.WOODEN_FENCES), ComponentCustomBlock.fromCustomItem(sawmill)},
						{null, null}
				})
				.setControllerBlockOffset(1, 0, 1)
				.setType(SawmillWood.class);

		return sawmill;
	}
}
