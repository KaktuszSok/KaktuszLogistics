package kaktusz.kaktuszlogistics.modules.nations;

import kaktusz.kaktuszlogistics.commands.KLCommand;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.modules.KaktuszModule;
import kaktusz.kaktuszlogistics.modules.nations.commands.FlagSubcommand;
import kaktusz.kaktuszlogistics.modules.nations.items.FlagItem;
import kaktusz.kaktuszlogistics.modules.nations.items.properties.FlagPlaceable;
import org.bukkit.Material;

public class KaktuszNations implements KaktuszModule {

	public static KaktuszNations INSTANCE;

	public static FlagItem FLAG_ITEM;

	public void initialise() {
		INSTANCE = this;

		//register items
		FLAG_ITEM = new FlagItem("nationFlag", "Flag", Material.WHITE_BANNER);
		FLAG_ITEM.getOrAddProperty(FlagPlaceable.class);
		CustomItemManager.registerItem(FLAG_ITEM);

		//register commands
		KLCommand.registerSubcommand(new FlagSubcommand("flag"));
	}

}
