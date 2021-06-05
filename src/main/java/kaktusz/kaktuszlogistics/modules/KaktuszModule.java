package kaktusz.kaktuszlogistics.modules;

import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigManager;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigOption;

public interface KaktuszModule {
	void initialise();
	ConfigOption<?>[] getAllOptions();
}
