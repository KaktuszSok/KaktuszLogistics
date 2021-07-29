package kaktusz.kaktuszlogistics.util.minecraft.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class StringOption extends ConfigOption<String> {
	public StringOption(String path, String defaultValue, List<ConfigOption<?>> optionList) {
		super(path, defaultValue, optionList);
	}

	@Override
	public String readFromConfig(FileConfiguration config) {
		return value = config.getString(path);
	}
}
