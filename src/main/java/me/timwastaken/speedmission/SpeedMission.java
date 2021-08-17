package me.timwastaken.speedmission;

import me.timwastaken.speedmission.commands.GameTimeCommand;
import me.timwastaken.speedmission.commands.ScoreCommand;
import me.timwastaken.speedmission.commands.SkipCommand;
import me.timwastaken.speedmission.commands.StartCommand;
import me.timwastaken.speedmission.listeners.GeneralListener;
import me.timwastaken.speedmission.listeners.ObjectiveListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpeedMission extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(new GeneralListener(), this);
        Bukkit.getPluginManager().registerEvents(new ObjectiveListener(), this);

        getCommand("start").setExecutor(new StartCommand());
        getCommand("skip").setExecutor(new SkipCommand());
        getCommand("gametime").setExecutor(new GameTimeCommand());
        getCommand("score").setExecutor(new ScoreCommand());

        GameManager.initiate(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /*
    TODO

    -distribute objective text over multiple lines
     */

}
