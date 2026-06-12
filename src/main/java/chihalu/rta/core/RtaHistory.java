package chihalu.rta.core;

import chihalu.rta.Rta;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class RtaHistory {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	private static final String DIRECTORY_NAME = "RTA_History";
	private static final String HISTORY_FILE = "history.txt";

	private RtaHistory() {
	}

	public static void append(MinecraftServer server, String timeText) {
		Path path = historyPath(server);
		String line = DATE_FORMAT.format(LocalDateTime.now()) + " / " + timeText;

		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, line + System.lineSeparator(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
		} catch (IOException exception) {
			Rta.LOGGER.warn("RTA履歴の保存に失敗しました。", exception);
		}
	}

	public static List<String> read(MinecraftServer server) {
		Path path = historyPath(server);
		if (!Files.exists(path)) {
			return List.of();
		}

		try {
			return Files.readAllLines(path);
		} catch (IOException exception) {
			Rta.LOGGER.warn("RTA履歴の読み込みに失敗しました。", exception);
			return List.of();
		}
	}

	private static Path historyPath(MinecraftServer server) {
		return server.getServerDirectory().resolve(DIRECTORY_NAME).resolve(HISTORY_FILE);
	}
}
