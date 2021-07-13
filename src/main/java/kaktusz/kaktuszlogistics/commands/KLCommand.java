package kaktusz.kaktuszlogistics.commands;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class KLCommand implements CommandExecutor, TabCompleter {

    private static final Map<String, Subcommand> SUBCOMMANDS = new HashMap<>();
    public static final String COMMAND_NAME = "kl";

    public static void registerSubcommand(Subcommand sub) {
        SUBCOMMANDS.put(sub.name, sub);
        KaktuszLogistics.LOGGER.info("Registering command " + sub.name + " of type " + sub.toString());
    }

    private Subcommand tryGetSubcommand(String name) {
        return SUBCOMMANDS.getOrDefault(name, null);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length < 1) { //no subcommand specified
            printAvailableSubCommands(commandSender, command);
            return true;
        }

        //try execute subcommand
        Subcommand sub = tryGetSubcommand(args[0]);
        if(sub != null) {
            if(commandSender.hasPermission(command.getPermission() + "." + sub.name)) {
                if(args.length < sub.getMinArgs() || args.length > sub.arguments.length+1) { //invalid arg count
                    sendErrorMessage(commandSender, "Invalid amount of arguments!");
                    sub.sendUsageMessage(commandSender);
                    return true;
                }
                return sub.runCommand(commandSender, args);
            }
            else {
                sendErrorMessage(commandSender, command.getPermissionMessage());
            }
        }

        //no subcommand matched
        printAvailableSubCommands(commandSender, command);
        return true;
    }

    private void printAvailableSubCommands(CommandSender target, Command command) {
        Set<String> commandNames = SUBCOMMANDS.keySet();
        List<String> message = new ArrayList<>();

        message.add("Available subcommands: ");
        for(String commandName : commandNames) {
            if(target.hasPermission(command.getPermission() + "." + commandName))
                message.add(commandName);
        }

        target.sendMessage(message.toArray(new String[0]));
    }

    public static void sendErrorMessage(CommandSender target, String message) {
        target.sendMessage(ChatColor.RED + message);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {

        if(!command.getName().equalsIgnoreCase(COMMAND_NAME) || args.length < 1)
            return null; //wrong command

        //get autocompletes
        String lastArg = args[args.length-1];
        List<String> result = matchAutocompletes(lastArg, getAutocompletes(commandSender, command, args));
        if(!lastArg.isEmpty() && result.isEmpty()) { //what player is typing doesn't match any autocomplete. Show the argument name instead.
            String hint = getArgumentName(args);
            if(hint != null)
                result.add(hint);
        }
        return result;
    }

    private String getArgumentName(String[] args) {
        switch (args.length) {
            case 0:
                return null;
            case 1:
                return "<subcommand>";
            default: //else, ask subcommand for their arguments
                Subcommand sub = tryGetSubcommand(args[0]);
                if(sub == null)
                    return null;
                return sub.getArgumentName(args);
        }
    }

    private List<String> getAutocompletes(CommandSender commandSender, Command command, String[] args) {
        List<String> result = new ArrayList<>();

        switch (args.length) {
            case 0:
                return result;
            case 1:
                for(String key : SUBCOMMANDS.keySet()) {
                    if(commandSender.hasPermission(command.getPermission() + "." + key)) { //check perms
                        result.add(key);
                    }
                }
                return result; //show all available subcommands
            default: //2 or more args
                Subcommand sub = tryGetSubcommand(args[0]);
                if(sub != null && commandSender.hasPermission(command.getPermission() + "." + sub.name)) { //check perms
                    return sub.getAutocompletes(args); //show autocomplete of subcommand
                }
                return result;
        }
    }

    private List<String> matchAutocompletes(String lastArg, List<String> autocompletes) {
        List<String> result = new ArrayList<>();
        if(autocompletes == null)
            return result;
        for(String ac : autocompletes) {
            if(ac.toLowerCase().startsWith(lastArg.toLowerCase()))
                result.add(ac);
        }
        return result;
    }
}
