package qumassotntrun.phases;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import qumassotntrun.QumassoTNTRunPlugin;
import qumassotntrun.arena.GameArena;

public class RestartingPhase implements Phase {
	public static final int SECONDS = 10;

	private GameArena game;
	private BukkitTask task;
	private int secondsBeforeEnd;

	public RestartingPhase(GameArena game) {
		this.game = game;
	}

	@Override
	public void start() {
		secondsBeforeEnd = SECONDS;
		task = Bukkit.getScheduler().runTaskTimer(QumassoTNTRunPlugin.getInstance(), () -> {
			secondsBeforeEnd--;
			if (secondsBeforeEnd <= 0) {
				game.restart();
				stop();
			}
			game.updateBossbar(String.format("Restart in %d seconds", secondsBeforeEnd), (double) secondsBeforeEnd / SECONDS);
		}, 0, 20);
	}

	@Override
	public void stop() {
		if (task != null && !task.isCancelled()) task.cancel();
	}

	@Override
	public boolean onPlayerJoined(Player p) {
		return false;
	}

	@Override
	public void onPlayerQuit(Player p) {

	}

	@Override
	public void onPlayerDied(Player p) {

	}
}
