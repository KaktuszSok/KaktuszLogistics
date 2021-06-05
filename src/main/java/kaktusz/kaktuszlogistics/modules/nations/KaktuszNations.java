package kaktusz.kaktuszlogistics.modules.nations;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.commands.KLCommand;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.modules.KaktuszModule;
import kaktusz.kaktuszlogistics.modules.nations.commands.FlagSubcommand;
import kaktusz.kaktuszlogistics.modules.nations.items.FlagItem;
import kaktusz.kaktuszlogistics.modules.nations.items.properties.FlagPlaceable;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigManager;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigOption;
import kaktusz.kaktuszlogistics.util.minecraft.config.IntegerOption;
import org.bukkit.Material;

import java.util.Arrays;

public class KaktuszNations implements KaktuszModule {

	public static KaktuszNations INSTANCE;

	//config quick access
	public static FlagItem FLAG_ITEM;

	public static final IntegerOption CLAIM_DISTANCE = new IntegerOption("nations.claim.radius", 3);
	public static final IntegerOption OUTSKIRTS_DISTANCE = new IntegerOption("nations.claims.outskirtsDepth", 4);

	public void initialise() {
		INSTANCE = this;

		//register items
		FLAG_ITEM = new FlagItem("nationFlag", "Flag", Material.WHITE_BANNER);
		FLAG_ITEM.getOrAddProperty(FlagPlaceable.class);
		CustomItemManager.registerItem(FLAG_ITEM);

		//register commands
		KLCommand.registerSubcommand(new FlagSubcommand("flag"));
	}

	@Override
	public ConfigOption<?>[] getAllOptions() {
		return new ConfigOption[] {
				CLAIM_DISTANCE,
				OUTSKIRTS_DISTANCE
		};
	}
}
