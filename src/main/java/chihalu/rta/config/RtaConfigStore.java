package chihalu.rta.config;

import chihalu.rta.Rta;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RtaConfigStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("rta.json");
	private static RtaConfig config = new RtaConfig();

	private RtaConfigStore() {
	}

	public static RtaConfig get() {
		return config;
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			RtaConfig loaded = GSON.fromJson(reader, RtaConfig.class);
			config = loaded == null ? new RtaConfig() : loaded;
		} catch (IOException exception) {
			Rta.LOGGER.warn("RTA設定の読み込みに失敗しました。既定値を使用します。", exception);
			config = new RtaConfig();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException exception) {
			Rta.LOGGER.warn("RTA設定の保存に失敗しました。", exception);
		}
	}
}
