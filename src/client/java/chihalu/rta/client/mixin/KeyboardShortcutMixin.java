package chihalu.rta.client.mixin;

import chihalu.rta.client.RtaClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardShortcutMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "keyPress", at = @At("HEAD"))
	private void rta$handleSeedResetShortcut(long windowPointer, int action, KeyEvent keyEvent, CallbackInfo callbackInfo) {
		if (action == 1 && keyEvent.key() == InputConstants.KEY_Y && minecraft.screen == null && minecraft.level != null) {
			RtaClient.resetSeed(minecraft);
		}
	}
}
