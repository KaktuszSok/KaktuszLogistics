package kaktusz.kaktuszlogistics.commands;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.properties.ItemQuality;
import kaktusz.kaktuszlogistics.util.MathsUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveQSubcommand extends Subcommand {

    public GiveQSubcommand(String name) {
        super(name);
        arguments = new CommandArg[]{
                new CommandArg("<player>", ArgumentType.PLAYER),
                new CommandArg("<item>", ArgumentType.TIERED_ITEM),
                new CommandArg("<min_quality>", ArgumentType.FLOAT),
                new CommandArg("<max_quality>", ArgumentType.FLOAT),
                new CommandArg("[<count>]", ArgumentType.INT)};
    }

    static final int PLAYER_ARG = 1;
    static final int ITEM_ARG = 2;
    static final int MINQ_ARG = 3;
    static final int MAXQ_ARG = 4;
    static final int COUNT_ARG = 5;

    @Override
    public String usageSuffix() {
        return ", where <item> is a tiered KL item type. If no maximum quality is provided, the item will have exactly <min_quality> quality.";
    }

    @Override
    public int getMinArgs() {
        return MINQ_ARG+1;
    }

    @SuppressWarnings("ConstantConditions") //validateParsing makes sure we don't get a NullReferenceException
    @Override
    public boolean runCommand(CommandSender commandSender, String[] args) {
        Player target = Bukkit.getPlayer(args[PLAYER_ARG]);
        CustomItem item = CustomItemManager.tryGetItem(args[ITEM_ARG]);
        Float min_quality = parseFloat(args[MINQ_ARG]);
        //optional args:
        Float max_quality = min_quality;
        Integer amount = 1;
        if(args.length > MAXQ_ARG)
            max_quality = parseFloat(args[MAXQ_ARG]);
        if(args.length > COUNT_ARG)
            amount = parseInt(args[COUNT_ARG]);

        if(!validateParsing(commandSender, args, target, item, min_quality, max_quality, amount)) //this will send the user an error message if it fails
            return true;

        if(amount < 1 || amount > 1024) {
            sendErrorMessage(commandSender, arguments[COUNT_ARG-1].argName + " must be in the range [1,1024]!");
            return true;
        }

        for(int i = 0; i < amount; i++) {
            float q = MathsUtils.randomRange(min_quality, max_quality);
            ItemStack stack = item.createStack(1);
            item.findProperty(ItemQuality.class).setQuality(stack, q);
            target.getInventory().addItem(stack);
        }

        return true;
    }
}
