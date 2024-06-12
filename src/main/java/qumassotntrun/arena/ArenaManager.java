package qumassotntrun.arena;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArenaManager implements Listener {
	private List<GameArena> arenas = new ArrayList<>();
	private Inventory arenasInventory = Bukkit.createInventory(null, 3 * 9);
	private List<Player> openedInventoryPlayers = new ArrayList<>();

	public void addArena(ArenaModel model) {
		arenas.add(new GameArena(model));
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

	private void updateArenasList() {
		for (int i = 0; i < arenas.size(); i++) {
			ItemStack arenaItem = new ItemStack(Material.SLIME_BALL);
			ItemMeta meta = arenaItem.getItemMeta();
			meta.displayName(Component.text(arenas.get(i).name()));
			meta.lore(List.of(Component.text(arenas.get(i).alivePlayers() + "/" + arenas.get(i).maxPlayers())));
			arenaItem.setItemMeta(meta);
			arenasInventory.setItem(i, arenaItem);
		}
		for (Player p : openedInventoryPlayers) p.updateInventory();
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

	@EventHandler
	private void onDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		if (e.getCause() == EntityDamageEvent.DamageCause.FALL) return;
		Player p = (Player) e.getEntity();
		for (GameArena arena : arenas) {
			if (arena.containsPlayer(p)) {
				arena.playerDied(p);
				return;
			}
		}
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
	private void onInventoryOpen(InventoryOpenEvent e) {
		if (e.getInventory() == arenasInventory) openedInventoryPlayers.add((Player) e.getPlayer());
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory() == arenasInventory) openedInventoryPlayers.remove((Player) e.getPlayer());
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		for (GameArena arena : arenas) {
			if (arena.onPlayerQuit(e.getPlayer())) break;
		}
		openedInventoryPlayers.remove(e.getPlayer());
	}
}
