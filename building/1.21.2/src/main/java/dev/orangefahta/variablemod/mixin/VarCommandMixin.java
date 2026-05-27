package dev.orangefahta.variablemod.mixin;

import dev.orangefahta.variablemod.VariableMod;
import dev.orangefahta.variablemod.command.CommandInterceptor;
import dev.orangefahta.variablemod.lang.Lang;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Индекс команд.
 * @author @OrangeFaHTA
 * @version 1.1.0
 */
@Mixin(value = CommandManager.class, priority = 900)
public abstract class VarCommandMixin {

    private static final ThreadLocal<ServerCommandSource> SOURCE_HOLDER = new ThreadLocal<>();

    @ModifyVariable(
        method = "executeWithPrefix",
        at = @At("HEAD"),
        argsOnly = true,
        index = 1
    )
    private ServerCommandSource varmod_captureSource(ServerCommandSource source) {
        VariableMod.LOGGER.info(Lang.get("variablemod.log.exec_prefix_src"),
            source != null ? source.getName() : "null");
        SOURCE_HOLDER.set(source);
        return source;
    }

    @ModifyVariable(
        method = "executeWithPrefix",
        at = @At("HEAD"),
        argsOnly = true,
        index = 2
    )
    private String varmod_resolveTokens(String command) {
        VariableMod.LOGGER.info(Lang.get("variablemod.log.exec_prefix_cmd"), command);

        if (!command.contains("v:") && !command.contains("v!:")) return command;

        ServerCommandSource source = SOURCE_HOLDER.get();
        String resolved = CommandInterceptor.resolveCommand(command, source);

        if (!resolved.equals(command)) {
            VariableMod.LOGGER.info(Lang.get("variablemod.log.exec_prefix_res"), command, resolved);
        }
        return resolved;
    }
}
