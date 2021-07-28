package kaktusz.kaktuszlogistics.modules.weaponry.input;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemProperty;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages shooting and reloading guns
 */
public class GunActionsManager implements Listener {

	private static final Map<Entity, Long> entityReloadFinishedMap = new HashMap<>();

	/**
	 * Starts reloading for the given entity
	 */
	public static void startReload(Entity entity, int ticks) {
		long endTime = VanillaUtils.getTickTime() + ticks;
		entityReloadFinishedMap.put(entity, endTime);
	}

	/**
	 * @return True if the entity is currently reloading
	 */
	public static boolean isReloading(Entity entity) {
		return VanillaUtils.getTickTime() < getReloadFinishedTime(entity);
	}

	/**
	 * Gets tick time when the specified entity will be finished reloading
	 */
	private static long getReloadFinishedTime(Entity entity) {
		Long result = entityReloadFinishedMap.get(entity);
		if(result == null)
			return 0;

		return result;
	}

	//EVENTS
	@EventHandler(ignoreCancelled = true)
	public void onTriggerHeld(PlayerTriggerHeldEvent e) {
		ItemStack main = e.getPlayer().getInventory().getItemInMainHand();
		ItemStack secondary = e.getPlayer().getInventory().getItemInOffHand();
		onTriggerHeldForItem(e, main);
		onTriggerHeldForItem(e, secondary);
	}
	private void onTriggerHeldForItem(PlayerTriggerHeldEvent e, ItemStack stack) {
		CustomItem customItem = CustomItem.getFromStack(stack);
		if(customItem == null)
			return;
		//item
		if(customItem instanceof ITriggerHeldListener)
			((ITriggerHeldListener)customItem).onTriggerHeld(e, stack);
		//properties
		for(ItemProperty p : customItem.getAllProperties()) {
			if(p instanceof ITriggerHeldListener) {
				((ITriggerHeldListener)p).onTriggerHeld(e, stack);
			}
		}
	}

	//cleanup events
	@EventHandler(ignoreCancelled = true)
	public void onPlayerLeft(PlayerQuitEvent e) {
		entityReloadFinishedMap.remove(e.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDied(EntityDeathEvent e) {
		entityReloadFinishedMap.remove(e.getEntity());
	}
}
