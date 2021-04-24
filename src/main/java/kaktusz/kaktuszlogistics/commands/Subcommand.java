package kaktusz.kaktuszlogistics.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class Subcommand {
    public final String name;

    public Subcommand(String name) {
        this.name = name;
    }

    /**
     * Returns the string [usage] to be used in the message: "usage: /kl [name] [usage]".
     */
    public abstract String usage();

    /**
     * @param args arguments provided, INCLUDING the subcommand name but NOT the base command name
     */
    public abstract boolean runCommand(CommandSender commandSender, String[] args);

    protected void sendUsageMessage(CommandSender target) {
        sendErrorMessage(target, "Usage: /" + KLCommand.COMMAND_NAME + " " + name + " " + usage());
    }
    protected void sendErrorMessage(CommandSender target, String message) {
        target.sendMessage(ChatColor.RED + message);
    }

    public abstract List<String> getAutocompletes(CommandSender sender, String[] args);

    public abstract String getArgumentName(CommandSender sender, String[] args);
}
