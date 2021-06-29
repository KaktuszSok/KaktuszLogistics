package kaktusz.kaktuszlogistics.world;

/**
 * Block that can run code on every tick as well as on being loaded and saved
 */
public interface TickingBlock {
	default void onLoaded() {}
	void onTick();
	default void onSave() {}
}
