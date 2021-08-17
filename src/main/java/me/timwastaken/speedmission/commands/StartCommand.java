package me.timwastaken.speedmission.commands;

import me.timwastaken.speedmission.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean toReturn = GameManager.startGame();
        if (!toReturn) {
            sender.sendMessage(GameManager.PREFIX + ChatColor.RED + "The game is already running");
        }
        return toReturn;
    }
}
