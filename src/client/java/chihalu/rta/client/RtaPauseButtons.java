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
			int buttonWidth = 96;
			int gap = 4;
			int startX = scaledWidth / 2 - buttonWidth - gap - buttonWidth / 2;

			Screens.getWidgets(screen).add(Button.builder(Component.literal("録画開始"), button -> {
				if (client.player != null) {
					RtaClientReplayManager.start(client);
				}
			}).bounds(startX, y, buttonWidth, 20).build());

			Screens.getWidgets(screen).add(Button.builder(Component.literal("録画停止"), button -> {
				if (client.player != null) {
					RtaClientReplayManager.stop(client);
				}
			}).bounds(startX + buttonWidth + gap, y, buttonWidth, 20).build());

			Screens.getWidgets(screen).add(Button.builder(Component.literal("シード値リセット"), button -> RtaClient.resetSeed(client))
				.bounds(startX + (buttonWidth + gap) * 2, y, buttonWidth, 20)
				.build());
		});
	}
}
