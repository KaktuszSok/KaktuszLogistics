package kaktusz.kaktuszlogistics.modules.weaponry.input;

import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerReloadManager implements Listener {

	private static final Map<Player, Long> playerReloadFinishedMap = new HashMap<>();

	public static void startReload(Player player, int ticks) {
		long endTime = VanillaUtils.getTickTime() + ticks;
		playerReloadFinishedMap.put(player, endTime);
	}

	public static boolean isPlayerReloading(Player player) {
		return VanillaUtils.getTickTime() < getPlayerReloadFinishedTime(player);
	}

	/**
	 * Gets tick time when the specified player will be finished reloading
	 */
	private static long getPlayerReloadFinishedTime(Player player) {
		Long result = playerReloadFinishedMap.get(player);
		if(result == null)
			return 0;

		return result;
	}

	@EventHandler
	public void onPlayerLeft(PlayerQuitEvent e) {
		playerReloadFinishedMap.remove(e.getPlayer()); //clean up players that leave
	}

}
