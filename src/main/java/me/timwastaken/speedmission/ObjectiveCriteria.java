package me.timwastaken.speedmission;

import me.timwastaken.speedmission.inferfaces.ClutchType;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.swing.text.html.parser.Entity;
import java.util.AbstractMap;

public class ObjectiveCriteria {

    public static Material material;
    public static Entity mob;
    public static Effect effect;
    public static EntityDamageEvent.DamageCause damageCause;
    public static AbstractMap.SimpleEntry<ClutchType, Integer> clutch;
    public static World.Environment dimension;
    public static Advancement advancement;
    public static int blockAmount;
    public static AbstractMap.SimpleEntry<EntityDamageEvent.DamageCause, Integer> deaths;

}
