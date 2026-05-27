package dev.orangefahta.variablemod.mixin;

import com.mojang.brigadier.ParseResults;
import dev.orangefahta.variablemod.VariableMod;
import dev.orangefahta.variablemod.command.CommandInterceptor;
import dev.orangefahta.variablemod.lang.Lang;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Перехват команд.
 * @author @OrangeFaHTA
 * @version 1.1.0
 */
@Mixin(CommandManager.class)
public class CommandManagerMixin {

    @ModifyVariable(
        method = "execute",
        at = @At("HEAD"),
        argsOnly = true,
        index = 2
    )
    private String varmod_universalResolve(String command, ParseResults<ServerCommandSource> parseResults) {
        if (command == null || (!command.contains("v:") && !command.contains("v!:"))) return command;

        try {
            ServerCommandSource source = parseResults.getContext().getSource();
            if (source != null) {
                String resolved = CommandInterceptor.resolveCommand(command, source);
                if (!resolved.equals(command)) {
                    VariableMod.LOGGER.info(Lang.get("variablemod.log.intercept_global"), command, resolved);
                }
                return resolved;
            }
        } catch (Exception e) {
            VariableMod.LOGGER.error(Lang.get("variablemod.log.intercept_error"), e.toString());
        }

        return command;
    }
}
