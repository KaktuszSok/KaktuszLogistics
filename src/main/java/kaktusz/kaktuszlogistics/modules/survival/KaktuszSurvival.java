package kaktusz.kaktuszlogistics.modules.survival;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.commands.KLCommand;
import kaktusz.kaktuszlogistics.modules.KaktuszModule;
import kaktusz.kaktuszlogistics.modules.survival.commands.HouseSubcommand;
import kaktusz.kaktuszlogistics.modules.survival.commands.RoomSubcommand;
import kaktusz.kaktuszlogistics.modules.survival.world.housing.RoomInfo;
import org.bukkit.configuration.file.FileConfiguration;

public class KaktuszSurvival implements KaktuszModule {

	public static KaktuszSurvival INSTANCE;

	//config quick access
	public static boolean CALC_ROOMS_ASYNC;

	public void initialise() {
		INSTANCE = this;

		//config
		CALC_ROOMS_ASYNC = KaktuszLogistics.INSTANCE.config.accessConfigDirectly().getBoolean("survival.housing.room.calculateRoomsAsync");
		RoomInfo.MAX_SIZE_HORIZONTAL = KaktuszLogistics.INSTANCE.config.accessConfigDirectly().getInt("survival.housing.room.maxSizeHorizontal");
		RoomInfo.MAX_SIZE_VERTICAL = KaktuszLogistics.INSTANCE.config.accessConfigDirectly().getInt("survival.housing.room.maxSizeVertical");

		//register commands
		KLCommand.registerSubcommand(new RoomSubcommand("room"));
		KLCommand.registerSubcommand(new HouseSubcommand("house"));
	}

	@Override
	public void addDefaultConfigs(FileConfiguration config) {
		config.addDefault("survival.housing.room.calculateRoomsAsync", false);
		config.addDefault("survival.housing.room.maxSizeHorizontal", 33);
		config.addDefault("survival.housing.room.maxSizeVertical", 12);
	}
}
