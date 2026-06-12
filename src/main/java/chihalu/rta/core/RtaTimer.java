package chihalu.rta.core;

import net.minecraft.server.MinecraftServer;

public final class RtaTimer {
	private static boolean running;
	private static boolean finished;
	private static long realStartMillis;
	private static long realElapsedMillis;
	private static long igtTicks;

	private RtaTimer() {
	}

	public static void startIfNeeded() {
		if (running || finished) {
			return;
		}

		running = true;
		realStartMillis = System.currentTimeMillis();
		realElapsedMillis = 0L;
		igtTicks = 0L;
	}

	public static void tick(MinecraftServer server) {
		if (!running || server.isPaused()) {
			return;
		}

		igtTicks++;
		realElapsedMillis = System.currentTimeMillis() - realStartMillis;
	}

	public static void stop() {
		if (!running) {
			return;
		}

		realElapsedMillis = System.currentTimeMillis() - realStartMillis;
		running = false;
		finished = true;
	}

	public static void reset() {
		running = false;
		finished = false;
		realStartMillis = 0L;
		realElapsedMillis = 0L;
		igtTicks = 0L;
	}

	public static boolean isRunning() {
		return running;
	}

	public static long realElapsedMillis() {
		return running ? System.currentTimeMillis() - realStartMillis : realElapsedMillis;
	}

	public static long igtTicks() {
		return igtTicks;
	}

	public static String formatMillis(long millis) {
		long totalSeconds = Math.max(0L, millis / 1000L);
		long hours = totalSeconds / 3600L;
		long minutes = (totalSeconds % 3600L) / 60L;
		long seconds = totalSeconds % 60L;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	public static String formatTicks(long ticks) {
		return formatMillis(ticks * 50L);
	}
}
