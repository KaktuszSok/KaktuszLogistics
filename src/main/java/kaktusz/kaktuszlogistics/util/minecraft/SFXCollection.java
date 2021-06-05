package kaktusz.kaktuszlogistics.util.minecraft;

import org.bukkit.Location;
import org.bukkit.SoundCategory;

public class SFXCollection {

	private final SoundEffect[] sfx;

	public SFXCollection(SoundEffect... sfx) {
		this.sfx = sfx;
	}

	/**
	 * Sets the category of all contained sound effects
	 */
	public void setCategory(SoundCategory category) {
		for(SoundEffect s : sfx) {
			s.setCategory(category);
		}
	}

	public void playAll(Location where) {
		for(SoundEffect s : sfx) {
			s.play(where);
		}
	}

	/**
	 * @param pitchLerp Interpolates pitch between pitchMin and pitchMax
	 */
	public void playAll(Location where, float pitchLerp) {
		for(SoundEffect s : sfx) {
			s.play(where, pitchLerp);
		}
	}
}
