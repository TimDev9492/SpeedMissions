package me.timwastaken.speedmission;

import me.timwastaken.speedmission.inferfaces.ClutchType;
import me.timwastaken.speedmission.inferfaces.ObjectiveType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Random;

public final class ObjectiveManager {

    private static final Random random = new Random();

    public static void generateObjective(ObjectiveType objective) {
        switch (objective) {
            case ObtainItem:
                Material mat;
                do {
                    mat = (Material) ObjectiveManager.randomElementFrom(Material.values());
                } while (!mat.isItem());
                GameManager.setObjective(ObjectiveType.ObtainItem, mat, GameManager.MINUTES * 2);
                break;
            case KillPlayer:
                GameManager.setObjective(ObjectiveType.KillPlayer, 1, GameManager.MINUTES * 3);
                break;
            case KillMob:
                EntityType type;
                do {
                    type = (EntityType) ObjectiveManager.randomElementFrom(EntityType.values());
                } while (!type.isAlive() || type.equals(EntityType.GIANT) || type.equals(EntityType.PLAYER));
                GameManager.setObjective(ObjectiveType.KillMob, type, GameManager.MINUTES * 3);
                break;
            case GetEffect:
                PotionEffectType effect = (PotionEffectType) ObjectiveManager.randomElementFrom(PotionEffectType.values());
                GameManager.setObjective(ObjectiveType.GetEffect, effect, GameManager.MINUTES * 5);
                break;
            case StandOnBlock:
                Material block;
                do {
                    block = (Material) ObjectiveManager.randomElementFrom(Material.values());
                } while (!block.isBlock());
                GameManager.setObjective(ObjectiveType.StandOnBlock, block, GameManager.MINUTES * 3);
                break;
            case ReceiveDamage:
                EntityDamageEvent.DamageCause damage;
                do {
                    damage = (EntityDamageEvent.DamageCause) ObjectiveManager.randomElementFrom(EntityDamageEvent.DamageCause.values());
                } while (damage.equals(EntityDamageEvent.DamageCause.CUSTOM) ||
                        damage.equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) ||
                        damage.equals(EntityDamageEvent.DamageCause.MELTING) ||
                        damage.equals(EntityDamageEvent.DamageCause.DRYOUT) ||
                        damage.equals(EntityDamageEvent.DamageCause.FLY_INTO_WALL) ||
                        damage.equals(EntityDamageEvent.DamageCause.STARVATION) ||
                        damage.equals(EntityDamageEvent.DamageCause.SUICIDE) ||
                        damage.equals(EntityDamageEvent.DamageCause.THORNS) ||
                        damage.equals(EntityDamageEvent.DamageCause.VOID));
                GameManager.setObjective(ObjectiveType.ReceiveDamage, damage, GameManager.MINUTES * 2);
                break;
            case DoClutch:
                ObjectiveManager.generateRandomObjective();
                GameManager.setObjective(ObjectiveType.DoClutch, ObjectiveManager.randomElementFrom(ClutchType.values()), GameManager.MINUTES);
                break;
            case EnterDimension:
                GameManager.setObjective(ObjectiveType.EnterDimension, ObjectiveManager.randomElementFrom(World.Environment.values()), GameManager.MINUTES * 3);
                break;
            case TraverseBlocks:
                int blockAmount = (5 + random.nextInt(16)) * 100;
                GameManager.resetStates(ObjectiveType.TraverseBlocks);
                GameManager.setObjective(ObjectiveType.TraverseBlocks, blockAmount, Math.round(GameManager.SECONDS * blockAmount / 5f));
                break;
            case PlaceBlocks:
                int blocksToPlace = 8 + random.nextInt(64 * 4 + 1 - 8);
                GameManager.resetStates(ObjectiveType.PlaceBlocks);
                GameManager.setObjective(ObjectiveType.PlaceBlocks, blocksToPlace, Math.round(GameManager.SECONDS * blocksToPlace / 2f));
                break;
            case DieMultipleTimes:
                int deathAmount = 1;
                GameManager.setObjective(ObjectiveType.DieMultipleTimes, deathAmount, GameManager.SECONDS * 30 * deathAmount);
                GameManager.resetStates(ObjectiveType.DieMultipleTimes);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + objective);
        }
    }

    public static void generateRandomObjective() {
//        ObjectiveType objType;
//        do {
//            objType = (ObjectiveType) ObjectiveManager.randomElementFrom(ObjectiveType.values());
//        } while (objType.equals(ObjectiveType.DoClutch));
//        ObjectiveManager.generateObjective(objType);

        String ratios = "6:1:3:2:7:2:0:1:1:2:1";
        ArrayList<Integer> weights = new ArrayList<>();
        String[] parts = ratios.split(":");
        for (int i = 0; i < parts.length; i++) {
            int num = Integer.parseInt(parts[i]);
            for (int y = 0; y < num; y++) {
                weights.add(i);
            }
        }
        ObjectiveManager.generateObjective(ObjectiveType.values()[weights.get(random.nextInt(weights.size()))]);
    }

    private static Object randomElementFrom(Object[] array) {
        return array[random.nextInt(array.length)];
    }

    public static String formatCriteria(Object criteria) {
        String toReturn = criteria.toString();

        if (criteria instanceof PotionEffectType) {
            toReturn = ((PotionEffectType) criteria).getName().toLowerCase().replace("_", " ");
        } else if (criteria instanceof Material || criteria instanceof EntityDamageEvent.DamageCause || criteria instanceof EntityType) {
            toReturn = criteria.toString().toLowerCase().replace("_", " ");
        } else if (criteria instanceof World.Environment) {
            switch ((World.Environment) criteria) {
                case NORMAL:
                    toReturn = "overworld";
                    break;
                case NETHER:
                    toReturn = "nether";
                    break;
                case THE_END:
                    toReturn = "end";
                    break;
            }
        }

        return toReturn;
    }

    public static String instructionText(Pair<ObjectiveType, Object> currentObjective) {
        String criteria = ObjectiveManager.formatCriteria(currentObjective.getValue());

        String s = "aeiou".contains(criteria.substring(0, 1)) ? "n" : "";
        switch (currentObjective.getKey()) {
            case ObtainItem:
                return "Obtain item: %s";
            case KillPlayer:
//                return "Get " + criteria + " player kill" + ((int) currentObjective.getValue() > 1 ? "s" : "");
                return "Kill a player";
            case KillMob:
                return "Kill a" + s + " %s";
            case GetEffect:
                return "Get the %s effect";
            case StandOnBlock:
                return "Stand on top off a" + s + " %s";
            case ReceiveDamage:
                switch ((EntityDamageEvent.DamageCause) currentObjective.getValue()) {
                    case CONTACT:
                        return "Receive damage by contacting a block";
                    case ENTITY_ATTACK:
                        return "Get attacked by an entity";
                    case PROJECTILE:
                        return "Get hit by a projectile";
                    case SUFFOCATION:
                        return "Receive damage by being put in a block";
                    case FALL:
                        return "Receive fall damage";
                    case FIRE:
                    case FIRE_TICK:
                        return "Receive fire damage";
                    case LAVA:
                        return "Get damaged by lava";
                    case DROWNING:
                        return "Receive drowning damage";
                    case BLOCK_EXPLOSION:
                    case ENTITY_EXPLOSION:
                        return "Receive explosion damage";
                    case POISON:
                        return "Receive poison damage";
                    case MAGIC:
                        return "Receive potion damage";
                    case WITHER:
                        return "Receive damage by wither effect";
                    case FALLING_BLOCK:
                        return "Let an anvil fall on top of you";
                    case DRAGON_BREATH:
                        return "Receive damage by dragon breath";
                    case HOT_FLOOR:
                        return "Receive magma block damage";
                    case CRAMMING:
                        return "Receive damage by entity cramming";
                }
                break;
            case DoClutch:
                return "Do a %s clutch";
            case EnterDimension:
                return "Enter the %s";
            case TraverseBlocks:
                return "Cover a distance of %s blocks";
            case PlaceBlocks:
                return "Place %s blocks";
            case DieMultipleTimes:
//                return "Die %s time" + ((int) currentObjective.getValue() > 1 ? "s" : "");
                return "Kill yourself";
        }
        return criteria;
    }

}
