package qumassotntrun.phases;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import qumassotntrun.QumassoTNTRunPlugin;
import qumassotntrun.arena.GameArena;

public class ActivePhase implements Phase {
	private GameArena game;
	private BukkitTask task;
	private int blocksBroken = 0;
	private BukkitTask bonusSpawnTask;

	public ActivePhase(GameArena game) {
		this.game = game;
	}

	@Override
	public void start() {
		game.getAlivePlayers().forEach(alive -> alive.getInventory().clear());
		game.updateBossbar("Block broken %d".formatted(blocksBroken), 1);
		bonusSpawnTask = Bukkit.getScheduler().runTaskTimer(QumassoTNTRunPlugin.getInstance(), () -> game.spawnBonus(), 0, 20 * 5);
		task = Bukkit.getScheduler().runTaskTimer(QumassoTNTRunPlugin.getInstance(), () -> {
			game.getAlivePlayers().forEach(p -> {
				double yBelow = p.getLocation().getY() - 0.0001;
				World world = p.getWorld();
				double x = p.getX(), z = p.getZ();
				Block northEast = new Location(world, x + 0.3, yBelow, z - 0.3).getBlock();
				Block northWest = new Location(world, x - 0.3, yBelow, z - 0.3).getBlock();
				Block southEast = new Location(world, x + 0.3, yBelow, z + 0.3).getBlock();
				Block southWest = new Location(world, x - 0.3, yBelow, z + 0.3).getBlock();
				Block[] blocks = {northEast, northWest, southEast, southWest};

				Bukkit.getScheduler().runTaskLater(QumassoTNTRunPlugin.getInstance(), () -> {
					if (game.alivePlayers() == 0) return;
					for (int i = 0; i < blocks.length; i++) {
						if (blocks[i].getType() == Material.AIR) continue;
						game.updateBossbar("Blocks broken %d".formatted(++blocksBroken), 1);
						blocks[i].setType(Material.AIR);
						Block cur = blocks[i];
						for (int j = 0; j < game.floorHeight() - 1; j++) {
							cur = cur.getRelative(BlockFace.DOWN);
							cur.setType(Material.AIR);
						}
					}
				}, 10);
			});
		}, 0, 1);
	}

	@Override
	public void stop() {
		if (task != null && !task.isCancelled()) task.cancel();
		if (bonusSpawnTask != null && !bonusSpawnTask.isCancelled()) bonusSpawnTask.cancel();
	}

	@Override
	public boolean onPlayerJoined(Player p) {
		return false;
	}

	@Override
	public void onPlayerQuit(Player p) {
		game.broadcast(p.getName() + " has left the game");
		if (game.alivePlayers() == 1) game.onWin();
		if (game.alivePlayers() == 0) game.restart();
	}

	@Override
	public void onPlayerDied(Player p) {
		game.makeSpectator(p);
		if (game.alivePlayers() == 1) game.onWin();
		if (game.alivePlayers() == 0) game.restart();
	}
}
