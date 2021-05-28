package kaktusz.kaktuszlogistics.modules;

import org.bukkit.configuration.file.FileConfiguration;

public interface KaktuszModule {
	void initialise();
	default void addDefaultConfigs(FileConfiguration config) {
		//no configs by default
	}
}
