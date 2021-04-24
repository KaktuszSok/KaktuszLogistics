package kaktusz.kaktuszlogistics.commands;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GiveSubcommand extends Subcommand {
    public GiveSubcommand(String name) {
        super(name);
    }

    static final int ARGS_MIN = 3;
    static final int ARGS_MAX = 4;
    static final int PLAYER_ARG = 1;
    static final int ITEM_ARG = 2;
    static final int COUNT_ARG = 3;

    @Override
    public String usage() {
        return "<player> <item> [<count>], where <item> is a custom KL item type.";
    }

    @Override
    public boolean runCommand(CommandSender commandSender, String[] args) {
        if(args.length < ARGS_MIN || args.length > ARGS_MAX) {
            sendErrorMessage(commandSender, "Invalid amount of arguments!");
            sendUsageMessage(commandSender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[PLAYER_ARG]);
        if(target == null || !target.isOnline()) {
            sendErrorMessage(commandSender, args[PLAYER_ARG] + " is an invalid player!");
            return true;
        }

        CustomItem item = CustomItemManager.tryGetItem(args[ITEM_ARG]);
        if(item == null) {
            sendErrorMessage(commandSender, args[ITEM_ARG] + " is not a registered KL item!");
            return true;
        }

        int amount = 1;
        if(args.length == COUNT_ARG+1) {
            String amountStr = args[COUNT_ARG];
            if(!NumberUtils.isNumber(amountStr)) {
                sendErrorMessage(commandSender,"<count> must be an integer in the range [1,64]!");
                return true;
            }
            amount = Integer.parseInt(amountStr);
            if(amount < 1 || amount > 64) {
                sendErrorMessage(commandSender,"<count> must be an integer in the range [1,64]!");
                return true;
            }
        }
        ItemStack stack = item.createStack(amount);
        target.getInventory().addItem(stack);

        return true;
    }

    @Override
    public List<String> getAutocompletes(CommandSender sender, String[] args) {
        List<String> autocompletes = new ArrayList<>();

        switch (args.length-1) {
            case PLAYER_ARG:
                for(Player p : Bukkit.getOnlinePlayers()) {
                    autocompletes.add(p.getName());
                }
                return autocompletes;
            case ITEM_ARG:
                return new ArrayList<>(CustomItemManager.CUSTOM_ITEMS.keySet());
            default:
                return autocompletes;
        }
    }

    @Override
    public String getArgumentName(CommandSender sender, String[] args) {
        switch (args.length-1) {
            case PLAYER_ARG:
                return "<player>";
            case ITEM_ARG:
                return "<item>";
            case COUNT_ARG:
                return "[<count>]";
            default:
                return null;
        }
    }
}
