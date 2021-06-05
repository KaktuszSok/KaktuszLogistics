package kaktusz.kaktuszlogistics.util.minecraft;

import kaktusz.kaktuszlogistics.util.MathsUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SoundEffect {

	public static final float LONG_RANGE_SOUND_THRESHOLD = 16;

	public final Sound sound;
	public final float volMin, volMax;
	public final float pitchMin, pitchMax;
	private SoundCategory category = null;

	public SoundEffect(Sound sound, float vol, float pitch) {
		this(sound, vol, vol, pitch, pitch);
	}
	public SoundEffect(Sound sound, float volMin, float volMax, float pitchMin, float pitchMax) {
		this.sound = sound;
		this.volMin = volMin;
		this.volMax = volMax;
		this.pitchMin = pitchMin;
		this.pitchMax = pitchMax;
	}

	public void setCategory(SoundCategory category) {
		this.category = category;
	}

	public void play(Location where) {
		play(where, MathsUtils.randomRange(0f, 1f));
	}

	/**
	 * @param pitchLerp Interpolates pitch between pitchMin and pitchMax
	 */
	public void play(Location where, float pitchLerp) {
		if(where.getWorld() == null)
			return;
		if(category == null)
			where.getWorld().playSound(where, sound, MathsUtils.randomRange(volMin, volMax), MathsUtils.lerpc(pitchMin, pitchMax, pitchLerp));
		else
			where.getWorld().playSound(where, sound, category, MathsUtils.randomRange(volMin, volMax), MathsUtils.lerpc(pitchMin, pitchMax, pitchLerp));
		Bukkit.broadcastMessage("playing " + sound.name() + " with pitch " + MathsUtils.lerpc(pitchMin, pitchMax, pitchLerp) + " (lerp=" + pitchLerp + ") and volume in range (" + volMin + "," + volMax + ")");
	}
}
