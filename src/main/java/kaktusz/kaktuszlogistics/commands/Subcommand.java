package kaktusz.kaktuszlogistics.commands;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.TieredItem;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public abstract class Subcommand {

    @SuppressWarnings("unused")
    protected enum ArgumentType {
        STRING (String.class),
        INT (Integer.class),
        FLOAT (Float.class),
        BOOL (Boolean.class),
        PLAYER (Player.class),
        CUSTOM_ITEM (CustomItem.class),
        TIERED_ITEM (TieredItem.class);

        public final Class<?> type;
        ArgumentType(Class<?> type) {
            this.type = type;
        }

        public List<String> getAutocompletes() {
            List<String> result = new ArrayList<>();

            switch(this) {
                case BOOL:
                    result.add("true");
                    result.add("false");
                    break;
                case PLAYER:
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        result.add(p.getName());
                    }
                    break;
                case CUSTOM_ITEM:
                    result.addAll(CustomItemManager.CUSTOM_ITEMS.keySet());
                    break;
                case TIERED_ITEM:
                    for(String key : CustomItemManager.CUSTOM_ITEMS.keySet()) {
                        if(CustomItemManager.CUSTOM_ITEMS.get(key) instanceof TieredItem) {
                            result.add(key);
                        }
                    }
                    break;
            }

            return result;
        }
    }

    protected static class CommandArg {
        public String argName;
        public ArgumentType argType;

        public CommandArg(String n, ArgumentType t) {
            argName = n;
            argType = t;
        }
    }

    public final String name;
    protected CommandArg[] arguments;

    public Subcommand(String name) {
        this.name = name;
    }

    /**
     * Returns the string [usageArgs] to be used in the message: "usage: /kl [name] [usageArgs][usageSuffix]".
     */
    private String usageArgs() {
        StringJoiner result = new StringJoiner(" ", " ", "");
        for(CommandArg arg : arguments) {
            result.add(arg.argName);
        }
        return result.toString();
    }

    /**
     * Returns the string [usageSuffix] to be used in the message: "usage: /kl [name] [usage][usageSuffix]".
     */
    public String usageSuffix() {
        return "";
    }

    public abstract int getMinArgs();

    /**
     * @param args arguments provided, INCLUDING the subcommand name but NOT the base command name
     */
    public abstract boolean runCommand(CommandSender commandSender, String[] args);

    protected Integer parseInt(String input) {
        if(NumberUtils.isDigits(input))
            return Integer.parseInt(input);

        return  null;
    }

    protected Float parseFloat(String input) {
        if(NumberUtils.isNumber(input))
            return Float.parseFloat(input);

        return  null;
    }

    /**
     * Returns true if all objects are instances of their respective argument's type and are not null
     */
    protected boolean validateParsing(CommandSender errorTarget, String[] inputArgs, Object... parsedObjects) {
        if(parsedObjects.length < getMinArgs()) {
            KaktuszLogistics.LOGGER.warning("validateParsing failed - given " + parsedObjects.length + " objects when " + getMinArgs() + " expected.");
            return false;
        }

        for(int i = 0; i < inputArgs.length-1; i++) {
            ArgumentType t = arguments[i].argType;
            if(parsedObjects[i] == null || !t.type.isInstance(parsedObjects[i])) {
                sendErrorMessage(errorTarget, "Invalid input for argument " + arguments[i].argName + "! (" + inputArgs[i+1] + " must be " + t.toString() + ")");
                return false;
            }
        }

        return true;
    }

    protected void sendUsageMessage(CommandSender target) {
        sendErrorMessage(target, "Usage: /" + KLCommand.COMMAND_NAME + " " + name + usageArgs() + usageSuffix());
    }
    protected void sendErrorMessage(CommandSender target, String message) {
        KLCommand.sendErrorMessage(target, message);
    }

    public List<String> getAutocompletes(String[] args) {
        int currArg = args.length-1; //0 = subcommand name, 1 = first argument (arguments[0]), etc.
        if(currArg > 0 && currArg <= arguments.length) {
            return arguments[currArg-1].argType.getAutocompletes(); //return autocomplete based on arg type
        }
        return new ArrayList<>(); //if we are out of bounds, return empty list
    }

    public String getArgumentName(String[] args) {
        if(args.length > arguments.length+1) {
            return null;
        }
        return arguments[args.length-2].argName;
    }
}
