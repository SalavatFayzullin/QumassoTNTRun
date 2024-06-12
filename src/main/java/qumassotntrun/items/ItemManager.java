package qumassotntrun.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import qumassotntrun.QumassoTNTRunPlugin;

public class ItemManager implements Listener {
	private final ItemStack GAME_SELECTOR = new ItemStack(Material.SLIME_BALL);
	private final ItemStack LEAVE_ARENA = new ItemStack(Material.MAGMA_CREAM);
	private final ItemStack JUMP = new ItemStack(Material.FEATHER);

	{
		ItemMeta meta = GAME_SELECTOR.getItemMeta();
		meta.displayName(Component.text("Choose a game"));
		GAME_SELECTOR.setItemMeta(meta);
		meta = LEAVE_ARENA.getItemMeta();
		meta.displayName(Component.text("Leave from the game"));
		LEAVE_ARENA.setItemMeta(meta);
		meta = JUMP.getItemMeta();
		meta.displayName(Component.text("RMB to make double jump"));
		JUMP.setItemMeta(meta);
	}

	public Item spawnDoubleJump(Location loc) {
		Item item = loc.getWorld().spawn(loc, Item.class);
		item.setItemStack(JUMP);
		return item;
	}

	public void giveArenaItems(Player p) {
		p.getInventory().setItem(0, LEAVE_ARENA);
	}

	public void giveInitialItems(Player p) {
		p.getInventory().setItem(0, GAME_SELECTOR);
	}

	private void checkItem(ItemStack item, Player p) {
		if (item == null) return;
		ItemStack clone = item.clone();
		clone.setAmount(1);
		if (GAME_SELECTOR.equals(item)) QumassoTNTRunPlugin.getInstance().openArenaMenu(p);
		else if (LEAVE_ARENA.equals(item)) QumassoTNTRunPlugin.getInstance().leaveFromArena(p);
		else if (JUMP.equals(clone)) {
			item.setAmount(item.getAmount() - 1);
			p.setVelocity(p.getLocation().getDirection().normalize());
		}
	}

	@EventHandler
	private void onInteract(PlayerInteractEvent e) {
		checkItem(e.getItem(), e.getPlayer());
		e.setCancelled(true);
	}

	@EventHandler
	private void onDrop(PlayerDropItemEvent e) {
		checkItem(e.getItemDrop().getItemStack(), e.getPlayer());
		e.setCancelled(true);
	}
}
