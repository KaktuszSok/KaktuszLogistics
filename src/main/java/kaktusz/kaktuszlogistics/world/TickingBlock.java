package kaktusz.kaktuszlogistics.world;

public interface TickingBlock {
	void onLoaded();
	void onTick();
	void onSave();
}
