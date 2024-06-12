package qumassotntrun.phases;

import org.bukkit.entity.Player;

public interface Phase {
	void start();
	void stop();
	boolean onPlayerJoined(Player p);
	void onPlayerQuit(Player p);
	void onPlayerDied(Player p);
}
