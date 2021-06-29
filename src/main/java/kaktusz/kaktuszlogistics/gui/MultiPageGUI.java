package kaktusz.kaktuszlogistics.gui;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import org.bukkit.entity.HumanEntity;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class MultiPageGUI extends InteractableGUI {

	private final CustomGUI previousGUI;

	/**
	 * @param previousGUI The GUI we return to upon closing this one
	 */
	public MultiPageGUI(int size, String title, CustomGUI previousGUI) {
		super(size, title);
		this.previousGUI = previousGUI;
	}

	@Override
	public void close(HumanEntity viewer) {
		if(previousGUI == null)
			super.close(viewer);
		else
			previousGUI.open(viewer, null);
	}

	@Override
	public void onClosed(HumanEntity viewer) {
		super.onClosed(viewer);

		if(previousGUI != null)
			new BukkitRunnable() {
				@Override
				public void run() {
					previousGUI.open(viewer, null);

				}
			}.runTask(KaktuszLogistics.INSTANCE);
	}
}
