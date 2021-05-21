package kaktusz.kaktuszlogistics;

import kaktusz.kaktuszlogistics.commands.CommandManager;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.GunItem;
import kaktusz.kaktuszlogistics.items.events.ItemEventsListener;
import kaktusz.kaktuszlogistics.items.events.input.PlayerContinuousShootingManager;
import kaktusz.kaktuszlogistics.projectiles.ProjectileManager;
import kaktusz.kaktuszlogistics.projectiles.rendering.ProjectileRenderer;
import kaktusz.kaktuszlogistics.util.VanillaUtils;
import kaktusz.kaktuszlogistics.world.WorldEventsListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class KaktuszLogistics extends JavaPlugin {

    public static KaktuszLogistics INSTANCE;
    public static Logger LOGGER;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = getLogger();

        VanillaUtils.initialiseTickTime();
        CustomItemManager.initialise(); //init items

        CommandManager.RegisterAllCommands(this);
        //register listeners
        WorldEventsListener worldEvents = new WorldEventsListener();
        getServer().getPluginManager().registerEvents(new ItemEventsListener(), this);
        getServer().getPluginManager().registerEvents(worldEvents, this);
        worldEvents.loadPreloadedChunks();

        new BukkitRunnable() {
            @Override
            public void run() {
                //run these every tick:
                VanillaUtils.incrementTickTime();
                PlayerContinuousShootingManager.onTick();
                ProjectileManager.onTick();
            }
        }.runTaskTimer(this, 0, 1);
    }

    @Override
    public void onDisable() {
        ProjectileManager.despawnAll();
    }
}
