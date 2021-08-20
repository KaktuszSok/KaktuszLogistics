package kaktusz.kaktuszlogistics.modules.survival.world.multiblocks.woodworking;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.PolymorphicItem;
import kaktusz.kaktuszlogistics.items.properties.multiblock.MatrixMultiblock;
import kaktusz.kaktuszlogistics.items.properties.multiblock.MultiblockTemplate;
import kaktusz.kaktuszlogistics.recipe.machine.MachineRecipe;
import kaktusz.kaktuszlogistics.util.ListUtils;
import kaktusz.kaktuszlogistics.util.minecraft.SFXCollection;
import kaktusz.kaktuszlogistics.util.minecraft.SoundEffect;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockMachine;
import kaktusz.kaktuszlogistics.world.multiblock.components.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SawmillWood extends MultiblockMachine {
	private static final long serialVersionUID = 100L;

	private static final SFXCollection RECIPE_DONE_SOUND = new SFXCollection(
			new SoundEffect(Sound.BLOCK_WOOD_BREAK, 0.5f, 0.5f, 1.4f, 1.6f)
	);
	private static final BlockData RECIPE_DONE_PARTICLE_DATA = Material.OAK_PLANKS.createBlockData();

	public SawmillWood(MultiblockTemplate property, Location location, ItemMeta meta) {
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
	public Material getGUIHeader() {
		return Material.STONECUTTER;
	}

	@Override
	protected List<String> getSupportedRecipePrefixes() {
		return ListUtils.listFromSingle("sawmill");
	}

	@Override
	protected SFXCollection getRecipeDoneSound(MachineRecipe<?> recipe) {
		return RECIPE_DONE_SOUND;
	}

	@Override
	protected BlockData getRecipeDoneParticlesData(MachineRecipe<?> recipe) {
		return RECIPE_DONE_PARTICLE_DATA;
	}

	@Override
	public boolean verify(Block block) {
		return Tag.FENCE_GATES.isTagged(block.getBlockData().getMaterial());
	}

	/**
	 * Creates the multiblock item and sets up the recipe.
	 * This should only be called once, and the result should be registered with the CustomItemManager.
	 */
	public static CustomItem createCustomItem() {
		PolymorphicItem sawmill = new PolymorphicItem("multiblockSawmill", "Sawmill", Material.OAK_FENCE_GATE);

		ComponentCompound barrelStripesAlongAxis = new ComponentCompound(
				new ComponentMaterial(Material.BARREL),
				new ComponentDirectional(MultiblockTemplate.RELATIVE_DIRECTION.RIGHT).setAllowOpposite(true)
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
						{new ComponentTag(Tag.WOODEN_FENCES), new ComponentCustomBlock(sawmill)},
						{null, null}
				})
				.setControllerBlockOffset(1, 0, 1)
				.setType(SawmillWood.class);

		return sawmill;
	}
}
