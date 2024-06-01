package me.timwastaken.speedmission;

import me.timwastaken.speedmission.inferfaces.ObjectiveType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {

    private static JavaPlugin plugin;
    public static ArrayList<UUID> registeredPlayers;
    private static ConcurrentHashMap<UUID, Integer> playerScores;
    private static ConcurrentHashMap<UUID, Boolean> finishedTask;
    private static Pair<ObjectiveType, Object> currentObjective;

    private static ConcurrentHashMap<UUID, Integer> offlineRegistered;

    public static ConcurrentHashMap<UUID, Object> rememberState = new ConcurrentHashMap<>();

    public static World gameWorld;

    private static String criteriaText;

    private static int timeLimitTicks;
    private static long pTimeRemaining = -1;
    private static long objectiveStartTime;
    public static int MINUTES = 20 * 60;
    public static int SECONDS = 20;
    public static String PREFIX = ChatColor.WHITE + "[" + ChatColor.DARK_PURPLE + "SpeedMissions" + ChatColor.WHITE + "] ";

    private static int scoreToAdd;
    private static int lastScore;
    private static long currentTick = 0;
    private static long lastTaskTick = 0;

    private static boolean GAME_RUNNING = false;

    public static void initiate(JavaPlugin plugin) {
        GameManager.plugin = plugin;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule keepInventory true");
    }

    public static boolean startGame() {
        if (GAME_RUNNING) return false;
        GAME_RUNNING = true;
        playerScores = new ConcurrentHashMap<>();
        registeredPlayers = new ArrayList<>();
        currentObjective = new Pair<>();
        finishedTask = new ConcurrentHashMap<>();
        offlineRegistered = new ConcurrentHashMap<>();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule keepInventory true");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule doDaylightCycle true");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setInvulnerable(false);
            p.setFoodLevel(30);
            registeredPlayers.add(p.getUniqueId());
            playerScores.put(p.getUniqueId(), 0);
            finishedTask.put(p.getUniqueId(), false);
        }
        GameManager.registerNewObjective();

        new BukkitRunnable() {
            @Override
            public void run() {
                long timeRemaining = timeLimitTicks * 50L - (System.currentTimeMillis() - objectiveStartTime);

                Scoreboard sc = GameStateScoreboard(timeRemaining);

                if (timeRemaining <= 0) {
                    GameManager.nextObjective();
                    sc = GameStateScoreboard(0);
                }
                if (Math.floor(timeRemaining / 1000f) != Math.floor(pTimeRemaining / 1000f)) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.setScoreboard(sc);
                    }
                }

                GameManager.checkProgress();

                pTimeRemaining = timeRemaining;
                currentTick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    private static void registerNewObjective() {
        ObjectiveManager.generateRandomObjective();
        scoreToAdd = 5;
        lastScore = scoreToAdd;
        objectiveStartTime = System.currentTimeMillis();
        for (UUID id : registeredPlayers) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                p.sendTitle(
                        ChatColor.DARK_PURPLE + "Your mission:",
                        ChatColor.GREEN + criteriaText,
                        5,
                        100,
                        5
                );
            }
        }
        Bukkit.broadcastMessage(GameManager.PREFIX + ChatColor.LIGHT_PURPLE + criteriaText);
    }

    public static void nextObjective() {
        for (UUID id : finishedTask.keySet()) {
            finishedTask.put(id, false);
        }
        GameManager.registerNewObjective();
    }

    public static Pair<ObjectiveType, Object> getObjective() {
        return GameManager.currentObjective;
    }

    public static void setObjectiveCriteria(Object newCriteria) {
        GameManager.currentObjective.setValue(newCriteria);
    }

    public static void setObjectiveType(ObjectiveType newType) {
        GameManager.currentObjective.setKey(newType);
    }

    public static int getScore(UUID id) {
        return GameManager.playerScores.get(id);
    }

    public static void setScore(UUID id, int score) {
        GameManager.playerScores.put(id, score);
    }

    public static void setObjective(ObjectiveType newType, Object newCriteria, int timeLimitTicks) {
        GameManager.timeLimitTicks = timeLimitTicks;
        GameManager.setObjectiveType(newType);
        GameManager.setObjectiveCriteria(newCriteria);
        String str = ObjectiveManager.instructionText(currentObjective);
        if (str.contains("%s")) {
            criteriaText = String.format(str, ObjectiveManager.formatCriteria(newCriteria));
        } else {
            criteriaText = str;
        }
    }

    public static void kickPlayer(UUID id) {
        registeredPlayers.remove(id);
        offlineRegistered.put(id, playerScores.get(id));
        playerScores.remove(id);
    }

    public static boolean rejoinPlayer(UUID id) {
        if (offlineRegistered.containsKey(id)) {
            registeredPlayers.add(id);
            playerScores.put(id, offlineRegistered.get(id));
            offlineRegistered.remove(id);
            return true;
        }
        return false;
    }

    public static void finishTask(Player p) {
        if (!finishedTask.get(p.getUniqueId())) {
            int score = Math.max(0, scoreToAdd);
            if (currentTick == lastTaskTick) {
                score = lastScore;
            } else {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.playSound(online.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
                }
            }
            GameManager.setScore(p.getUniqueId(), GameManager.getScore(p.getUniqueId()) + score);
            Bukkit.broadcastMessage(GameManager.PREFIX + ChatColor.YELLOW + p.getName() + ChatColor.GOLD + " finished the mission " + ChatColor.GREEN + "[+" + score + "]");
            finishedTask.put(p.getUniqueId(), true);
            lastScore = score;
            scoreToAdd--;

            int finishedAmount = 0;
            for (boolean bool : finishedTask.values()) {
                if (bool) finishedAmount++;
            }
            if (finishedAmount >= registeredPlayers.size()) {
                GameManager.nextObjective();
            }

            lastTaskTick = currentTick;
        }
    }

    public static void resetStates(ObjectiveType type) {
        rememberState = new ConcurrentHashMap<>();
        switch (type) {
            case TraverseBlocks:
                for (UUID id : registeredPlayers) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) {
                        rememberState.put(id, LocationXY(p.getLocation()));
                    }
                }
                break;
            case PlaceBlocks:
            case DieMultipleTimes:
                for (UUID id : registeredPlayers) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) {
                        rememberState.put(id, 0);
                    }
                }
                break;
        }
    }

    public static void showHotbarTitle(Player p, String text) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(text).color(ChatColor.GOLD.asBungee()).build());
    }

    public static Location LocationXY(Location loc) {
        loc.setY(0);
        return loc;
    }

    private static Scoreboard GameStateScoreboard(long timeMillisRemaining) {
        int playerAmount = Math.min(8, GameManager.registeredPlayers.size());

        Scoreboard currentBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = currentBoard.registerNewObjective("gameState", "dummy", ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "SpeedMissions");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score score0 = obj.getScore("");
        score0.setScore(playerAmount + 6);
        Score score1 = obj.getScore(ChatColor.GREEN + "" + ChatColor.BOLD + "Current Objective:");
        score1.setScore(playerAmount + 5);
        Score score2 = obj.getScore(ChatColor.AQUA + (criteriaText.length() > 38 ? criteriaText.substring(0, 35).trim() + "..." : criteriaText));
        score2.setScore(playerAmount + 4);
        Score score3 = obj.getScore(" ");
        score3.setScore(playerAmount + 3);
        HashMap<UUID, Integer> sortedScores = (HashMap<UUID, Integer>) sortByValue(playerScores);
        UUID[] ids = sortedScores.keySet().toArray(new UUID[0]);
        for (int i = 0; i < Math.min(ids.length, 8); i++) {
            UUID id = ids[i];
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                Score sc = obj.getScore(ChatColor.GRAY + (playerScores.get(id)).toString() + " " + ChatColor.LIGHT_PURPLE + p.getName() + (GameManager.finishedTask.get(id) ? ChatColor.GREEN + " ✔" : ChatColor.RED + " ✘"));
                sc.setScore(i + 3);
            }
        }
        Score score4 = obj.getScore("  ");
        score4.setScore(2);
        Score score5 = obj.getScore(ChatColor.GOLD + "Time remaining");
        score5.setScore(1);
        String timeStr = DurationFormatUtils.formatDuration(timeMillisRemaining, "mm:ss");
        Score score6 = obj.getScore(timeStr);
        score6.setScore(0);

        return currentBoard;
    }

    public static boolean isGameRunning() {
        return GAME_RUNNING;
    }

    public static ObjectiveType getObjectiveType() {
        return currentObjective.getKey();
    }

    public static Object getObjectiveCriteria() {
        return currentObjective.getValue();
    }

    public static boolean playerInGame(UUID id) {
        return registeredPlayers.contains(id);
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private static void checkProgress() {
        switch (currentObjective.getKey()) {
            case ObtainItem:
                for (UUID id : registeredPlayers) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) {
                        if (p.getInventory().contains((Material) currentObjective.getValue())) {
                            finishTask(p);
                        }
                    }
                }
                break;
            case KillPlayer:
                for (UUID id : GameManager.registeredPlayers) {
                    Player self = Bukkit.getPlayer(id);
                    if (self != null) {
                        self.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1, true, false, false));
                        Pair<Player, Double> nearest = new Pair<>();
                        for (UUID otherID : GameManager.registeredPlayers) {
                            if (otherID != id) {
                                Player other = Bukkit.getPlayer(otherID);
                                if (other != null) {
                                    double dist = self.getLocation().distance(other.getLocation());
                                    if (nearest.getKey() != null) {
                                        if (nearest.getValue() > dist) {
                                            nearest.setKey(other);
                                            nearest.setValue(dist);
                                        }
                                    } else {
                                        nearest.setKey(other);
                                        nearest.setValue(dist);
                                    }
                                }
                            }
                        }
                        GameManager.showHotbarTitle(self, ChatColor.GOLD + "Nearest player: " + nearest.getKey().getName() + "   Distance: " + Math.round(nearest.getValue() * 10) / 10f + "m");
                    }
                }
                break;
            case GetEffect:
                for (UUID id : registeredPlayers) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) {
                        for (PotionEffect activeEffect : p.getActivePotionEffects()) {
                            if (activeEffect.getType().equals(GameManager.getObjectiveCriteria())) {
                                GameManager.finishTask(p);
                            }
                        }
                    }
                }
                break;
            case StandOnBlock:
                for (UUID id : registeredPlayers) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) {
                        Location ploc = p.getLocation();
                        if (ploc.getBlock().getType().equals(GameManager.getObjectiveCriteria()))
                            finishTask(p);
                        ploc.add(0, -1, 0);
                        if (ploc.getBlock().getType().equals(GameManager.getObjectiveCriteria()))
                            finishTask(p);
                    }
                }
                break;
            case DoClutch:
                break;
            case EnterDimension:
                for (UUID id : registeredPlayers) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) {
                        if (p.getWorld().getEnvironment().equals(GameManager.getObjectiveCriteria()))
                            finishTask(p);
                    }
                }
                break;
            case PlaceBlocks:
                for (UUID id : registeredPlayers) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) {
                        int amount = (int) GameManager.rememberState.get(p.getUniqueId());
                        GameManager.showHotbarTitle(p, "Blocks placed: " + Math.min(amount, (int) GameManager.getObjectiveCriteria()) + " / " + GameManager.getObjectiveCriteria());
                    }
                }
                break;
        }
    }

}
