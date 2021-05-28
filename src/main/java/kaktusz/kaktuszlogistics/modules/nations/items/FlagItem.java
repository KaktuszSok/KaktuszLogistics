package kaktusz.kaktuszlogistics.modules.nations.items;

import kaktusz.kaktuszlogistics.items.CustomItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class FlagItem extends CustomItem {
	/**
	 * @param material MUST be a banner!!!
	 */
	public FlagItem(String type, String displayName, Material material) {
		super(type, displayName, material);
	}

	/**
	 * Creates a flag with the same pattern as the banner
	 * @param banner The source of the pattern
	 */
	public ItemStack createFlagFromBanner(ItemStack banner) {
		//get banner meta
		ItemMeta meta = banner.getItemMeta();
		if(!(meta instanceof BannerMeta))
			return null;
		BannerMeta bannerMeta = (BannerMeta) meta;

		//create flag and copy over patterns
		ItemStack flag = createStack(1);
		flag.setType(banner.getType());
		BannerMeta flagMeta = (BannerMeta)flag.getItemMeta();
		flagMeta.setPatterns(bannerMeta.getPatterns());
		flag.setItemMeta(flagMeta);

		return flag;
	}

	@Override
	protected void updateDisplay(ItemStack stack) {
		//don't update material - otherwise, the base banner colour gets reset to whatever arbitrary colour of banner we passed to the constructor
		updateDisplayName(stack);
		updateItemLore(stack);
	}
}
