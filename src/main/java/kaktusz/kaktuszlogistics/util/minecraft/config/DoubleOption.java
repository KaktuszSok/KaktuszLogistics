package kaktusz.kaktuszlogistics.util.minecraft.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class DoubleOption extends ConfigOption<Double> {
	public DoubleOption(String path, Double defaultValue, List<ConfigOption<?>> optionList) {
		super(path, defaultValue, optionList);
	}

	@Override
	public Double readFromConfig(FileConfiguration config) {
		return value = config.getDouble(path);
	}
}
