package kaktusz.kaktuszlogistics.util.minecraft.config;

import org.bukkit.configuration.file.FileConfiguration;

public class IntegerOption extends ConfigOption<Integer> {
	public IntegerOption(String path, Integer defaultValue) {
		super(path, defaultValue);
	}

	@Override
	public Integer readFromConfig(FileConfiguration config) {
		return value = config.getInt(path);
	}
}
