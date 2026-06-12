package chihalu.rta.mixin;

import chihalu.rta.config.RtaConfigStore;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderMan.class)
public class EnderManCarryMixin {
	@Inject(method = "setCarriedBlock", at = @At("HEAD"), cancellable = true, remap = false)
	private void rta$preventCarriedBlock(BlockState blockState, CallbackInfo callbackInfo) {
		if (RtaConfigStore.get().preventEndermanCarry && blockState != null) {
			callbackInfo.cancel();
		}
	}
}
