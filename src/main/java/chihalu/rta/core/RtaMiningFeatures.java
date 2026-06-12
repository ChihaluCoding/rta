package chihalu.rta.core;

import chihalu.rta.config.RtaConfigStore;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class RtaMiningFeatures {
	private static final int MAX_BATCH_BLOCKS = 64;

	private RtaMiningFeatures() {
	}

	public static void register() {
		PlayerBlockBreakEvents.BEFORE.register(RtaMiningFeatures::beforeBreak);
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, entity) -> {
			if (RtaConfigStore.get().batchBreak && level instanceof ServerLevel serverLevel) {
				breakBatch(serverLevel, player, pos, state);
			}
		});
	}

	private static boolean beforeBreak(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
		if (!RtaConfigStore.get().autoSmeltOres || !(level instanceof ServerLevel serverLevel)) {
			return true;
		}

		Item smelted = smeltedDrop(state);
		if (smelted == null) {
			return true;
		}

		serverLevel.destroyBlock(pos, false, player, 512);
		Block.popResource(serverLevel, pos, new ItemStack(smelted));
		return false;
	}

	private static void breakBatch(ServerLevel level, Player player, BlockPos origin, BlockState originalState) {
		if (!isBatchTarget(originalState)) {
			return;
		}

		Set<BlockPos> visited = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		queue.add(origin);
		visited.add(origin);

		while (!queue.isEmpty() && visited.size() <= MAX_BATCH_BLOCKS) {
			BlockPos current = queue.removeFirst();
			for (BlockPos next : BlockPos.betweenClosed(current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
				BlockPos immutable = next.immutable();
				if (!visited.add(immutable)) {
					continue;
				}

				BlockState nextState = level.getBlockState(immutable);
				if (sameBatchFamily(originalState, nextState)) {
					level.destroyBlock(immutable, true, player, 512);
					queue.add(immutable);
				}
			}
		}
	}

	private static boolean sameBatchFamily(BlockState base, BlockState candidate) {
		return (isLog(base) && isLog(candidate)) || (isOre(base) && isOre(candidate)) || (isGrassLike(base) && isGrassLike(candidate));
	}

	private static boolean isBatchTarget(BlockState state) {
		return isLog(state) || isOre(state) || isGrassLike(state);
	}

	private static boolean isLog(BlockState state) {
		return state.is(BlockTags.LOGS, ignored -> true);
	}

	private static boolean isOre(BlockState state) {
		return state.is(BlockTags.COAL_ORES, ignored -> true)
			|| state.is(BlockTags.IRON_ORES, ignored -> true)
			|| state.is(BlockTags.COPPER_ORES, ignored -> true)
			|| state.is(BlockTags.GOLD_ORES, ignored -> true)
			|| state.is(BlockTags.REDSTONE_ORES, ignored -> true)
			|| state.is(BlockTags.LAPIS_ORES, ignored -> true)
			|| state.is(BlockTags.DIAMOND_ORES, ignored -> true)
			|| state.is(BlockTags.EMERALD_ORES, ignored -> true);
	}

	private static boolean isGrassLike(BlockState state) {
		return state.getBlock() == Blocks.SHORT_GRASS
			|| state.getBlock() == Blocks.TALL_GRASS
			|| state.getBlock() == Blocks.FERN
			|| state.getBlock() == Blocks.LARGE_FERN;
	}

	private static Item smeltedDrop(BlockState state) {
		if (state.is(BlockTags.IRON_ORES, ignored -> true)) {
			return Items.IRON_INGOT;
		}
		if (state.is(BlockTags.COPPER_ORES, ignored -> true)) {
			return Items.COPPER_INGOT;
		}
		if (state.is(BlockTags.GOLD_ORES, ignored -> true)) {
			return Items.GOLD_INGOT;
		}
		if (state.is(BlockTags.COAL_ORES, ignored -> true)) {
			return Items.COAL;
		}
		if (state.is(BlockTags.REDSTONE_ORES, ignored -> true)) {
			return Items.REDSTONE;
		}
		if (state.is(BlockTags.LAPIS_ORES, ignored -> true)) {
			return Items.LAPIS_LAZULI;
		}
		if (state.is(BlockTags.DIAMOND_ORES, ignored -> true)) {
			return Items.DIAMOND;
		}
		if (state.is(BlockTags.EMERALD_ORES, ignored -> true)) {
			return Items.EMERALD;
		}
		return null;
	}
}
