package chihalu.rta.client;

import chihalu.rta.config.RtaConfig;
import chihalu.rta.config.RtaConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class RtaHudRenderer {
	private static final int COLOR = 0xFFFFFFFF;
	private static final int MARGIN = 6;

	private RtaHudRenderer() {
	}

	public static void render(GuiGraphicsExtractor graphics) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) {
			return;
		}

		RtaConfig config = RtaConfigStore.get();
		List<String> lines = new ArrayList<>();
		lines.add("RTA " + RtaClientTimer.formatMillis(RtaClientTimer.elapsedMillis()));
		lines.add("IGT " + RtaClientTimer.formatMillis(RtaClientTimer.igtTicks() * 50L));
		lines.add("Biome " + biomeName(client));

		if (config.coordinateHud) {
			lines.add(coordinateText(client));
		}

		if (config.showRemainingCrystals && client.level.dimension() == Level.END) {
			lines.add("Crystal " + countCrystals(client));
		}

		Font font = client.font;
		int lineHeight = font.lineHeight + 2;
		int maxWidth = lines.stream().mapToInt(font::width).max().orElse(0);
		int x = switch (config.hudPosition) {
			case TOP_RIGHT, BOTTOM_RIGHT -> graphics.guiWidth() - maxWidth - MARGIN;
			default -> MARGIN;
		};
		int y = switch (config.hudPosition) {
			case BOTTOM_LEFT, BOTTOM_RIGHT -> graphics.guiHeight() - (lineHeight * lines.size()) - MARGIN;
			default -> MARGIN;
		};

		for (int index = 0; index < lines.size(); index++) {
			graphics.text(font, lines.get(index), x, y + index * lineHeight, COLOR);
		}
	}

	private static String biomeName(Minecraft client) {
		return client.level.getBiome(client.player.blockPosition())
			.unwrapKey()
			.map(ResourceKey::identifier)
			.map(Object::toString)
			.orElse("unknown");
	}

	private static String coordinateText(Minecraft client) {
		int x = client.player.blockPosition().getX();
		int z = client.player.blockPosition().getZ();
		if (client.level.dimension() == Level.NETHER) {
			return "OW " + (x * 8) + ", " + (z * 8);
		}
		if (client.level.dimension() == Level.OVERWORLD) {
			return "Nether " + (x / 8) + ", " + (z / 8);
		}
		return "XYZ " + x + ", " + client.player.blockPosition().getY() + ", " + z;
	}

	private static long countCrystals(Minecraft client) {
		long count = 0L;
		for (Entity entity : client.level.entitiesForRendering()) {
			if (entity instanceof EndCrystal) {
				count++;
			}
		}
		return count;
	}
}
