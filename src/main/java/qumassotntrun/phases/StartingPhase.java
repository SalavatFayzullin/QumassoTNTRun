package qumassotntrun.phases;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import qumassotntrun.QumassoTNTRunPlugin;
import qumassotntrun.arena.GameArena;

public class StartingPhase implements Phase {
	private GameArena game;
	private BukkitTask task;
	private int secondsLeft;

	public StartingPhase(GameArena game) {
		this.game = game;
		game.updateBossbar(String.format("Beginning in %d seconds", secondsLeft), (double) secondsLeft / QumassoTNTRunPlugin.getInstance().getSecondsBeforeStart());
	}

	@Override
	public void start() {
		secondsLeft = QumassoTNTRunPlugin.getInstance().getSecondsBeforeStart();
		task = Bukkit.getScheduler().runTaskTimer(QumassoTNTRunPlugin.getInstance(), () -> {
			secondsLeft--;
			if (secondsLeft <= 0) {
				stop();
				game.setPhase(new ActivePhase(game));
				return;
			}
			if (secondsLeft % 5 == 0 || secondsLeft < 5) game.broadcast(String.format("The game will start in %d seconds", secondsLeft));
			game.updateBossbar(String.format("Beginning in %d seconds", secondsLeft), (double) secondsLeft / QumassoTNTRunPlugin.getInstance().getSecondsBeforeStart());
		}, 0, 20);
	}

	@Override
	public void stop() {
		if (task != null && !task.isCancelled()) task.cancel();
	}

	@Override
	public boolean onPlayerJoined(Player p) {
		if (game.containsPlayer(p) || !game.hasFreeSlots()) return false;
		game.addPlayer(p);
		return true;
	}

	@Override
	public void onPlayerQuit(Player p) {
		if (!game.hasMinPlayersForStart()) {
			stop();
			game.setPhase(new WaitingPhase(game));
		}
	}

	@Override
	public void onPlayerDied(Player p) {
		game.teleport(p);
	}
}
