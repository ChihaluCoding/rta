package chihalu.rta.core;

import chihalu.rta.config.RtaConfigStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class RtaServerActions {
	private RtaServerActions() {
	}

	public static void completeRun(MinecraftServer server) {
		if (!RtaTimer.isRunning()) {
			return;
		}

		RtaTimer.stop();
		String timeText = RtaTimer.formatMillis(RtaTimer.realElapsedMillis());
		RtaHistory.append(server, timeText);

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 100, 20));
			player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(timeText)));
			player.connection.send(new ClientboundSoundPacket(
				Holder.direct(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE),
				SoundSource.MASTER,
				player.getX(),
				player.getY(),
				player.getZ(),
				1.0F,
				1.0F,
				player.getRandom().nextLong()
			));
		}
	}

	public static void stopForDeath(ServerPlayer player) {
		RtaTimer.stop();
		if (RtaConfigStore.get().spectatorAssist) {
			BlockPos deathPos = player.blockPosition();
			player.level().getServer().getPlayerList().setAllowCommandsForAllPlayers(true);
			player.level().getChunk(deathPos);
		}
		player.sendSystemMessage(Component.literal("死亡したためRTAタイマーを停止しました。"));
	}
}
