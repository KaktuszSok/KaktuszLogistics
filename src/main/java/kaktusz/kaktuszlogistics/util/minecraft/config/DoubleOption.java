package kaktusz.kaktuszlogistics.util.minecraft.config;

import org.bukkit.configuration.file.FileConfiguration;

public class DoubleOption extends ConfigOption<Double> {
	public DoubleOption(String path, Double defaultValue) {
		super(path, defaultValue);
	}

	@Override
	public Double readFromConfig(FileConfiguration config) {
		return value = config.getDouble(path);
	}
}
