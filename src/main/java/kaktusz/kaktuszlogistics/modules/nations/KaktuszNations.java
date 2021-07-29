package kaktusz.kaktuszlogistics.modules.nations;

import kaktusz.kaktuszlogistics.commands.KLCommand;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.modules.KaktuszModule;
import kaktusz.kaktuszlogistics.modules.nations.commands.FlagSubcommand;
import kaktusz.kaktuszlogistics.modules.nations.items.FlagItem;
import kaktusz.kaktuszlogistics.modules.nations.items.properties.FlagPlaceable;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigOption;
import kaktusz.kaktuszlogistics.util.minecraft.config.IntegerOption;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class KaktuszNations implements KaktuszModule {
	@SuppressWarnings("unused")
	public static KaktuszNations INSTANCE;

	//item quick access
	public static FlagItem FLAG_ITEM;

	//config
	private static final List<ConfigOption<?>> ALL_OPTIONS = new ArrayList<>();
	public static final IntegerOption CLAIM_DISTANCE = new IntegerOption("nations.claims.radius", 3, ALL_OPTIONS);
	public static final IntegerOption OUTSKIRTS_DISTANCE = new IntegerOption("nations.claims.outskirtsDepth", 4, ALL_OPTIONS);

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
	public List<ConfigOption<?>> getAllOptions() {
		return ALL_OPTIONS;
	}
}
