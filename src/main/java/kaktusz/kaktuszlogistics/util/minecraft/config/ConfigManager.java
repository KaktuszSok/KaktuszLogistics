package kaktusz.kaktuszlogistics.util.minecraft.config;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.modules.KModule;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for interfacing with the plugin configuration
 */
public class ConfigManager {

	private FileConfiguration fileConfig = null;
	/**
	 * All registered options
	 */
	private final List<ConfigOption<?>> configOptions = new ArrayList<>();

	private static final List<ConfigOption<?>> GENERAL_OPTIONS = new ArrayList<>();
	public static final BooleanOption BROADCAST_PLAYER_KILLS = new BooleanOption("messages.broadcastKillMessages.players",true, GENERAL_OPTIONS);
	public static final BooleanOption BROADCAST_NAMED_MOB_KILLS = new BooleanOption("messages.broadcastKillMessages.namedMobs",false, GENERAL_OPTIONS);

	public void initialise() {
		fileConfig = KaktuszLogistics.INSTANCE.getConfig();

		//add module enable/disable options
		for(KModule module : KModule.values()) {
			registerOption(module.isEnabled);
		}

		//add general options
		registerOptions(GENERAL_OPTIONS);

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
		KaktuszLogistics.LOGGER.info("Registering option " + option.path);
		return option;
	}

	public void registerOptions(List<ConfigOption<?>> options) {
		for (ConfigOption<?> option : options) {
			registerOption(option);
		}
	}

	/**
	 * Updates all registered options to match the configuration file's values
	 */
	public void readFromConfig() {
		configOptions.forEach(o -> o.readFromConfig(fileConfig));
	}

}
