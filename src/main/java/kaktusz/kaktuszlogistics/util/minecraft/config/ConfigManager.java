package kaktusz.kaktuszlogistics.util.minecraft.config;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.modules.KModule;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Class responsible for interfacing with the plugin configuration
 */
public class ConfigManager {

	private FileConfiguration fileConfig = null;
	private final List<ConfigOption<?>> configOptions = new ArrayList<>();

	public static final BooleanOption BROADCAST_PLAYER_KILLS = new BooleanOption("messages.broadcastKillMessages.players",true);
	public static final BooleanOption BROADCAST_NAMED_MOB_KILLS = new BooleanOption("messages.broadcastKillMessages.namedMobs",false);

	public void initialise() {
		fileConfig = KaktuszLogistics.INSTANCE.getConfig();

		//add module enable/disable options
		for(KModule module : KModule.values()) {
			registerOption(module.isEnabled);
		}

		//add general options
		registerOptions(BROADCAST_PLAYER_KILLS, BROADCAST_NAMED_MOB_KILLS);

		//add module options
		for (KModule module : KModule.values()) {
			registerOptions(module.instance.getAllOptions());
		}

		//sync options with config file
		readFromConfig();

		fileConfig.options().copyDefaults(true);
		KaktuszLogistics.INSTANCE.saveConfig();
	}

	@SuppressWarnings("UnusedReturnValue")
	public <T extends ConfigOption<?>> T registerOption(T option) {
		configOptions.add(option);
		fileConfig.addDefault(option.path, option.getValue());
		return option;
	}

	public void registerOptions(ConfigOption<?>... options) {
		if(options != null)
			Arrays.stream(options).iterator().forEachRemaining(this::registerOption);
	}

	/**
	 * Updates all registered options to match the configuration file's values
	 */
	public void readFromConfig() {
		configOptions.forEach(o -> o.readFromConfig(fileConfig));
	}

}
