package qumassotntrun.arena;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ArenaModel implements ConfigurationSerializable {
	private String name;
	private Location arenaSpawn;
	private Location firstCorner, secondCorner;
	private int minPlayersCount, maxPlayersCount;
	private int floorHeight;

	@Override
	public @NotNull Map<String, Object> serialize() {
		Map<String , Object> map = new HashMap<>();
		map.put("name", name);
		map.put("arenaSpawn", arenaSpawn);
		map.put("firstCorner", firstCorner);
		map.put("secondCorner", secondCorner);
		map.put("minPlayersCount", minPlayersCount);
		map.put("maxPlayersCount", maxPlayersCount);
		map.put("floorHeight", floorHeight);
		return map;
	}

	public ArenaModel(String name, int minPlayersCount, int maxPlayersCount, int floorHeight) {
		this.name = name;
		this.minPlayersCount = minPlayersCount;
		this.maxPlayersCount = maxPlayersCount;
		this.floorHeight = floorHeight;
	}

	public ArenaModel() {
	}

	public static ArenaModel deserialize(Map<String, Object> map) {
		ArenaModel model = new ArenaModel();
		model.name = (String) map.get("name");
		model.arenaSpawn = (Location) map.get("arenaSpawn");
		model.firstCorner = (Location) map.get("firstCorner");
		model.secondCorner = (Location) map.get("secondCorner");
		model.minPlayersCount = (Integer) map.get("minPlayersCount");
		model.maxPlayersCount = (Integer) map.get("maxPlayersCount");
		model.floorHeight = (Integer) map.get("floorHeight");
		return model;
	}

	public int getFloorHeight() {
		return floorHeight;
	}

	public void setFloorHeight(int floorHeight) {
		this.floorHeight = floorHeight;
	}

	public Location getArenaSpawn() {
		return arenaSpawn;
	}

	public void setArenaSpawn(Location arenaSpawn) {
		this.arenaSpawn = arenaSpawn;
	}

	public Location getFirstCorner() {
		return firstCorner;
	}

	public void setFirstCorner(Location firstCorner) {
		this.firstCorner = firstCorner;
	}

	public Location getSecondCorner() {
		return secondCorner;
	}

	public void setSecondCorner(Location secondCorner) {
		this.secondCorner = secondCorner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMinPlayersCount() {
		return minPlayersCount;
	}

	public void setMinPlayersCount(int minPlayersCount) {
		this.minPlayersCount = minPlayersCount;
	}

	public int getMaxPlayersCount() {
		return maxPlayersCount;
	}

	public void setMaxPlayersCount(int maxPlayersCount) {
		this.maxPlayersCount = maxPlayersCount;
	}
}
