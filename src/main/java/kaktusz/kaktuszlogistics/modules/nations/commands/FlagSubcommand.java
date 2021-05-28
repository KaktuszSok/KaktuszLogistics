package kaktusz.kaktuszlogistics.modules.nations.commands;

import kaktusz.kaktuszlogistics.commands.Subcommand;
import kaktusz.kaktuszlogistics.modules.nations.KaktuszNations;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

public class FlagSubcommand extends Subcommand {
	public FlagSubcommand(String name) {
		super(name);
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public boolean runCommand(CommandSender commandSender, String[] args) {
		if(args.length > 1)
			return false;

		if(!(commandSender instanceof Player)) {
			sendErrorMessage(commandSender, "Command sender must be a player!");
			return true;
		}

		Player p = (Player)commandSender;
		ItemStack handItem = p.getInventory().getItemInMainHand();
		if(handItem.getItemMeta() == null || !(handItem.getItemMeta() instanceof BannerMeta)) {
			sendErrorMessage(commandSender, "You must be holding a banner to use this command");
			return true;
		}

		p.getInventory().addItem(KaktuszNations.FLAG_ITEM.createFlagFromBanner(handItem));

		return true;
	}
}
