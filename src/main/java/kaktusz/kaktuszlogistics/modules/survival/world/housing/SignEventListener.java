package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.items.PolymorphicItem;
import kaktusz.kaktuszlogistics.world.CustomSignBlock;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Locale;

public class SignEventListener implements Listener {

	public static final PlaceableHouseSign HOUSE_SIGN_PROPERTY = new PolymorphicItem("houseSign", "House", Material.OAK_SIGN).getOrAddProperty(PlaceableHouseSign.class);
	public static final PlaceableGoodsSupplySign GOODS_SIGN_PROPERTY = new PolymorphicItem("goodsSign", "Goods Supplier", Material.OAK_SIGN).getOrAddProperty(PlaceableGoodsSupplySign.class);

	@EventHandler(ignoreCancelled = true)
	public void onSignWritten(SignChangeEvent e) {
		Block b = e.getBlock();
		KLWorld world = KLWorld.get(b.getWorld());
		if(world.getBlockAt(b.getX(), b.getY(), b.getZ()) != null)
			return; //ignore signs that are already custom blocks

		String firstLine = e.getLine(0);
		if(firstLine == null)
			return;

		if(b.getBlockData() instanceof WallSign) {
			switch (firstLine.toLowerCase(Locale.ROOT)) {
				case "house":
					setCustomSignBlock(world, HOUSE_SIGN_PROPERTY.createCustomBlock(HOUSE_SIGN_PROPERTY.item.createStack(1).getItemMeta(), b.getLocation()), b);
					return;
				case "goods":
					setCustomSignBlock(world, GOODS_SIGN_PROPERTY.createCustomBlock(GOODS_SIGN_PROPERTY.item.createStack(1).getItemMeta(), b.getLocation()), b);
					return;
				default:
					break;
			}
		}
	}

	private void setCustomSignBlock(KLWorld world, CustomSignBlock cb, Block sign) {
		WallSign data = (WallSign) sign.getBlockData();
		world.setBlock(cb, sign.getX(), sign.getY(), sign.getZ());
		cb.setFacing(data.getFacing());
		cb.reverifyStructure();
	}

}
