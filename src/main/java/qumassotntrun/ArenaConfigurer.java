package qumassotntrun;

import org.bukkit.Location;

public interface ArenaConfigurer {
	boolean arenaExists(String name);
	void createArena(String name, int minPlayers, int maxPlayer, int floorHeight);
	void setArenaSpawnForArena(String arena, Location loc);
	void setFirstCornerForArena(String arena, Location loc);
	void setSecondCornerForArena(String arena, Location lco);
}
