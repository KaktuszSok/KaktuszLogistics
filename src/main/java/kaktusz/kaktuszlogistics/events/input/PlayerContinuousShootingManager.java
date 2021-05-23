package kaktusz.kaktuszlogistics.events.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerContinuousShootingManager {

	/**
	 * How long should the trigger be held for when an input event is received
	 */
	public static final int INPUT_EVENTS_DELAY = 4;
	public static Map<Player, Integer> playerTriggerTicks = new HashMap<>();

	public static void pullTrigger(Player player) {
		playerTriggerTicks.put(player, INPUT_EVENTS_DELAY);
	}

	public static void onTick() {
		List<Player> trackedPlayers = new ArrayList<>(playerTriggerTicks.keySet());
		int size = trackedPlayers.size();

		for (Player p : trackedPlayers) {
			if (!p.isOnline()) { //remove offline players
				playerTriggerTicks.remove(p);
			}

			//noinspection ConstantConditions
			playerTriggerTicks.compute(p, (k, v) -> v - 1); //decrement all players by 1
			if (playerTriggerTicks.get(p) <= 0) { //remove players once their trigger ticks expire
				playerTriggerTicks.remove(p);
			} else { //otherwise, call trigger held event
				PlayerTriggerHeldEvent event = new PlayerTriggerHeldEvent(p);
				Bukkit.getPluginManager().callEvent(event);
			}
		}
	}

}
