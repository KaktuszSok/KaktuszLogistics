package kaktusz.kaktuszlogistics.modules.nations;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.commands.KLCommand;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.modules.KaktuszModule;
import kaktusz.kaktuszlogistics.modules.nations.commands.FlagSubcommand;
import kaktusz.kaktuszlogistics.modules.nations.items.FlagItem;
import kaktusz.kaktuszlogistics.modules.nations.items.properties.FlagPlaceable;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class KaktuszNations implements KaktuszModule {

	public static KaktuszNations INSTANCE;

	public static FlagItem FLAG_ITEM;
	public static int CLAIM_DISTANCE;
	public static int OUTSKIRTS_DISTANCE;

	public void initialise() {
		INSTANCE = this;

		//config
		CLAIM_DISTANCE = KaktuszLogistics.INSTANCE.config.accessConfigDirectly().getInt("nations.claims.radius");
		OUTSKIRTS_DISTANCE = KaktuszLogistics.INSTANCE.config.accessConfigDirectly().getInt("nations.claims.outskirtsDepth");

		//register items
		FLAG_ITEM = new FlagItem("nationFlag", "Flag", Material.WHITE_BANNER);
		FLAG_ITEM.getOrAddProperty(FlagPlaceable.class);
		CustomItemManager.registerItem(FLAG_ITEM);

		//register commands
		KLCommand.registerSubcommand(new FlagSubcommand("flag"));
	}

	@Override
	public void addDefaultConfigs(FileConfiguration config) {
		config.addDefault("nations.claims.radius", 3);
		config.addDefault("nations.claims.outskirtsDepth", 4);
	}
}
