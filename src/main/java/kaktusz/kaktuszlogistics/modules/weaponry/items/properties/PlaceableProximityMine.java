package kaktusz.kaktuszlogistics.modules.weaponry.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.multiblock.SupportedBlockProperty;
import kaktusz.kaktuszlogistics.modules.weaponry.blocks.ProximityMineBlock;
import kaktusz.kaktuszlogistics.util.minecraft.SFXCollection;
import kaktusz.kaktuszlogistics.util.minecraft.SoundEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.meta.ItemMeta;

public class PlaceableProximityMine extends SupportedBlockProperty {

	private float explosionPower = 2.5f;

	public PlaceableProximityMine(CustomItem item) {
		super(item);
		setDamageSound(new SFXCollection(
			new SoundEffect(Sound.BLOCK_GRAVEL_BREAK, 0.7f, 0.925f),
			new SoundEffect(Sound.ENTITY_CREEPER_DEATH, 0.7f, 1.35f)
		));
	}

	/**
	 * Set the explosion power for this type of mine
	 */
	public PlaceableProximityMine setExplosionPower(float explosionPower) {
		this.explosionPower = explosionPower;

		return this;
	}

	public float getExplosionPower() {
		return explosionPower;
	}

	@Override
	public ProximityMineBlock createCustomBlock(ItemMeta stackMeta, Location location) {
		return new ProximityMineBlock(this, location, stackMeta);
	}
}
