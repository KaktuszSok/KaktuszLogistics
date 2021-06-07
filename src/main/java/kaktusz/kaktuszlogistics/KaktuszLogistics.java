package kaktusz.kaktuszlogistics;

import kaktusz.kaktuszlogistics.commands.CommandManager;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.events.ItemEventsListener;
import kaktusz.kaktuszlogistics.modules.KModule;
import kaktusz.kaktuszlogistics.modules.survival.multiblocks.MultiblockMachine;
import kaktusz.kaktuszlogistics.modules.weaponry.input.PlayerContinuousShootingManager;
import kaktusz.kaktuszlogistics.projectile.ProjectileManager;
import kaktusz.kaktuszlogistics.util.minecraft.config.ConfigManager;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.KLWorld;
import kaktusz.kaktuszlogistics.world.WorldEventsListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

/**
 * Main class for KL plugin
 */
public class KaktuszLogistics extends JavaPlugin {

    public static KaktuszLogistics INSTANCE;
    public static Logger LOGGER;

    public final ConfigManager config = new ConfigManager();

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = getLogger();
        config.initialise();

        //init some keys
        MultiblockMachine.CHOSEN_RECIPE_KEY = new NamespacedKey(this, "ChosenRecipe");
        MultiblockMachine.PROCESSING_INPUTS_KEY = new NamespacedKey(this, "ProcessingInputs");
        MultiblockMachine.HALTED_KEY = new NamespacedKey(this, "Halted");
        MultiblockMachine.TIME_LEFT_KEY = new NamespacedKey(this, "TimeLeft");

        VanillaUtils.initialiseTickTime();
        CustomItemManager.initialise(); //init items

        initModules();
        registerCommands();
        registerListeners();

        boolean enableKWeaponry = KModule.WEAPONRY.isEnabled.value;

        new BukkitRunnable() {
            @Override
            public void run() {
                //run these every tick:
                VanillaUtils.incrementTickTime();
                if(enableKWeaponry)
                    PlayerContinuousShootingManager.onTick();
                ProjectileManager.onTick();
                KLWorld.onTick();
            }
        }.runTaskTimer(this, 0, 1);
    }

    @Override
    public void onDisable() {
        ProjectileManager.despawnAll();
        KLWorld.saveAllLoadedWorlds();
    }

    private void initModules() {
        for (KModule module : KModule.values()) {
            if(module.isEnabled.value)
                module.instance.initialise();
        }
    }

    private void registerCommands() {
        CommandManager.RegisterAllCommands(this);
    }

    private void registerListeners() {
        //items
        getServer().getPluginManager().registerEvents(new ItemEventsListener(), this);
        //world
        WorldEventsListener worldEvents = new WorldEventsListener();
        getServer().getPluginManager().registerEvents(worldEvents, this);
        worldEvents.loadPreloadedChunks();
    }
}
