package chihalu.rta.mixin;

import chihalu.rta.core.RtaServerActions;
import chihalu.rta.core.RtaTimer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerDeathMixin {
	@Inject(method = "die", at = @At("HEAD"), remap = false)
	private void rta$stopTimerOnDeath(DamageSource damageSource, CallbackInfo callbackInfo) {
		if (RtaTimer.isRunning()) {
			RtaServerActions.stopForDeath((ServerPlayer) (Object) this);
		}
	}
}
