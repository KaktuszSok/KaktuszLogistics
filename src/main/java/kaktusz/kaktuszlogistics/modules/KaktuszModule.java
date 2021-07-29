package kaktusz.kaktuszlogistics.modules;

import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigOption;

import java.util.List;

public interface KaktuszModule {
	void initialise();
	List<ConfigOption<?>> getAllOptions();
}
