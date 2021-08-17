package me.timwastaken.speedmission.listeners;

import me.timwastaken.speedmission.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.potion.PotionEffect;

public class GeneralListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!GameManager.isGameRunning()) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!GameManager.isGameRunning())
            event.getPlayer().setInvulnerable(true);
        else {
            Player p = event.getPlayer();
            if (GameManager.rejoinPlayer(p.getUniqueId())) {
                event.setJoinMessage(GameManager.PREFIX + ChatColor.WHITE + p.getName() + ChatColor.AQUA + " rejoined the game");
            } else {
                event.setJoinMessage(GameManager.PREFIX + ChatColor.WHITE + p.getName() + ChatColor.GRAY + " is now spectating");
                p.setGameMode(GameMode.SPECTATOR);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (!GameManager.isGameRunning()) return;
        Player p = event.getPlayer();
        if (GameManager.playerInGame(p.getUniqueId())) {
            GameManager.kickPlayer(p.getUniqueId());
            event.setQuitMessage(GameManager.PREFIX + ChatColor.WHITE + p.getName() + ChatColor.AQUA + " left the game");
        } else {
            event.setQuitMessage(GameManager.PREFIX + ChatColor.WHITE + p.getName() + ChatColor.GRAY + " stopped spectating");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!GameManager.isGameRunning())
            event.setCancelled(true);
    }

    @EventHandler
    public void onWorldLoaded(WorldLoadEvent event) {
        event.getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule doDaylightCycle false");

        GameManager.gameWorld = event.getWorld();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a times 0 60 0");
    }

    @EventHandler
    public void onDimensionChange(PlayerChangedWorldEvent event) {
        event.getPlayer().getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        event.getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
    }

}
