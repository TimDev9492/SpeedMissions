package me.timwastaken.speedmission.listeners;

import me.timwastaken.speedmission.GameManager;
import me.timwastaken.speedmission.inferfaces.ClutchType;
import me.timwastaken.speedmission.inferfaces.ObjectiveType;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class ObjectiveListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!GameManager.isGameRunning()) return;
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (GameManager.getObjectiveType().equals(ObjectiveType.ReceiveDamage)) {
                if (event.getCause().equals(GameManager.getObjectiveCriteria()))
                    GameManager.finishTask(p);
            }
            if (p.getHealth() - event.getFinalDamage() <= 0) {
                if (GameManager.getObjectiveType().equals(ObjectiveType.DieMultipleTimes)) {
                    int newAmount = (int) GameManager.rememberState.get(p.getUniqueId()) + 1;
                    GameManager.rememberState.put(p.getUniqueId(), newAmount);
                    if (newAmount >= (int) GameManager.getObjectiveCriteria()) {
                        GameManager.finishTask(p);
                    }
                } else if (!GameManager.getObjectiveType().equals(ObjectiveType.KillPlayer)) {
                    GameManager.setScore(p.getUniqueId(), Math.max(0, GameManager.getScore(p.getUniqueId()) - 1));
                    p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 1f);
                    p.sendMessage(GameManager.PREFIX + ChatColor.RED + "You died" + ChatColor.DARK_RED + " [-1]");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!GameManager.isGameRunning()) return;
        Player p = event.getPlayer();
        if (GameManager.getObjectiveType().equals(ObjectiveType.TraverseBlocks)) {
            // recalculate distance
            if (p.getWorld().getEnvironment().equals(((Location) GameManager.rememberState.get(p.getUniqueId())).getWorld().getEnvironment())) {
                double dist = GameManager.LocationXY(p.getLocation()).distance((Location) GameManager.rememberState.get(p.getUniqueId()));
                GameManager.showHotbarTitle(p, "Distance traveled: " + Math.round(dist * 10f) / 10f + " blocks");
                if (dist >= (int) GameManager.getObjectiveCriteria()) {
                    GameManager.finishTask(p);
                }
            } else {
                // reset distance
                GameManager.rememberState.put(p.getUniqueId(), GameManager.LocationXY(p.getLocation()));
            }
        } else if (GameManager.getObjectiveType().equals(ObjectiveType.DoClutch)) {
            p.getFallDistance();
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!GameManager.isGameRunning()) return;
        if (event.getEntity().getKiller() != null && event.getEntity().getType().isAlive()) {
            if (GameManager.getObjectiveType().equals(ObjectiveType.KillMob)) {
                if (event.getEntity().getType().equals(GameManager.getObjectiveCriteria())) {
                    GameManager.finishTask(event.getEntity().getKiller());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player p = event.getPlayer();
        if (GameManager.playerInGame(p.getUniqueId())) {
            if (GameManager.getObjectiveType().equals(ObjectiveType.TraverseBlocks)) {
                GameManager.rememberState.put(p.getUniqueId(), GameManager.LocationXY(event.getRespawnLocation()));
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!GameManager.isGameRunning()) return;
        Player dead = event.getEntity();
        if (dead.isDead()) {
            if (dead.getKiller() != null) {
                if (GameManager.getObjectiveType().equals(ObjectiveType.KillPlayer)) {
                    if (GameManager.playerInGame(dead.getUniqueId()) && GameManager.playerInGame(dead.getKiller().getUniqueId())) {
                        GameManager.finishTask(dead.getKiller());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPlaceBoat(EntitySpawnEvent event) {
        if (event.getEntity().getType().equals(EntityType.BOAT)) {
            Player p = (Player) event.getEntity().getWorld().getNearbyEntities(event.getLocation(), 8, 8, 8, (entity -> entity.getType().equals(EntityType.BOAT)));
        }
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        Material blockPlaced = event.getBlockPlaced().getType();
        if (blockPlaced.toString().endsWith("BOAT")) {
            Bukkit.broadcastMessage("Bloat placed");
        }
        if (!GameManager.isGameRunning()) return;
        Player p = event.getPlayer();
        if (GameManager.getObjectiveType().equals(ObjectiveType.PlaceBlocks)) {
            int newAmount = (int) GameManager.rememberState.get(p.getUniqueId()) + 1;
            if (newAmount >= (int) GameManager.getObjectiveCriteria()) {
                GameManager.finishTask(p);
            }
            GameManager.rememberState.put(p.getUniqueId(), newAmount);
        } else if (GameManager.getObjectiveType().equals(ObjectiveType.DoClutch)) {
            blockPlaced = event.getBlockPlaced().getType();
            switch ((ClutchType) GameManager.getObjectiveCriteria()) {
                case Boat:
                    if (blockPlaced.toString().endsWith("BOAT")) {

                    }
                    break;
                case Water:
                    break;
                case Cobweb:
                    break;
                case Ladder:
                    break;
                case SlimeBlock:
                    break;
                case TwistingVines:
                    break;
                case Scaffolding:
                    break;
            }
        }
    }

}
