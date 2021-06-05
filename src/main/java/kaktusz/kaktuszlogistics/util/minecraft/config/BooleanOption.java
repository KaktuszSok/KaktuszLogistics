package kaktusz.kaktuszlogistics.util.minecraft.config;

import org.bukkit.configuration.file.FileConfiguration;

public class BooleanOption extends ConfigOption<Boolean> {
	public BooleanOption(String path, Boolean defaultValue) {
		super(path, defaultValue);
	}

	@Override
	public Boolean readFromConfig(FileConfiguration config) {
		return value = config.getBoolean(path);
	}
}
