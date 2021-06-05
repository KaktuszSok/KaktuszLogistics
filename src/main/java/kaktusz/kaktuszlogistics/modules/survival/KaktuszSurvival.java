package kaktusz.kaktuszlogistics.modules.survival;

import kaktusz.kaktuszlogistics.commands.KLCommand;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.PolymorphicItem;
import kaktusz.kaktuszlogistics.items.properties.MatrixMultiblock;
import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.modules.KaktuszModule;
import kaktusz.kaktuszlogistics.modules.survival.commands.HouseSubcommand;
import kaktusz.kaktuszlogistics.modules.survival.commands.RoomSubcommand;
import kaktusz.kaktuszlogistics.modules.survival.multiblocks.woodworking.SawmillWood;
import kaktusz.kaktuszlogistics.modules.survival.world.housing.RoomInfo;
import kaktusz.kaktuszlogistics.util.minecraft.config.BooleanOption;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigOption;
import kaktusz.kaktuszlogistics.world.multiblock.*;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.type.Slab;

public class KaktuszSurvival implements KaktuszModule {

	public static KaktuszSurvival INSTANCE;

	//config quick access
	public static BooleanOption CALC_ROOMS_ASYNC = new BooleanOption("survival.housing.room.calculateRoomsAsync", false);

	public void initialise() {
		INSTANCE = this;

		//do precalculations
		precalcSlabs();

		//update config-based pseudoconstants
		RoomInfo.MAX_VOLUME = RoomInfo.MAX_SIZE_HORIZONTAL.value*RoomInfo.MAX_SIZE_VERTICAL.value*RoomInfo.MAX_SIZE_HORIZONTAL.value/3;

		//add content
		initItems();

		//register commands
		KLCommand.registerSubcommand(new RoomSubcommand("room"));
		KLCommand.registerSubcommand(new HouseSubcommand("house"));
	}

	private void initItems() {
		PolymorphicItem sawmill = new PolymorphicItem("multiblockSawmill", "Sawmill", Material.OAK_FENCE_GATE);
		ComponentCompound barrelStripesAlongAxis = new ComponentCompound(
				new ComponentMaterial(Material.BARREL),
				new ComponentDirectional(Multiblock.RELATIVE_DIRECTION.RIGHT).setAllowOpposite(true)
		);
		sawmill.getOrAddProperty(MatrixMultiblock.class)
				.setLayerModeHorizontal(true)
				.addLayer(new MultiblockComponent[][] {
						{new ComponentMaterial(Material.CRAFTING_TABLE), barrelStripesAlongAxis},
						{new ComponentAgnostic(), barrelStripesAlongAxis}
				})
				.addLayer(new MultiblockComponent[][] {
						{new ComponentTag(Tag.WOODEN_FENCES), ComponentCustomBlock.fromCustomItem(sawmill)},
						{null, null}
				})
				.setControllerBlockOffset(1, 0, 1)
				.setType(SawmillWood.class);
		CustomItemManager.registerItem(sawmill);
	}

	@Override
	public ConfigOption<?>[] getAllOptions() {
		return new ConfigOption[] {
				CALC_ROOMS_ASYNC,
				RoomInfo.MAX_SIZE_HORIZONTAL,
				RoomInfo.MAX_SIZE_VERTICAL
		};
	}

	private void precalcSlabs() {
		RoomInfo.SLABS.clear();
		for (Material mat : Material.values()) {
			if(Slab.class.isAssignableFrom(mat.data)) {
				RoomInfo.SLABS.add(mat);
			}
		}
	}
}
