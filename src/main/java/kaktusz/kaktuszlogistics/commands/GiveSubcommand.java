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
        arguments = new CommandArg[]{
                new CommandArg("<player>", ArgumentType.PLAYER),
                new CommandArg("<item>", ArgumentType.CUSTOM_ITEM),
                new CommandArg("[<count>]", ArgumentType.INT)};
    }

    static final int PLAYER_ARG = 1;
    static final int ITEM_ARG = 2;
    static final int COUNT_ARG = 3;

    @Override
    public String usageSuffix() {
        return ", where <item> is a custom KL item type.";
    }

    @Override
    public int getMinArgs() {
        return ITEM_ARG+1;
    }

    @SuppressWarnings("ConstantConditions") //validateParsing makes sure we don't get a NullReferenceException
    @Override
    public boolean runCommand(CommandSender commandSender, String[] args) {
        Player target = Bukkit.getPlayer(args[PLAYER_ARG]);
        CustomItem item = CustomItemManager.tryGetItem(args[ITEM_ARG]);
        Integer amount = 1;
        if(args.length > COUNT_ARG)
            amount = parseInt(args[COUNT_ARG]);

        if(!validateParsing(commandSender, args, target, item, amount)) //this will send the user an error message if it fails
            return true;

        if(amount < 1 || amount > 64) {
            sendErrorMessage(commandSender, arguments[COUNT_ARG-1].argName + " must be in the range [1,64]!");
            return true;
        }

        ItemStack stack = item.createStack(amount);
        target.getInventory().addItem(stack);

        return true;
    }
}
