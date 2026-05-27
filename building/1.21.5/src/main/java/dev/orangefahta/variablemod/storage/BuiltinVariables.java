package dev.orangefahta.variablemod.storage;

import dev.orangefahta.variablemod.lang.Lang;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Встроенные переменные (v!:).
 *
 * @author @OrangeFaHTA
 * @version 1.1.0
 * Full list:
 *   v!:nickname   — display name of the player
 *   v!:name       — raw username (no formatting)
 *   v!:gamemode   — current gamemode (survival/creative/adventure/spectator)
 *   v!:world      — dimension key (e.g. minecraft:overworld)
 *   v!:x          — block X coordinate
 *   v!:y          — block Y coordinate
 *   v!:z          — block Z coordinate
 *   v!:health     — current health (integer)
 *   v!:food       — food level (integer)
 *   v!:xp         — total experience points
 *   v!:level      — experience level
 *   v!:time       — current in-game time (HH:mm)
 *   v!:date       — real-world date (yyyy-MM-dd)
 *   v!:day        — current in-game day number
 *   v!:onlinecnt  — number of players currently online
 */
public class BuiltinVariables {
    private static final String[] NAMES = {
        "nickname", "name", "gamemode", "world",
        "x", "y", "z",
        "health", "food", "xp", "level",
        "time", "date", "day", "onlinecnt"
    };

    public static Set<String> names() {
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (String n : NAMES) set.add(n);
        return set;
    }

    public static Map<String, String> descriptions() {
        Map<String, String> map = new LinkedHashMap<>();
        for (String name : NAMES) {
            map.put(name, Lang.get("variablemod.builtin." + name));
        }
        return map;
    }

    public static String resolve(String name, ServerCommandSource source) {
        ServerPlayerEntity player = null;
        try {
            player = source != null ? source.getPlayer() : null;
        } catch (Exception ignored) {}

        return switch (name) {
            case "nickname" -> player != null
                ? player.getDisplayName().getString()
                : (source != null ? source.getName() : "?");

            case "name" -> player != null
                ? player.getName().getString()
                : (source != null ? source.getName() : "?");

            case "gamemode" -> player != null
                ? gamemodeString(player.interactionManager.getGameMode())
                : "unknown";

            case "world" -> source != null
                ? source.getWorld().getRegistryKey().getValue().toString()
                : "unknown";

            case "x" -> player != null
                ? String.valueOf(player.getBlockX())
                : (source != null ? String.valueOf((int) source.getPosition().x) : "0");

            case "y" -> player != null
                ? String.valueOf(player.getBlockY())
                : (source != null ? String.valueOf((int) source.getPosition().y) : "0");

            case "z" -> player != null
                ? String.valueOf(player.getBlockZ())
                : (source != null ? String.valueOf((int) source.getPosition().z) : "0");

            case "health" -> player != null
                ? String.valueOf((int) player.getHealth())
                : "?";

            case "food" -> player != null
                ? String.valueOf(player.getHungerManager().getFoodLevel())
                : "?";

            case "xp" -> player != null
                ? String.valueOf(player.totalExperience)
                : "?";

            case "level" -> player != null
                ? String.valueOf(player.experienceLevel)
                : "?";

            case "time" -> {
                if (source != null) {
                    long ticks = source.getWorld().getTimeOfDay() % 24000;
                    int hours   = (int) ((ticks / 1000 + 6) % 24);
                    int minutes = (int) ((ticks % 1000) * 60 / 1000);
                    yield String.format("%02d:%02d", hours, minutes);
                }
                yield "00:00";
            }

            case "date" -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            case "day" -> source != null
                ? String.valueOf(source.getWorld().getTime() / 24000)
                : "0";

            case "onlinecnt" -> source != null
                ? String.valueOf(source.getServer().getCurrentPlayerCount())
                : "0";

            default -> null;
        };
    }

    private static String gamemodeString(GameMode mode) {
        return switch (mode) {
            case SURVIVAL  -> "survival";
            case CREATIVE  -> "creative";
            case ADVENTURE -> "adventure";
            case SPECTATOR -> "spectator";
        };
    }
}
