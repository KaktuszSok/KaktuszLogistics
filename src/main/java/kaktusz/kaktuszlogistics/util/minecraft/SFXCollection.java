package kaktusz.kaktuszlogistics.util.minecraft;

import kaktusz.kaktuszlogistics.util.MathsUtils;
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
		where = fixLocationIfBlock(where);
		for(SoundEffect s : sfx) {
			s.play(where);
		}
	}

	/**
	 * @param pitchLerp Interpolates pitch between pitchMin and pitchMax
	 */
	public void playAll(Location where, float pitchLerp) {
		where = fixLocationIfBlock(where);
		for(SoundEffect s : sfx) {
			s.play(where, pitchLerp);
		}
	}

	public void playRandom(Location where) {
		where = fixLocationIfBlock(where);
		MathsUtils.chooseRandom(sfx).play(where);
	}

	/**
	 * @param pitchLerp Interpolates pitch between pitchMin and pitchMax
	 */
	public void playRandom(Location where, float pitchLerp) {
		where = fixLocationIfBlock(where);
		MathsUtils.chooseRandom(sfx).play(where, pitchLerp);
	}

	/**
	 * Snaps the location to the centre of the block if it is deemed to be a block location
	 * @return The location at the centre of the block if it was a block location, otherwise the unmodified input location
	 */
	private Location fixLocationIfBlock(Location where) {
		//check if location is a block position
		if(where.getPitch() == 0 && where.getYaw() == 0
				&& where.getX() % 1d < 0.01d
				&& where.getY() % 1d < 0.01d
				&& where.getZ() % 1d < 0.01d)
			return where.clone().add(0.5d, 0.5d, 0.5d); //convert to centre of block
		return where;
	}
}
