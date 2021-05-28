package kaktusz.kaktuszlogistics.modules.survival;

import kaktusz.kaktuszlogistics.modules.KaktuszModule;

public class KaktuszSurvival implements KaktuszModule {

	public static KaktuszSurvival INSTANCE;

	public void initialise() {
		INSTANCE = this;
	}

}
