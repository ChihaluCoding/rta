package chihalu.rta.client;

import chihalu.rta.config.RtaConfig;
import chihalu.rta.config.RtaConfigStore;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RtaOptionsScreen extends Screen {
	private static final String[] VILLAGE_BIOMES = {
		"minecraft:plains",
		"minecraft:desert",
		"minecraft:savanna",
		"minecraft:snowy_plains",
		"minecraft:taiga"
	};
	private final Screen parent;

	public RtaOptionsScreen(Screen parent) {
		super(Component.literal("RTA Mod 設定"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		RtaConfig config = RtaConfigStore.get();
		int left = this.width / 2 - 205;
		int right = this.width / 2 + 5;
		int y = 40;
		int step = 24;

		addToggle(left, y, "座標換算HUD", () -> config.coordinateHud, value -> config.coordinateHud = value);
		addToggle(right, y, "フルブライト", () -> config.fullBright, value -> config.fullBright = value);
		y += step;
		addToggle(left, y, "一括破壊", () -> config.batchBreak, value -> config.batchBreak = value);
		addToggle(right, y, "鉱石自動精錬", () -> config.autoSmeltOres, value -> config.autoSmeltOres = value);
		y += step;
		addToggle(left, y, "確定ドロップ", () -> config.guaranteedDrops, value -> config.guaranteedDrops = value);
		addToggle(right, y, "エンダーアイ破壊無効", () -> config.preventEyeBreak, value -> config.preventEyeBreak = value);
		y += step;
		addToggle(left, y, "エンダーマンブロック禁止", () -> config.preventEndermanCarry, value -> config.preventEndermanCarry = value);
		addToggle(right, y, "残クリスタル数表示", () -> config.showRemainingCrystals, value -> config.showRemainingCrystals = value);
		y += step;
		addToggle(left, y, "死亡後観覧補助", () -> config.spectatorAssist, value -> config.spectatorAssist = value);
		addToggle(right, y, "村スポーン", () -> config.villageSpawn, value -> config.villageSpawn = value);
		y += step;
		this.addRenderableWidget(Button.builder(Component.literal("HUD位置: " + label(config.hudPosition)), button -> {
			config.hudPosition = nextHudPosition(config.hudPosition);
			button.setMessage(Component.literal("HUD位置: " + label(config.hudPosition)));
			RtaConfigStore.save();
		}).bounds(left, y, 200, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("村バイオーム: " + config.villageBiome), button -> {
			config.villageBiome = nextBiome(config.villageBiome);
			button.setMessage(Component.literal("村バイオーム: " + config.villageBiome));
			RtaConfigStore.save();
		}).bounds(right, y, 200, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("完了"), button -> {
			RtaConfigStore.save();
			this.minecraft.setScreen(parent);
		}).bounds(this.width / 2 - 100, this.height - 32, 200, 20).build());
	}

	@Override
	public void onClose() {
		RtaConfigStore.save();
		this.minecraft.setScreen(parent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress) {
		super.extractRenderState(graphics, mouseX, mouseY, tickProgress);
		graphics.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFFFF);
	}

	private void addToggle(int x, int y, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		this.addRenderableWidget(Button.builder(toggleLabel(label, getter.get()), button -> {
			boolean next = !getter.get();
			setter.accept(next);
			button.setMessage(toggleLabel(label, next));
			RtaConfigStore.save();
		}).bounds(x, y, 200, 20).build());
	}

	private static Component toggleLabel(String label, boolean enabled) {
		return Component.literal(label + ": " + (enabled ? "ON" : "OFF"));
	}

	private static RtaConfig.HudPosition nextHudPosition(RtaConfig.HudPosition current) {
		RtaConfig.HudPosition[] values = RtaConfig.HudPosition.values();
		return values[(current.ordinal() + 1) % values.length];
	}

	private static String label(RtaConfig.HudPosition position) {
		return switch (position) {
			case TOP_LEFT -> "左上";
			case TOP_RIGHT -> "右上";
			case BOTTOM_LEFT -> "左下";
			case BOTTOM_RIGHT -> "右下";
		};
	}

	private static String nextBiome(String current) {
		for (int index = 0; index < VILLAGE_BIOMES.length; index++) {
			if (VILLAGE_BIOMES[index].equals(current)) {
				return VILLAGE_BIOMES[(index + 1) % VILLAGE_BIOMES.length];
			}
		}
		return VILLAGE_BIOMES[0];
	}
}
