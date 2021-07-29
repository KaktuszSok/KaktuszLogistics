package kaktusz.kaktuszlogistics.util.minecraft.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class IntegerOption extends ConfigOption<Integer> {
	public IntegerOption(String path, Integer defaultValue, List<ConfigOption<?>> optionList) {
		super(path, defaultValue, optionList);
	}

	@Override
	public Integer readFromConfig(FileConfiguration config) {
		return value = config.getInt(path);
	}
}
