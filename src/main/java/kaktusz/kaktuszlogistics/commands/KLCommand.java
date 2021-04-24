package kaktusz.kaktuszlogistics.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class KLCommand implements CommandExecutor, TabCompleter {

    @SuppressWarnings("FieldMayBeFinal")
    private static Map<String, Subcommand> SUBCOMMANDS = new HashMap<>();
    public static final String COMMAND_NAME = "kl";

    public static void registerSubcommand(Subcommand sub) {
        SUBCOMMANDS.put(sub.name, sub);
    }

    private Subcommand tryGetSubcommand(String name) {
        return SUBCOMMANDS.getOrDefault(name, null);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length < 1) { //no subcommand specified
            Set<String> commandNames = SUBCOMMANDS.keySet();
            String[] message = new String[commandNames.size() + 1];

            message[0] = "Available subcommands: ";
            int i = 1;
            for(String commandName : commandNames) {
                message[i] = commandName;
                i++;
            }

            commandSender.sendMessage(message);
            return true;
        }

        //try execute subcommand
        Subcommand sub = tryGetSubcommand(args[0]);
        if(sub != null) {
            return sub.runCommand(commandSender, args);
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {

        if(!command.getName().equalsIgnoreCase(COMMAND_NAME) || args.length < 1)
            return null; //wrong command

        //get autocompletes
        String lastArg = args[args.length-1];
        List<String> result = matchAutocompletes(lastArg, getAutocompletes(commandSender, command, alias, args));
        if(!lastArg.isEmpty() && result.isEmpty()) { //what player is typing doesn't match any autocomplete. Show the argument name instead.
            String hint = getArgumentName(commandSender, command, alias, args);
            if(hint != null)
                result.add(hint);
        }
        return result;
    }

    private String getArgumentName(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
                return null;
            case 1:
                return "<subcommand>";
            default: //else, ask subcommand for their arguments
                Subcommand sub = tryGetSubcommand(args[0]);
                if(sub == null)
                    return null;
                return sub.getArgumentName(commandSender, args);
        }
    }

    private List<String> getAutocompletes(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
                return new ArrayList<>();
            case 1:
                return new ArrayList<>(SUBCOMMANDS.keySet()); //show all subcommands
            default: //2 or more args
                Subcommand sub = tryGetSubcommand(args[0]);
                return sub.getAutocompletes(commandSender, args); //show autocomplete of subcommand
        }
    }

    private List<String> matchAutocompletes(String lastArg, List<String> autocompletes) {
        List<String> result = new ArrayList<>();
        if(autocompletes == null)
            return result;
        for(String ac : autocompletes) {
            if(ac.startsWith(lastArg))
                result.add(ac);
        }
        return result;
    }
}
