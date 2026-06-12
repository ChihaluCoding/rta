package chihalu.rta;

import chihalu.rta.config.RtaConfigStore;
import chihalu.rta.core.RtaHistory;
import chihalu.rta.core.RtaMiningFeatures;
import chihalu.rta.core.RtaTimer;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Rta implements ModInitializer {
	public static final String MOD_ID = "rta";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		RtaConfigStore.load();
		RtaMiningFeatures.register();
		ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> RtaTimer.startIfNeeded());
		ServerTickEvents.END_SERVER_TICK.register(RtaTimer::tick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("rtahistory").executes(context -> {
				List<String> history = RtaHistory.read(context.getSource().getServer());
				if (history.isEmpty()) {
					context.getSource().sendSuccess(() -> Component.literal("RTA履歴はまだありません。"), false);
					return Command.SINGLE_SUCCESS;
				}

				context.getSource().sendSuccess(() -> Component.literal("RTA履歴"), false);
				for (String line : history) {
					context.getSource().sendSuccess(() -> Component.literal(line), false);
				}
				return Command.SINGLE_SUCCESS;
			}));
		});

		LOGGER.info("RTA mod initialized.");
	}
}
