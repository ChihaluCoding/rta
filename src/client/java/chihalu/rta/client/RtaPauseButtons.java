package chihalu.rta.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

public final class RtaPauseButtons {
	private RtaPauseButtons() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof PauseScreen)) {
				return;
			}

			int y = scaledHeight / 4 + 148;
			int buttonWidth = 200;
			int startX = scaledWidth / 2 - buttonWidth / 2;

			Screens.getWidgets(screen).add(Button.builder(Component.literal("シード値リセット"), button -> RtaClient.resetSeed(client))
				.bounds(startX, y, buttonWidth, 20)
				.build());
		});
	}
}
