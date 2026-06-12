package chihalu.rta.client;

import chihalu.rta.config.RtaConfigStore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class RtaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		RtaConfigStore.load();
		RtaPauseButtons.register();
		RtaDeathScreenNotice.register();
		RtaClientReplayManager.registerCommands();

		ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> RtaClientTimer.start());
		ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> RtaClientTimer.stop());
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			RtaClientTimer.tick(client.isPaused());
			RtaClientReplayManager.tick(client);
			if (client.player != null) {
				if (RtaConfigStore.get().fullBright) {
					client.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, false, false, false));
				} else if (client.player.hasEffect(MobEffects.NIGHT_VISION)) {
					client.player.removeEffect(MobEffects.NIGHT_VISION);
				}
			}
		});
	}

	public static void resetSeed(net.minecraft.client.Minecraft client) {
		if (client.player != null) {
			client.player.sendSystemMessage(Component.literal("シード値リセット: タイトルへ戻ります。新しいランを作成してください。"));
		}
		client.disconnect(new TitleScreen(), false, false);
	}
}
