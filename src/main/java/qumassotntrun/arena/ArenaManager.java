package qumassotntrun.arena;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import qumassotntrun.QumassoTNTRunPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArenaManager implements Listener {
	private List<GameArena> arenas = new ArrayList<>();
	private Inventory arenasInventory = Bukkit.createInventory(null, 3 * 9);

	public void addArena(ArenaModel model) {
		arenas.add(new GameArena(model, this));
	}

	public void openArenasList(Player p) {
		updateArenasList();
		p.openInventory(arenasInventory);
	}

	public void save() {
		arenas.forEach(arena -> arena.save());
	}

	public void restore() {
		arenas.forEach(arena -> arena.restore());
	}

	public void onLeaveFromArena(Player p) {
		for (GameArena arena : arenas) {
			if (arena.onPlayerQuit(p)) return;
		}
	}

	public void updateArenasList() {
		for (int i = 0; i < arenas.size(); i++) {
			ItemStack arenaItem = new ItemStack(Material.SLIME_BALL);
			ItemMeta meta = arenaItem.getItemMeta();
			meta.displayName(Component.text(arenas.get(i).name()));
			meta.lore(List.of(Component.text(arenas.get(i).alivePlayers() + "/" + arenas.get(i).maxPlayers())));
			arenaItem.setItemMeta(meta);
			arenasInventory.setItem(i, arenaItem);
		}
	}

	@EventHandler
	private void onInteract(PlayerInteractEvent e) {
		for (GameArena arena : arenas) {
			if (arena.isSpectator(e.getPlayer())) {
				e.setCancelled(true);
				return;
			}
		}
	}

	public void checkArenaForSaving(String arenaName) {
		for (GameArena game : arenas) {
			if (game.name().equals(arenaName)) {
				game.save();
				break;
			}
		}
	}

	@EventHandler
	private void onDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		if (e.getCause() == EntityDamageEvent.DamageCause.FALL) return;
		if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
		Player p = (Player) e.getEntity();
		for (GameArena arena : arenas) {
			if (arena.containsPlayer(p)) {
				arena.playerDied(p);
				return;
			}
		}
		p.teleport(QumassoTNTRunPlugin.getInstance().getSpawn());
	}

	@EventHandler
	private void onInteractEntity(PlayerInteractEntityEvent e) {
		for (GameArena arena : arenas) {
			if (arena.isSpectator(e.getPlayer())) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	private void onClick(InventoryClickEvent e) {
		if (e.getInventory() != arenasInventory) return;
		e.setCancelled(true);
		for (int i = 0; i < arenas.size(); i++) {
			if (Objects.equals(arenasInventory.getItem(i), e.getCurrentItem())) {
				arenas.get(i).onPlayerJoined((Player) e.getWhoClicked());
				break;
			}
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		for (GameArena arena : arenas) {
			if (arena.onPlayerQuit(e.getPlayer())) break;
		}
	}
}
