package chihalu.rta.mixin;

import chihalu.rta.core.RtaServerActions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragon.class)
public class EnderDragonDeathMixin {
	@Unique
	private boolean rta$completionHandled;

	@Inject(method = "tickDeath", at = @At("HEAD"), remap = false)
	private void rta$completeRunOnDragonDeath(CallbackInfo callbackInfo) {
		if (rta$completionHandled) {
			return;
		}

		rta$completionHandled = true;
		EnderDragon dragon = (EnderDragon) (Object) this;
		if (dragon.level() instanceof ServerLevel serverLevel) {
			RtaServerActions.completeRun(serverLevel.getServer());
		}
	}
}
