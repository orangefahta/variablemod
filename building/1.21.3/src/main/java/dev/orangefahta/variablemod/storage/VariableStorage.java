package dev.orangefahta.variablemod.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.orangefahta.variablemod.VariableMod;
import dev.orangefahta.variablemod.lang.Lang;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Хранение переменных.
 * @author @OrangeFaHTA
 * @version 1.1.0
 */
public class VariableStorage {

    public enum VarType {
        TEXT,
        NUMBER
    }

    public static class Variable {
        public String value;
        public VarType type;

        public Variable(String value, VarType type) {
            this.value = value;
            this.type = type;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Variable> variables = new HashMap<>();
    private static Path variableFolder;

    public static void init(MinecraftServer server) {
        variableFolder = server
                .getSavePath(WorldSavePath.ROOT)
                .resolve("variable")
                .resolve("commands")
                .resolve("v");

        try {
            Files.createDirectories(variableFolder);
        } catch (IOException e) {
            VariableMod.LOGGER.error(Lang.get("variablemod.log.failed_folder"), e);
        }

        loadAll();
    }

    public static boolean create(String name, String value) {
        if (variables.containsKey(name)) return false;
        Variable variable = new Variable(value, VarType.TEXT);
        variables.put(name, variable);
        saveVariable(name, variable);
        return true;
    }

    public static boolean edit(String name, String newValue) {
        Variable variable = variables.get(name);
        if (variable == null) return false;

        if (variable.type == VarType.NUMBER) {
            try {
                Double.parseDouble(newValue);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        variable.value = newValue;
        saveVariable(name, variable);
        return true;
    }

    public static boolean delete(String name) {
        Variable removed = variables.remove(name);
        if (removed == null) return false;

        try {
            Files.deleteIfExists(variableFolder.resolve(name + ".json"));
        } catch (IOException e) {
            VariableMod.LOGGER.error(Lang.get("variablemod.log.failed_delete"), name, e);
        }
        return true;
    }

    public static int setType(String name, VarType newType) {
        Variable variable = variables.get(name);
        if (variable == null) return -1;

        if (newType == VarType.NUMBER) {
            try {
                Double.parseDouble(variable.value);
            } catch (NumberFormatException e) {
                return -2;
            }
        }

        variable.type = newType;
        saveVariable(name, variable);
        return 0;
    }

    public static Variable get(String name) { return variables.get(name); }
    public static boolean exists(String name) { return variables.containsKey(name); }
    public static Set<String> names() { return variables.keySet(); }
    public static void clear() { variables.clear(); }

    private static void saveVariable(String name, Variable variable) {
        if (variableFolder == null) return;

        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("value", variable.value);
        json.addProperty("type", variable.type.name());

        Path file = variableFolder.resolve(name + ".json");
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            VariableMod.LOGGER.error(Lang.get("variablemod.log.failed_save"), name, e);
        }
    }

    private static void loadAll() {
        variables.clear();
        if (variableFolder == null) return;

        try {
            Files.list(variableFolder)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(VariableStorage::loadFile);
        } catch (IOException e) {
            VariableMod.LOGGER.error(Lang.get("variablemod.log.failed_load"), e);
        }
    }

    private static void loadFile(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            String name  = json.get("name").getAsString();
            String value = json.get("value").getAsString();
            VarType type = VarType.valueOf(json.get("type").getAsString());
            variables.put(name, new Variable(value, type));
            VariableMod.LOGGER.info(Lang.get("variablemod.log.loaded_var"), name, value);
        } catch (Exception e) {
            VariableMod.LOGGER.error(Lang.get("variablemod.log.failed_load_file"), file, e);
        }
    }

    public static String resolveUserVars(String input) {
        if (!input.contains("v:")) return input;

        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("v:([\\w\\-]+)").matcher(input);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Variable variable = variables.get(varName);
            matcher.appendReplacement(result,
                variable != null
                    ? java.util.regex.Matcher.quoteReplacement(variable.value)
                    : java.util.regex.Matcher.quoteReplacement(matcher.group(0)));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
