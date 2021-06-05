package kaktusz.kaktuszlogistics.util.minecraft.config;

import org.bukkit.configuration.file.FileConfiguration;

public class StringOption extends ConfigOption<String> {
	public StringOption(String path, String defaultValue) {
		super(path, defaultValue);
	}

	@Override
	public String readFromConfig(FileConfiguration config) {
		return value = config.getString(path);
	}
}
