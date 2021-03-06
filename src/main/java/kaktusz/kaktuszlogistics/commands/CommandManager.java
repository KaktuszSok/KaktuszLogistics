package kaktusz.kaktuszlogistics.commands;

import kaktusz.kaktuszlogistics.KaktuszLogistics;

public class CommandManager {

    @SuppressWarnings("ConstantConditions") //Method invocation 'setExecutor' may produce 'NullPointerException'. This is fine, we want to know if it goes wrong.
    public static void RegisterAllCommands(KaktuszLogistics main) {
        //KL Command
        main.getCommand(KLCommand.COMMAND_NAME).setExecutor(new KLCommand());
        KLCommand.registerSubcommand(new GiveSubcommand("give"));
        KLCommand.registerSubcommand(new GiveQSubcommand("giveq"));
    }
}
