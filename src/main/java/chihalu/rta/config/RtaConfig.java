package chihalu.rta.config;

public class RtaConfig {
	public boolean coordinateHud = true;
	public boolean fullBright = false;
	public boolean batchBreak = false;
	public boolean autoSmeltOres = false;
	public boolean guaranteedDrops = true;
	public boolean preventEyeBreak = true;
	public boolean preventEndermanCarry = true;
	public boolean showRemainingCrystals = true;
	public boolean spectatorAssist = true;
	public boolean villageSpawn = false;
	public String villageBiome = "minecraft:plains";
	public HudPosition hudPosition = HudPosition.TOP_LEFT;

	public enum HudPosition {
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT
	}
}
