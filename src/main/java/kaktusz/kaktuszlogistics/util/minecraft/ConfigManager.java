package kaktusz.kaktuszlogistics.util.minecraft;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.modules.KModule;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Class responsible for interfacing with the plugin configuration
 */
public class ConfigManager {

	private FileConfiguration fileConfig = null;

	public void initialise() {
		fileConfig = KaktuszLogistics.INSTANCE.getConfig();

		addModulesSection();
		addMessagesSection();
		//add module defaults
		for (KModule module : KModule.values()) {
			module.instance.addDefaultConfigs(fileConfig);
		}

		fileConfig.options().copyDefaults(true);
		KaktuszLogistics.INSTANCE.saveConfig();
	}

	//MODULES
	private void addModulesSection() {
		for(KModule module : KModule.values()) {
			fileConfig.addDefault("modules.enable" + module.name, true);
		}
	}

	/**
	 * @return True if this module is enabled, false if it is not.
	 */
	public boolean isModuleEnabled(KModule module) {
		return fileConfig.getBoolean("modules.enable" + module.name);
	}

	//MESSAGES
	private void addMessagesSection() {
		fileConfig.addDefault("messages.broadcastKillMessages.players", true);
		fileConfig.addDefault("messages.broadcastKillMessages.nametaggedMobs", true);
	}
	/**
	 * @return Should the plugin broadcast player kill messages?
	 */
	public boolean broadcastPlayerKills() {
		return fileConfig.getBoolean("messages.broadcastKillMessages.players");
	}

	/**
	 * @return Should the plugin broadcast named mob kill messages?
	 */
	public boolean broadcastNamedMobKills() {
		return fileConfig.getBoolean("messages.broadcastKillMessages.nametaggedMobs");
	}

	//OTHER
	/**
	 * Use this if you want to work with the configuration directly.
	 * @return The configuration we are using
	 */
	public FileConfiguration accessConfigDirectly() {
		return fileConfig;
	}

}
