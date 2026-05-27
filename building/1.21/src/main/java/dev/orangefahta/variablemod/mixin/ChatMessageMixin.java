package dev.orangefahta.variablemod.mixin;

import dev.orangefahta.variablemod.VariableMod;
import dev.orangefahta.variablemod.command.CommandInterceptor;
import dev.orangefahta.variablemod.lang.Lang;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Перехват чата и чатсей-команд
 * @author @OrangeFaHTA
 * @version 1.1.0
 */
@Mixin(value = ServerPlayNetworkHandler.class, priority = 900)
public abstract class ChatMessageMixin {

    @Shadow
    public ServerPlayerEntity player;

    @ModifyVariable(
        method = "executeCommand",
        at = @At("HEAD"),
        argsOnly = true,
        index = 1
    )
    private String varmod_resolveCommand(String command) {
        if (command == null || (!command.contains("v:") && !command.contains("v!:"))) return command;
        try {
            if (player != null) {
                String resolved = CommandInterceptor.resolveCommand(command, player.getCommandSource());
                if (!resolved.equals(command)) {
                    VariableMod.LOGGER.info(Lang.get("variablemod.log.exec_cmd_resolve"), command, resolved);
                }
                return resolved;
            }
        } catch (Exception e) {
            VariableMod.LOGGER.error(Lang.get("variablemod.log.exec_cmd_error"), e.toString());
        }
        return command;
    }

    @Inject(
        method = "onChatMessage",
        at = @At("HEAD"),
        cancellable = true
    )
    private void varmod_resolveChatPacket(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (packet == null) return;
        String message = packet.chatMessage();
        if (message == null || (!message.contains("v:") && !message.contains("v!:"))) return;

        try {
            if (player != null) {
                String resolved = CommandInterceptor.resolveCommand(message, player.getCommandSource());
                if (!resolved.equals(message)) {
                    VariableMod.LOGGER.info(Lang.get("variablemod.log.chat_intercept"), message, resolved);
                    ci.cancel();
                    ChatMessageC2SPacket newPacket = new ChatMessageC2SPacket(
                        resolved,
                        packet.timestamp(),
                        packet.salt(),
                        packet.signature(),
                        packet.acknowledgment()
                    );
                    ((ServerPlayNetworkHandler)(Object)this).onChatMessage(newPacket);
                }
            }
        } catch (Exception e) {
            VariableMod.LOGGER.error(Lang.get("variablemod.log.chat_error"), e.toString());
        }
    }

    @ModifyVariable(
        method = "onChatCommandSigned",
        at = @At("HEAD"),
        argsOnly = true
    )
    private ChatCommandSignedC2SPacket varmod_resolveChatCommand(ChatCommandSignedC2SPacket packet) {
        if (packet == null) return packet;
        String command = packet.command();
        if (command == null || (!command.contains("v:") && !command.contains("v!:"))) return packet;

        try {
            if (player != null) {
                String resolved = CommandInterceptor.resolveCommand(command, player.getCommandSource());
                if (!resolved.equals(command)) {
                    VariableMod.LOGGER.info(Lang.get("variablemod.log.say_intercept"), command, resolved);
                    return new ChatCommandSignedC2SPacket(
                        resolved,
                        packet.timestamp(),
                        packet.salt(),
                        packet.argumentSignatures(),
                        packet.lastSeenMessages()
                    );
                }
            }
        } catch (Exception e) {
            VariableMod.LOGGER.error(Lang.get("variablemod.log.say_error"), e.toString());
        }
        return packet;
    }
}
