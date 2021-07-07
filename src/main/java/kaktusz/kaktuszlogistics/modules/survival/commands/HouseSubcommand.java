package kaktusz.kaktuszlogistics.modules.survival.commands;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.commands.Subcommand;
import kaktusz.kaktuszlogistics.modules.survival.KaktuszSurvival;
import kaktusz.kaktuszlogistics.modules.survival.world.housing.HouseInfo;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HouseSubcommand extends Subcommand {
	public HouseSubcommand(String name) {
		super(name);
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public boolean runCommand(CommandSender commandSender, String[] args) {
		if(!(commandSender instanceof Player)) {
			sendErrorMessage(commandSender, "Command sender must be a player!");
			return true;
		}

		Player p = ((Player) commandSender).getPlayer();
		if(p == null) {
			sendErrorMessage(commandSender, "Failed - Player is null");
			return true;
		}

		if(KaktuszSurvival.CALC_ROOMS_ASYNC.getValue()) {
			Bukkit.getScheduler().runTaskAsynchronously(KaktuszLogistics.INSTANCE, () -> {
				String playerMessage = doHouseCheck(p);
				if (p.isOnline())
					p.sendMessage(playerMessage);
			});
		}
		else {
			String playerMessage = doHouseCheck(p);
			p.sendMessage(playerMessage);
		}

		return true;
	}

	private String doHouseCheck(Player p) {
		long startTime = System.currentTimeMillis();
		HouseInfo houseInfo = HouseInfo.calculateHouse(p.getWorld(), new VanillaUtils.BlockPosition(p.getLocation()));
		String msString = "[" + (System.currentTimeMillis() - startTime) + "ms] ";

		if(houseInfo == null)
			return msString + "You are not inside a house, or the house is too big";

		return msString + "You are inside a house:" +
				"\nAccessible floor area: " + houseInfo.getTotalFloorArea() + "m^2" +
				"\nBeds: " + houseInfo.getTotalBeds() +
				"\nAmount of rooms: " + houseInfo.rooms.size();
	}
}
