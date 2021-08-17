package me.timwastaken.speedmission.commands;

import me.timwastaken.speedmission.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScoreCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 3) {
            sender.sendMessage(GameManager.PREFIX + ChatColor.RED + "You entered the wrong format");
            return false;
        } else {
            Player p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                sender.sendMessage(GameManager.PREFIX + ChatColor.RED + "Couldn't find the player you're looking for");
                return false;
            } else if (!"addremoveset".contains(args[1])) {
                sender.sendMessage(GameManager.PREFIX + ChatColor.RED + "You entered the wrong format");
                return false;
            } else if (!GameManager.playerInGame(p.getUniqueId())) {
                sender.sendMessage(GameManager.PREFIX + ChatColor.RED + "This player isn't currently playing");
            } else {
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(GameManager.PREFIX + ChatColor.RED + "You entered the wrong format");
                    return false;
                }
                if (args[1].equals("add")) {
                    GameManager.setScore(p.getUniqueId(), GameManager.getScore(p.getUniqueId()) + amount);
                    sender.sendMessage(GameManager.PREFIX + ChatColor.GREEN + p.getName() + "'s new score is " + GameManager.getScore(p.getUniqueId()));
                } else if (args[1].equals("remove")) {
                    GameManager.setScore(p.getUniqueId(), GameManager.getScore(p.getUniqueId()) - amount);
                    sender.sendMessage(GameManager.PREFIX + ChatColor.GREEN + p.getName() + "'s new score is " + GameManager.getScore(p.getUniqueId()));
                } else if (args[1].equals("set")) {
                    GameManager.setScore(p.getUniqueId(), Math.max(0, amount));
                    sender.sendMessage(GameManager.PREFIX + ChatColor.GREEN + p.getName() + "'s new score is " + GameManager.getScore(p.getUniqueId()));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> completions = new ArrayList<>();

        if (!GameManager.isGameRunning()) return completions;
        switch (args.length) {
            case 1:
                for (UUID id : GameManager.registeredPlayers) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) {
                        completions.add(p.getName());
                    }
                }
                break;
            case 2:
                completions.add("add");
                completions.add("remove");
                completions.add("set");
                break;
        }
        return completions;

    }
}
