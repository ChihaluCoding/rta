package chihalu.rta.client;

import chihalu.rta.config.RtaConfigStore;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class RtaDeathScreenNotice {
	private RtaDeathScreenNotice() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof DeathScreen && client.player != null && RtaConfigStore.get().spectatorAssist) {
				Component command = Component.literal("/rta replay").withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.YELLOW));
				client.player.sendSystemMessage(Component.literal("リプレイを見るには ")
					.withStyle(ChatFormatting.YELLOW)
					.append(command)
					.append(Component.literal(" コマンドを実行してください。").withStyle(ChatFormatting.YELLOW)));
			}
		});
	}
}
