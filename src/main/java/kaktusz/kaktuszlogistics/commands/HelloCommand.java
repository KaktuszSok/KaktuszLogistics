package kaktusz.kaktuszlogistics.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelloCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        commandSender.sendMessage("Hello " + commandSender.getName() + "!!");

        return true;
    }
}
