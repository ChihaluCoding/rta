package chihalu.rta.client;

import chihalu.rta.Rta;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerSkin;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class RtaClientReplayManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	private static final AtomicInteger GHOST_ENTITY_IDS = new AtomicInteger(-100000);
	private static final int MAX_FRAMES = 20 * 60 * 60 * 6;
	private static Recorder recorder;
	private static Playback playback;

	private RtaClientReplayManager() {
	}

	public static void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> dispatcher.register(ClientCommands.literal("rta")
			.then(ClientCommands.literal("replay")
				.executes(context -> {
					playLatest(Minecraft.getInstance());
					return Command.SINGLE_SUCCESS;
				})
				.then(ClientCommands.literal("start").executes(context -> {
					start(Minecraft.getInstance());
					return Command.SINGLE_SUCCESS;
				}))
				.then(ClientCommands.literal("stop").executes(context -> {
					stop(Minecraft.getInstance());
					return Command.SINGLE_SUCCESS;
				}))
				.then(ClientCommands.literal("play").executes(context -> {
					playLatest(Minecraft.getInstance());
					return Command.SINGLE_SUCCESS;
				}))
				.then(ClientCommands.literal("list").executes(context -> {
					list(Minecraft.getInstance());
					return Command.SINGLE_SUCCESS;
				})))));
	}

	public static void tick(Minecraft client) {
		if (client.player == null || client.level == null) {
			cleanupPlayback(client);
			return;
		}

		if (recorder != null) {
			recorder.capture(client);
		}

		if (playback != null && playback.tick(client)) {
			cleanupPlayback(client);
			client.player.sendSystemMessage(Component.literal("リプレイ再生が終了しました。"));
		}
	}

	public static void start(Minecraft client) {
		if (client.player == null || client.level == null) {
			return;
		}

		cleanupPlayback(client);
		recorder = new Recorder(client.player.getName().getString());
		client.player.sendSystemMessage(Component.literal("リプレイ録画を開始しました。"));
	}

	public static void stop(Minecraft client) {
		if (client.player == null) {
			return;
		}

		if (recorder == null) {
			client.player.sendSystemMessage(Component.literal("録画中のリプレイがありません。"));
			return;
		}

		Path saved = save(client);
		client.player.sendSystemMessage(Component.literal("リプレイ録画を保存しました: " + saved.getFileName()));
	}

	public static void playLatest(Minecraft client) {
		if (client.player == null || client.level == null) {
			return;
		}

		Optional<Path> latest = latestReplay(client);
		if (latest.isEmpty()) {
			client.player.sendSystemMessage(Component.literal("再生できるリプレイがありません。"));
			return;
		}

		try {
			ReplayFile replay = read(latest.get());
			if (replay.frames.isEmpty()) {
				client.player.sendSystemMessage(Component.literal("リプレイにフレームがありません。"));
				return;
			}

			cleanupPlayback(client);
			recorder = null;
			playback = new Playback(replay, client.player.getSkin());
			playback.positionViewerAtStart(client);
			client.player.sendSystemMessage(Component.literal("リプレイ再生を開始しました: " + latest.get().getFileName()));
		} catch (IOException exception) {
			Rta.LOGGER.warn("クライアントリプレイの読み込みに失敗しました。", exception);
			client.player.sendSystemMessage(Component.literal("リプレイの読み込みに失敗しました。"));
		}
	}

	private static void list(Minecraft client) {
		if (client.player == null) {
			return;
		}

		List<Path> files = replayFiles(client);
		if (files.isEmpty()) {
			client.player.sendSystemMessage(Component.literal("リプレイはまだありません。"));
			return;
		}

		client.player.sendSystemMessage(Component.literal("リプレイ一覧"));
		for (Path file : files) {
			client.player.sendSystemMessage(Component.literal(file.getFileName().toString()));
		}
	}

	private static Path save(Minecraft client) {
		Recorder activeRecorder = recorder;
		recorder = null;
		if (activeRecorder == null) {
			return replayDirectory(client).resolve("none.json");
		}

		ReplayFile replay = new ReplayFile();
		replay.version = 2;
		replay.playerName = activeRecorder.playerName;
		replay.createdAt = LocalDateTime.now().toString();
		replay.frames = activeRecorder.frames;

		Path path = replayDirectory(client).resolve("skin_replay_" + FILE_DATE.format(LocalDateTime.now()) + ".json");
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(replay, writer);
			}
		} catch (IOException exception) {
			Rta.LOGGER.warn("クライアントリプレイの保存に失敗しました。", exception);
		}
		return path;
	}

	private static ReplayFile read(Path path) throws IOException {
		try (Reader reader = Files.newBufferedReader(path)) {
			ReplayFile replay = GSON.fromJson(reader, ReplayFile.class);
			return replay == null ? new ReplayFile() : replay;
		}
	}

	private static Optional<Path> latestReplay(Minecraft client) {
		return replayFiles(client).stream().max(Comparator.comparing(path -> path.getFileName().toString()));
	}

	private static List<Path> replayFiles(Minecraft client) {
		Path directory = replayDirectory(client);
		if (!Files.isDirectory(directory)) {
			return List.of();
		}

		try (var stream = Files.list(directory)) {
			return stream
				.filter(path -> path.getFileName().toString().endsWith(".json"))
				.sorted()
				.toList();
		} catch (IOException exception) {
			Rta.LOGGER.warn("クライアントリプレイ一覧の読み込みに失敗しました。", exception);
			return List.of();
		}
	}

	private static Path replayDirectory(Minecraft client) {
		return client.gameDirectory.toPath().resolve("RTA_History").resolve("replays");
	}

	private static void cleanupPlayback(Minecraft client) {
		if (playback != null) {
			playback.cleanup(client);
			playback = null;
		}
	}

	private static final class Recorder {
		private final String playerName;
		private final List<ReplayFrame> frames = new ArrayList<>();
		private long tick;

		private Recorder(String playerName) {
			this.playerName = playerName;
		}

		private void capture(Minecraft client) {
			if (frames.size() >= MAX_FRAMES || client.player == null || client.level == null) {
				return;
			}

			ReplayFrame frame = new ReplayFrame();
			frame.tick = tick++;
			frame.dimension = client.level.dimension().identifier().toString();
			frame.x = client.player.getX();
			frame.y = client.player.getY();
			frame.z = client.player.getZ();
			frame.yaw = client.player.getYRot();
			frame.pitch = client.player.getXRot();
			frames.add(frame);
		}
	}

	private static final class Playback {
		private final ReplayFile replay;
		private final PlayerSkin skin;
		private int index;
		private RtaGhostPlayer ghost;

		private Playback(ReplayFile replay, PlayerSkin skin) {
			this.replay = replay;
			this.skin = skin;
		}

		private boolean tick(Minecraft client) {
			if (client.level == null || index >= replay.frames.size()) {
				return true;
			}

			ReplayFrame frame = replay.frames.get(index++);
			if (!frame.dimension.equals(client.level.dimension().identifier().toString())) {
				return index >= replay.frames.size();
			}

			RtaGhostPlayer activeGhost = ghostFor(client, frame);
			activeGhost.teleportTo(frame.x, frame.y, frame.z);
			activeGhost.setYRot(frame.yaw);
			activeGhost.setXRot(frame.pitch);
			activeGhost.setYHeadRot(frame.yaw);
			activeGhost.setYBodyRot(frame.yaw);
			activeGhost.setOldPosAndRot();
			return index >= replay.frames.size();
		}

		private void positionViewerAtStart(Minecraft client) {
			if (client.player == null || replay.frames.isEmpty()) {
				return;
			}

			ReplayFrame first = replay.frames.getFirst();
			double yawRadians = Math.toRadians(first.yaw);
			double viewX = first.x - Math.sin(yawRadians) * 4.0D;
			double viewZ = first.z + Math.cos(yawRadians) * 4.0D;
			client.player.teleportTo(viewX, first.y + 2.0D, viewZ);
		}

		private RtaGhostPlayer ghostFor(Minecraft client, ReplayFrame frame) {
			if (ghost != null && !ghost.isRemoved()) {
				return ghost;
			}

			GameProfile profile = new GameProfile(UUID.randomUUID(), replay.playerName);
			ghost = new RtaGhostPlayer(client.level, profile, skin);
			ghost.setId(GHOST_ENTITY_IDS.getAndDecrement());
			ghost.setUUID(UUID.randomUUID());
			ghost.setCustomName(Component.literal(replay.playerName + " のリプレイ"));
			ghost.setCustomNameVisible(true);
			ghost.setPos(frame.x, frame.y, frame.z);
			ghost.setYRot(frame.yaw);
			ghost.setXRot(frame.pitch);
			client.level.addEntity(ghost);
			return ghost;
		}

		private void cleanup(Minecraft client) {
			if (ghost != null && client.level != null) {
				client.level.removeEntity(ghost.getId(), Entity.RemovalReason.DISCARDED);
			}
			ghost = null;
		}
	}

	private static final class ReplayFile {
		private int version;
		private String playerName = "";
		private String createdAt = "";
		private List<ReplayFrame> frames = new ArrayList<>();
	}

	private static final class ReplayFrame {
		private long tick;
		private String dimension;
		private double x;
		private double y;
		private double z;
		private float yaw;
		private float pitch;
	}
}
