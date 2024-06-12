package qumassotntrun.phases;

import org.bukkit.entity.Player;
import qumassotntrun.arena.GameArena;

public class WaitingPhase implements Phase {
	private GameArena game;

	public WaitingPhase(GameArena game) {
		this.game = game;
	}

	@Override
	public void start() {
		game.updateBossbar(String.format("Waiting for people %d/%d", game.alivePlayers(), game.maxPlayers()), (double) game.alivePlayers() / game.maxPlayers());
	}

	@Override
	public void stop() {

	}

	@Override
	public boolean onPlayerJoined(Player p) {
		if (game.containsPlayer(p) || !game.hasFreeSlots()) return false;
		game.addPlayer(p);
		if (game.hasMinPlayersForStart()) game.setPhase(new StartingPhase(game));
		return true;
	}

	@Override
	public void onPlayerQuit(Player p) {

	}

	@Override
	public void onPlayerDied(Player p) {
		game.teleport(p);
	}
}
