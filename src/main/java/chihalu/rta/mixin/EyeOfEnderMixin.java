package chihalu.rta.mixin;

import chihalu.rta.config.RtaConfigStore;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EyeOfEnder.class)
public class EyeOfEnderMixin {
	@Shadow
	private boolean surviveAfterDeath;

	@Inject(method = "signalTo", at = @At("TAIL"), remap = false)
	private void rta$preventEyeBreak(Vec3 target, CallbackInfo callbackInfo) {
		if (RtaConfigStore.get().preventEyeBreak) {
			this.surviveAfterDeath = true;
		}
	}
}
