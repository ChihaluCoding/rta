package chihalu.rta.client;

public final class RtaClientTimer {
	private static boolean running;
	private static long startMillis;
	private static long elapsedMillis;
	private static long igtTicks;

	private RtaClientTimer() {
	}

	public static void start() {
		running = true;
		startMillis = System.currentTimeMillis();
		elapsedMillis = 0L;
		igtTicks = 0L;
	}

	public static void tick(boolean paused) {
		if (!running || paused) {
			return;
		}

		igtTicks++;
		elapsedMillis = System.currentTimeMillis() - startMillis;
	}

	public static void stop() {
		if (!running) {
			return;
		}

		elapsedMillis = System.currentTimeMillis() - startMillis;
		running = false;
	}

	public static long elapsedMillis() {
		return running ? System.currentTimeMillis() - startMillis : elapsedMillis;
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
}
