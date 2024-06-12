package qumassotntrun.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import qumassotntrun.QumassoTNTRunPlugin;
import qumassotntrun.Utils;
import qumassotntrun.phases.Phase;
import qumassotntrun.phases.RestartingPhase;
import qumassotntrun.phases.StartingPhase;
import qumassotntrun.phases.WaitingPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameArena {
	private List<Player> alivePlayers = new ArrayList<>(), spectators = new ArrayList<>();
	private ArenaModel model;
	private Phase current;
	private BossBar bar = Bukkit.createBossBar("Beginning in %d seconds", BarColor.PURPLE, BarStyle.SOLID);;
	private BlockData[][][] region;
	private List<Item> spawnedBonuses = new ArrayList<>();
	private Random random = new Random();

	public GameArena(ArenaModel model) {
		this.model = model;
		save();
		restart();
	}

	public void onWin() {
		if (alivePlayers() == 1) {
			setPhase(new RestartingPhase(this));
			spectators.forEach(spec -> QumassoTNTRunPlugin.getInstance().systemMessage("Player %s has won!".formatted(alivePlayers.get(0).getName()), spec));
			QumassoTNTRunPlugin.getInstance().systemMessage("You have won", alivePlayers.get(0));
		}
	}

	public void makeSpectator(Player p) {
		alivePlayers.remove(p);
		spectators.add(p);
		Utils.cleanup(p, "You died", model.getArenaSpawn());
		p.setAllowFlight(true);
		p.setFlying(true);
		Bukkit.getOnlinePlayers().forEach(player -> {
			if (p == player) return;
			player.hidePlayer(QumassoTNTRunPlugin.getInstance(), p);
		});
	}

	public void playerDied(Player p) {
		current.onPlayerDied(p);
	}

	public boolean isSpectator(Player p) {
		return alivePlayers.contains(p);
	}

	public void teleport(Player p) {
		Utils.cleanup(p, "", model.getArenaSpawn());
	}

	public void save() {
		if (model.getFirstCorner() == null || model.getSecondCorner() == null) return;
		int minX = Math.min(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX());
		int maxX = Math.max(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX());
		int minY = Math.min(model.getFirstCorner().getBlockY(), model.getSecondCorner().getBlockY());
		int maxY = Math.max(model.getFirstCorner().getBlockY(), model.getSecondCorner().getBlockY());
		int minZ = Math.min(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ());
		int maxZ = Math.max(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ());
		region = new BlockData[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];
		World world = model.getFirstCorner().getWorld();
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					region[x - minX][y - minY][z - minZ] = world.getBlockData(x, y, z);
				}
			}
		}
	}

	public void restore() {
		if (model.getFirstCorner() == null || model.getSecondCorner() == null || region == null) return;
		int minX = Math.min(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX());
		int maxX = Math.max(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX());
		int minY = Math.min(model.getFirstCorner().getBlockY(), model.getSecondCorner().getBlockY());
		int maxY = Math.max(model.getFirstCorner().getBlockY(), model.getSecondCorner().getBlockY());
		int minZ = Math.min(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ());
		int maxZ = Math.max(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ());
		World world = model.getFirstCorner().getWorld();
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					world.setBlockData(x, y, z, region[x - minX][y - minY][z - minZ]);
				}
			}
		}
	}

	public void broadcast(String message) {
		alivePlayers.forEach(p -> p.sendMessage(Component.text(message)));
		spectators.forEach(p -> p.sendMessage(Component.text(message)));
	}

	public void spawnBonus() {
		int minX = Math.min(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX());
		int maxX = Math.max(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX());
		int minY = Math.min(model.getFirstCorner().getBlockY(), model.getSecondCorner().getBlockY());
		int maxY = Math.max(model.getFirstCorner().getBlockY(), model.getSecondCorner().getBlockY());
		int minZ = Math.min(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ());
		int maxZ = Math.max(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ());
		int x = random.nextInt(minX, maxX), y = random.nextInt(minY, maxY), z = random.nextInt(minZ, maxZ);
		Item bonus = QumassoTNTRunPlugin.getInstance().spawnDoubleJump(new Location(model.getFirstCorner().getWorld(), x, y, z));
		bonus.setCustomNameVisible(true);
		bonus.customName(Component.text("Double jump").color(TextColor.color(0, 255, 0)));
		spawnedBonuses.add(bonus);
	}

	public void restart() {
		bar.removeAll();
		restore();
		setPhase(new WaitingPhase(this));
		if (alivePlayers != null) alivePlayers.forEach(p -> {
			Utils.cleanup(p, "", QumassoTNTRunPlugin.getInstance().getSpawn());
			QumassoTNTRunPlugin.getInstance().giveInitialItems(p);
			p.setAllowFlight(false);
		});
		if (spectators != null) spectators.forEach(p -> {
			Utils.cleanup(p, "", QumassoTNTRunPlugin.getInstance().getSpawn());
			QumassoTNTRunPlugin.getInstance().giveInitialItems(p);
			p.setAllowFlight(false);
		});
		alivePlayers = new ArrayList<>();
		spectators = new ArrayList<>();
		spawnedBonuses.forEach(bonus -> {
			if (!bonus.isValid()) return;
			bonus.remove();
		});
		spawnedBonuses.clear();
	}

	public int floorHeight() {
		return model.getFloorHeight();
	}

	public List<Player> getAlivePlayers() {
		return alivePlayers;
	}

	public void updateBossbar(String title, double progress) {
		bar.setTitle(title);
		bar.setProgress(progress);
	}

	public void setPhase(Phase phase) {
		if (current != null) current.stop();
		current = phase;
		phase.start();
	}

	public boolean onPlayerJoined(Player joined) {
		return current.onPlayerJoined(joined);
	}

	public boolean onPlayerQuit(Player quited) {
		if (!alivePlayers.contains(quited)) return false;
		alivePlayers.remove(quited);
		spectators.remove(quited);
		current.onPlayerQuit(quited);
		bar.removePlayer(quited);
		return true;
	}

	public boolean containsPlayer(Player p) {
		return alivePlayers.contains(p);
	}

	public boolean hasFreeSlots() {
		return alivePlayers.size() < model.getMaxPlayersCount();
	}

	public boolean hasMinPlayersForStart() {
		return alivePlayers.size() >= model.getMinPlayersCount();
	}

	public void addPlayer(Player p) {
		alivePlayers.forEach(player -> player.sendMessage(Component.text(p.getName() + " joined the game!")));
		alivePlayers.add(p);
		Location loc = p.getLocation();
		if (model.getArenaSpawn() != null) loc = model.getArenaSpawn();
		Utils.cleanup(p, "You joined the game " + model.getName(), loc);
		bar.addPlayer(p);
		QumassoTNTRunPlugin.getInstance().giveArenaItems(p);
	}

	public int alivePlayers() {
		return alivePlayers.size();
	}

	public int minPlayers() {
		return model.getMinPlayersCount();
	}

	public int maxPlayers() {
		return model.getMaxPlayersCount();
	}

	public String name() {
		return model.getName();
	}
}
