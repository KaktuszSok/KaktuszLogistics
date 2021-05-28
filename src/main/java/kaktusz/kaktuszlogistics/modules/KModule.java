package kaktusz.kaktuszlogistics.modules;

import kaktusz.kaktuszlogistics.modules.nations.KaktuszNations;
import kaktusz.kaktuszlogistics.modules.survival.KaktuszSurvival;
import kaktusz.kaktuszlogistics.modules.weaponry.KaktuszWeaponry;

/**
 * Enum containing all valid KaktuszModules
 */
public enum KModule {
	NATIONS("KNations", new KaktuszNations()),
	SURVIVAL("KSurvival", new KaktuszSurvival()),
	WEAPONRY("KWeaponry", new KaktuszWeaponry());

	public final String name;
	public final KaktuszModule instance;

	KModule(String name, KaktuszModule moduleInstance) {
		this.name = name;
		this.instance = moduleInstance;
	}
}
