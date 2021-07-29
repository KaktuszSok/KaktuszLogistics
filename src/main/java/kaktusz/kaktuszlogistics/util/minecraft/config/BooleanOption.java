package kaktusz.kaktuszlogistics.util.minecraft.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class BooleanOption extends ConfigOption<Boolean> {
	public BooleanOption(String path, Boolean defaultValue, List<ConfigOption<?>> optionList) {
		super(path, defaultValue, optionList);
	}

	@Override
	public Boolean readFromConfig(FileConfiguration config) {
		return value = config.getBoolean(path);
	}
}
