package dev.orangefahta.variablemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.orangefahta.variablemod.VariableMod;
import dev.orangefahta.variablemod.lang.Lang;
import dev.orangefahta.variablemod.storage.BuiltinVariables;
import dev.orangefahta.variablemod.storage.VariableStorage;
import dev.orangefahta.variablemod.storage.VariableStorage.VarType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Команды /variable
 * @author @OrangeFaHTA
 * @version 1.1.0
 */
public class VariableCommand {

    private static final SuggestionProvider<ServerCommandSource> VAR_NAMES =
        (ctx, builder) -> {
            VariableStorage.names().forEach(builder::suggest);
            return builder.buildFuture();
        };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("variable")
                .requires(src -> src.hasPermissionLevel(2))

                .then(CommandManager.literal("new")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .then(CommandManager.argument("value", StringArgumentType.greedyString())
                            .executes(VariableCommand::cmdNew))))

                .then(CommandManager.literal("delete")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(VAR_NAMES)
                        .executes(VariableCommand::cmdDelete)))

                .then(CommandManager.literal("edit")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(VAR_NAMES)
                        .then(CommandManager.argument("value", StringArgumentType.greedyString())
                            .executes(VariableCommand::cmdEdit))))

                .then(CommandManager.literal("type")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(VAR_NAMES)
                        .then(CommandManager.literal("text")
                            .executes(ctx -> cmdType(ctx, VarType.TEXT)))
                        .then(CommandManager.literal("number")
                            .executes(ctx -> cmdType(ctx, VarType.NUMBER)))))

                .then(CommandManager.literal("cfg")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(VAR_NAMES)
                        .executes(VariableCommand::cmdCfg)))

                .then(CommandManager.literal("list")
                    .executes(VariableCommand::cmdList))

                .then(CommandManager.literal("builtins")
                    .executes(VariableCommand::cmdBuiltins))
        );

        VariableMod.LOGGER.info(Lang.get("variablemod.log.cmd_registered"));
    }

    private static void sendLine(ServerCommandSource src, MutableText text) {
        src.sendFeedback(() -> text, false);
    }

    private static MutableText t(String text, Formatting... fmts) {
        MutableText mt = Text.literal(text);
        for (Formatting f : fmts) mt = mt.formatted(f);
        return mt;
    }

    private static int cmdNew(CommandContext<ServerCommandSource> ctx) {
        String name  = StringArgumentType.getString(ctx, "name");
        String value = StringArgumentType.getString(ctx, "value");
        VariableMod.LOGGER.info("[VariableMod] CMD new '{}' = '{}'", name, value);

        if (VariableStorage.create(name, value)) {
            sendLine(ctx.getSource(), t(Lang.fmt("variablemod.cmd.created", name, value), Formatting.GREEN));
            return 1;
        } else {
            ctx.getSource().sendError(t(Lang.fmt("variablemod.cmd.already_exists", name, name), Formatting.RED));
            return 0;
        }
    }

    private static int cmdDelete(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        VariableMod.LOGGER.info("[VariableMod] CMD delete '{}'", name);

        if (VariableStorage.delete(name)) {
            sendLine(ctx.getSource(), t(Lang.fmt("variablemod.cmd.deleted", name), Formatting.GREEN));
            return 1;
        } else {
            ctx.getSource().sendError(t(Lang.fmt("variablemod.cmd.not_found", name), Formatting.RED));
            return 0;
        }
    }

    private static int cmdEdit(CommandContext<ServerCommandSource> ctx) {
        String name  = StringArgumentType.getString(ctx, "name");
        String value = StringArgumentType.getString(ctx, "value");
        VariableMod.LOGGER.info("[VariableMod] CMD edit '{}' = '{}'", name, value);

        if (!VariableStorage.exists(name)) {
            ctx.getSource().sendError(t(Lang.fmt("variablemod.cmd.not_found", name), Formatting.RED));
            return 0;
        }
        if (VariableStorage.edit(name, value)) {
            sendLine(ctx.getSource(), t(Lang.fmt("variablemod.cmd.edited", name, value), Formatting.GREEN));
            return 1;
        } else {
            ctx.getSource().sendError(t(Lang.fmt("variablemod.cmd.not_a_number", value, name), Formatting.RED));
            return 0;
        }
    }

    private static int cmdType(CommandContext<ServerCommandSource> ctx, VarType newType) {
        String name = StringArgumentType.getString(ctx, "name");
        VariableMod.LOGGER.info("[VariableMod] CMD type '{}' -> {}", name, newType);

        int result = VariableStorage.setType(name, newType);
        switch (result) {
            case 0 -> sendLine(ctx.getSource(),
                t(Lang.fmt("variablemod.cmd.type_changed", name, newType.name().toLowerCase()), Formatting.GREEN));
            case -1 -> ctx.getSource().sendError(
                t(Lang.fmt("variablemod.cmd.not_found", name), Formatting.RED));
            case -2 -> ctx.getSource().sendError(
                t(Lang.fmt("variablemod.cmd.value_not_number", VariableStorage.get(name).value), Formatting.RED));
        }
        return result == 0 ? 1 : 0;
    }

    private static int cmdCfg(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        VariableStorage.Variable v = VariableStorage.get(name);

        if (v == null) {
            ctx.getSource().sendError(t(Lang.fmt("variablemod.cmd.not_found", name), Formatting.RED));
            return 0;
        }

        sendLine(ctx.getSource(), t(Lang.fmt("variablemod.cmd.cfg_header", name), Formatting.GOLD));
        sendLine(ctx.getSource(), t(Lang.fmt("variablemod.cmd.cfg_value", v.value), Formatting.GRAY));
        sendLine(ctx.getSource(), t(Lang.fmt("variablemod.cmd.cfg_type", v.type.name().toLowerCase()), Formatting.GRAY));
        return 1;
    }

    private static int cmdList(CommandContext<ServerCommandSource> ctx) {
        if (VariableStorage.names().isEmpty()) {
            sendLine(ctx.getSource(), t(Lang.get("variablemod.cmd.list_empty"), Formatting.GRAY));
            return 1;
        }
        sendLine(ctx.getSource(), t(Lang.get("variablemod.cmd.list_header"), Formatting.GOLD));
        for (String name : VariableStorage.names()) {
            VariableStorage.Variable v = VariableStorage.get(name);
            sendLine(ctx.getSource(),
                t(Lang.fmt("variablemod.cmd.list_entry", name, v.value, v.type.name().toLowerCase()), Formatting.WHITE));
        }
        return 1;
    }

    private static int cmdBuiltins(CommandContext<ServerCommandSource> ctx) {
        sendLine(ctx.getSource(), t(Lang.get("variablemod.cmd.builtins_header"), Formatting.GOLD));
        BuiltinVariables.descriptions().forEach((name, desc) ->
            sendLine(ctx.getSource(),
                t("  v!:" + name, Formatting.YELLOW)
                    .append(t(" — " + desc, Formatting.GRAY))));
        return 1;
    }
}
