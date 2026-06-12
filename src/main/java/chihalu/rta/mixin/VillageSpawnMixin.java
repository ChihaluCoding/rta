package chihalu.rta.mixin;

import chihalu.rta.config.RtaConfigStore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.server.MinecraftServer.class)
public class VillageSpawnMixin {
	@Inject(method = "setInitialSpawn", at = @At("TAIL"), remap = false)
	private static void rta$moveInitialSpawnToVillage(ServerLevel level, ServerLevelData levelData, boolean generateBonusChest, boolean debugWorld, LevelLoadListener listener, CallbackInfo callbackInfo) {
		if (!RtaConfigStore.get().villageSpawn || level.dimension() != Level.OVERWORLD) {
			return;
		}

		BlockPos base = LevelData.RespawnData.DEFAULT.pos();
		BlockPos village = level.findNearestMapStructure(villageTag(), base, 64, false);
		if (village != null) {
			levelData.setSpawn(LevelData.RespawnData.of(Level.OVERWORLD, village.offset(0, 2, 0), 0.0F, 0.0F));
		}
	}

	private static TagKey<Structure> villageTag() {
		return switch (RtaConfigStore.get().villageBiome) {
			case "minecraft:desert" -> StructureTags.ON_DESERT_VILLAGE_MAPS;
			case "minecraft:savanna" -> StructureTags.ON_SAVANNA_VILLAGE_MAPS;
			case "minecraft:snowy_plains" -> StructureTags.ON_SNOWY_VILLAGE_MAPS;
			case "minecraft:taiga" -> StructureTags.ON_TAIGA_VILLAGE_MAPS;
			default -> StructureTags.ON_PLAINS_VILLAGE_MAPS;
		};
	}
}
