package dev.orangefahta.variablemod;

import dev.orangefahta.variablemod.command.CommandInterceptor;
import dev.orangefahta.variablemod.command.VariableCommand;
import dev.orangefahta.variablemod.lang.Lang;
import dev.orangefahta.variablemod.storage.VariableStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главный файл
 * @author @OrangeFaHTA
 * @version 1.1.0
 */
public class VariableMod implements ModInitializer {

    public static final String MOD_ID = "variablemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Lang.init();

        LOGGER.info(Lang.get("variablemod.log.init"));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            VariableCommand.register(dispatcher)
        );

        CommandInterceptor.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Lang.reload(server);
            LOGGER.info(Lang.get("variablemod.log.loading"));
            VariableStorage.init(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info(Lang.get("variablemod.log.stopping"));
            VariableStorage.clear();
        });

        LOGGER.info(Lang.get("variablemod.log.ready"));
    }
}
