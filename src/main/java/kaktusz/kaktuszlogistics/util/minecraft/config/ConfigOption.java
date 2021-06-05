package kaktusz.kaktuszlogistics.util.minecraft.config;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class ConfigOption<T> {

	public final String path;
	public T value;

	public ConfigOption(String path, T defaultValue) {
		this.path = path;
		this.value = defaultValue;
	}

	/**
	 * Sets the value of this option to that specified by the configuration file
	 * @param config file to read from
	 * @return the value that was set
	 */
	@SuppressWarnings("UnusedReturnValue")
	protected abstract T readFromConfig(FileConfiguration config);

	@Override
	public String toString() {
		return value.toString();
	}
}
