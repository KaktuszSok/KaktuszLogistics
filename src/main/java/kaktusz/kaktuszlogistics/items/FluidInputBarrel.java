package kaktusz.kaktuszlogistics.items;

import kaktusz.kaktuszlogistics.items.properties.PlaceableOpenBarrel;
import org.bukkit.Material;

public class FluidInputBarrel extends CustomItem {
	public FluidInputBarrel(String type, String displayName) {
		super(type, displayName, Material.BARREL);
		getOrAddProperty(PlaceableOpenBarrel.class);
	}
}
