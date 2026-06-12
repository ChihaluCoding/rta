package chihalu.rta.mixin;

import chihalu.rta.config.RtaConfigStore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class GuaranteedDropMixin {
	@Inject(method = "dropCustomDeathLoot", at = @At("TAIL"), remap = false)
	private void rta$addGuaranteedDrop(ServerLevel level, DamageSource damageSource, boolean recentlyHit, CallbackInfo callbackInfo) {
		if (!RtaConfigStore.get().guaranteedDrops) {
			return;
		}

		LivingEntity entity = (LivingEntity) (Object) this;
		if (entity instanceof Blaze) {
			entity.spawnAtLocation(level, new ItemStack(Items.BLAZE_ROD));
		} else if (entity instanceof EnderMan) {
			entity.spawnAtLocation(level, new ItemStack(Items.ENDER_PEARL));
		}
	}
}
