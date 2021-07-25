package kaktusz.kaktuszlogistics.items.properties.multiblock;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.util.minecraft.SFXCollection;
import kaktusz.kaktuszlogistics.util.minecraft.SoundEffect;
import kaktusz.kaktuszlogistics.world.multiblock.CustomSupportedBlock;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockAABB;
import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;

public class SupportedBlockProperty extends MultiblockTemplate {
	public SupportedBlockProperty(CustomItem item) {
		super(item);
		setDamageSound(new SFXCollection(
				new SoundEffect(Sound.ENTITY_ITEM_BREAK, 0.3f, 0.75f),
				new SoundEffect(Sound.ENTITY_ITEM_BREAK, 0.45f, 1.8f)));
	}

	@Override
	public boolean verifyStructure(MultiblockBlock block) {
		return block.update();
	}

	@Override
	public BlockAABB getAABB(MultiblockBlock multiblock) {
		if(!(multiblock instanceof CustomSupportedBlock)) {
			KaktuszLogistics.LOGGER.warning("SupportedBlockProperty used on multiblock of wrong type! This should not happen.");
			return null;
		}

		CustomSupportedBlock block = (CustomSupportedBlock)multiblock;

		BlockFace supportedFace = block.getSupportedFace();
		BlockPosition selfPos = new BlockPosition(block.getLocation());
		return BlockAABB.fromAnyCorners(selfPos,
				new BlockPosition(selfPos.x+supportedFace.getModX(), selfPos.y+supportedFace.getModY(), selfPos.z+supportedFace.getModZ()));
	}
}
