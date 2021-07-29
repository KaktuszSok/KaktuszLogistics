package kaktusz.kaktuszlogistics.world;

/**
 * Adding this interface to a block will stop it from being destroyed or damaged by explosions,
 * so you should handle doing so manually.
 */
public interface ExplodableBlock {
	/**
	 * Called when the block is destroyed by an explosion
	 * @param yield Yield of the explosion. Keep in mind it seems to be inverted?
	 */
	void onExploded(float yield);
}
