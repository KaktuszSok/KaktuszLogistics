package kaktusz.kaktuszlogistics;

import kaktusz.kaktuszlogistics.commands.CommandManager;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.ItemEventsListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class KaktuszLogistics extends JavaPlugin {

    public static KaktuszLogistics INSTANCE;
    public static Logger LOGGER;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = getLogger();

        CustomItemManager.initialise();
        CommandManager.RegisterAllCommands(this);
        getServer().getPluginManager().registerEvents(new ItemEventsListener(), this);
    }

    @Override
    public void onDisable() {

    }
}
