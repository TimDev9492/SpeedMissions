package me.timwastaken.speedmission.commands;

import me.timwastaken.speedmission.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkipCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (GameManager.isGameRunning()) {
            Bukkit.broadcastMessage(GameManager.PREFIX + ChatColor.BLUE + "Skipped current objective");
            GameManager.nextObjective();
            return true;
        } else {
            sender.sendMessage(GameManager.PREFIX + ChatColor.RED + "The game hasn't started yet. Use " + ChatColor.YELLOW + "/start" + ChatColor.RED + " to start it.");
            return false;
        }
    }
}
