package dev.orangefahta.variablemod.lang;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.orangefahta.variablemod.VariableMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Локализация мода. 
 * @author @OrangeFaHTA
 * @version 1.1.1
 */
public class Lang {

    private static final Gson GSON = new Gson();
    private static final String DEFAULT_LANG = "en_us";
    private static final String[] SUPPORTED = {"en_us", "ru_ru"};

    private static Map<String, String> strings = new HashMap<>();
    private static Map<String, String> fallback = new HashMap<>();
    private static String currentLang = DEFAULT_LANG;

    public static void init() {
        loadLang(detectLang(null));
    }

    public static void reload(MinecraftServer server) {
        loadLang(detectLang(server));
    }

    public static String getCurrentLang() {
        return currentLang;
    }

    public static String get(String key) {
        return strings.getOrDefault(key, fallback.getOrDefault(key, key));
    }

    public static String fmt(String key, Object... args) {
        try {
            return String.format(get(key), args);
        } catch (Exception e) {
            return get(key);
        }
    }

    private static String detectLang(MinecraftServer server) {
        try {
            Path gameDir = server != null ? server.getRunDirectory() : FabricLoader.getInstance().getGameDir();
            Path optionsFile = gameDir.resolve("options.txt");

            if (Files.exists(optionsFile)) {
                for (String line : Files.readAllLines(optionsFile, StandardCharsets.UTF_8)) {
                    if (line.startsWith("lang:")) {
                        String lang = line.substring(5).trim().toLowerCase();
                        String matched = matchSupported(lang);
                        if (matched != null) {
                            return matched;
                        }
                        String langOnly = lang.contains("_") ? lang.split("_")[0] : lang;
                        matched = matchSupported(langOnly);
                        if (matched != null) return matched;
                        
                        return DEFAULT_LANG;
                    }
                }
            }
        } catch (Exception e) {
            VariableMod.LOGGER.warn("[VariableMod] Could not read options.txt: {}", e.toString());
        }

        java.util.Locale locale = java.util.Locale.getDefault();
        String candidate = locale.getLanguage().toLowerCase();
        String country = locale.getCountry().toLowerCase();
        String full = country.isEmpty() ? candidate : candidate + "_" + country;

        String matched = matchSupported(full);
        if (matched != null) return matched;

        matched = matchSupported(candidate);
        if (matched != null) return matched;

        return DEFAULT_LANG;
    }

    private static String matchSupported(String candidate) {
        for (String lang : SUPPORTED) {
            if (lang.equalsIgnoreCase(candidate)) return lang;
        }
        for (String lang : SUPPORTED) {
            if (lang.startsWith(candidate.split("_")[0])) return lang;
        }
        return null;
    }

    private static void loadLang(String lang) {
        currentLang = lang;
        fallback = loadFile(DEFAULT_LANG);

        if (lang.equals(DEFAULT_LANG)) {
            strings = fallback;
        } else {
            strings = loadFile(lang);
        }

        VariableMod.LOGGER.info("[VariableMod] Language loaded: {}", lang);
    }

    private static Map<String, String> loadFile(String lang) {
        String path = "/assets/variablemod/lang/" + lang + ".json";
        try (InputStream is = Lang.class.getResourceAsStream(path)) {
            if (is == null) {
                VariableMod.LOGGER.warn("[VariableMod] Lang file not found: {}", path);
                return new HashMap<>();
            }
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            return GSON.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), type);
        } catch (Exception e) {
            VariableMod.LOGGER.error("[VariableMod] Failed loading lang file {}: {}", path, e.toString());
            return new HashMap<>();
        }
    }
}