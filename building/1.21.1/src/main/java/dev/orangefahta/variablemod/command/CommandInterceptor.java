package dev.orangefahta.variablemod.command;

import dev.orangefahta.variablemod.VariableMod;
import dev.orangefahta.variablemod.lang.Lang;
import dev.orangefahta.variablemod.storage.BuiltinVariables;
import dev.orangefahta.variablemod.storage.VariableStorage;
import net.minecraft.server.command.ServerCommandSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Создание v: и v!:
 * @author @OrangeFaHTA
 * @version 1.1.0
 */
public class CommandInterceptor {

    private static final Pattern BUILTIN_PATTERN = Pattern.compile("v!:([\\w\\-]+)");

    public static void register() {}

    public static String resolveCommand(String raw, ServerCommandSource source) {
        if (!raw.contains("v:") && !raw.contains("v!:")) return raw;
        VariableMod.LOGGER.info(Lang.get("variablemod.log.resolve_in"), raw);

        String result = resolveBuiltins(raw, source);
        result = VariableStorage.resolveUserVars(result);

        VariableMod.LOGGER.info(Lang.get("variablemod.log.resolve_out"), result);
        return result;
    }

    public static String resolveCommandNoSource(String raw) {
        if (!raw.contains("v:")) return raw;
        return VariableStorage.resolveUserVars(raw);
    }

    private static String resolveBuiltins(String input, ServerCommandSource source) {
        if (!input.contains("v!:")) return input;

        Matcher m = BUILTIN_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String name = m.group(1);
            String val = BuiltinVariables.resolve(name, source);
            VariableMod.LOGGER.info(Lang.get("variablemod.log.builtin_resolve"), name, val);
            m.appendReplacement(sb, val != null
                ? Matcher.quoteReplacement(val)
                : Matcher.quoteReplacement(m.group(0)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
