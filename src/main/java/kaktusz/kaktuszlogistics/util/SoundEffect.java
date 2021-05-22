package kaktusz.kaktuszlogistics.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
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
		if(where.getWorld() == null)
			return;
		if(category == null)
			where.getWorld().playSound(where, sound, MathsUtils.randomRange(volMin, volMax), MathsUtils.randomRange(pitchMin, pitchMax));
		else
			where.getWorld().playSound(where, sound, category, MathsUtils.randomRange(volMin, volMax), MathsUtils.randomRange(pitchMin, pitchMax));
	}
}
