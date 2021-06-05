package kaktusz.kaktuszlogistics.modules.survival.commands;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.commands.Subcommand;
import kaktusz.kaktuszlogistics.modules.survival.KaktuszSurvival;
import kaktusz.kaktuszlogistics.modules.survival.world.housing.RoomInfo;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;

public class RoomSubcommand extends Subcommand {
	public RoomSubcommand(String name) {
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

		if(KaktuszSurvival.CALC_ROOMS_ASYNC.value) {
			Bukkit.getScheduler().runTaskAsynchronously(KaktuszLogistics.INSTANCE, () -> {
				String playerMessage = doRoomCheck(p);
				if (p.isOnline())
					p.sendMessage(playerMessage);
			});
		}
		else {
			String playerMessage = doRoomCheck(p);
			p.sendMessage(playerMessage);
		}

		return true;
	}

	/**
	 * @return The message that should be sent to the player
	 */
	private String doRoomCheck(Player p) {
		long startTime = System.currentTimeMillis();
		RoomInfo roomInfo = RoomInfo.calculateRoom(p.getWorld(), new VanillaUtils.BlockPosition(p.getLocation()), new HashMap<>(), new HashSet<>());
		String msString = "[" + (System.currentTimeMillis() - startTime) + "ms] ";

		if(roomInfo == null) {
			return msString + "You are not standing in a room, or the room is too big";
		}

		return msString + "You are standing in a room:" +
				"\nAccessible floor area: " + roomInfo.getFloorArea() + "m^2" +
				"\nBeds: " + roomInfo.getBeds() +
				"\nPossible connected rooms #: " + roomInfo.getPossibleConnectedRooms().size();
	}
}
