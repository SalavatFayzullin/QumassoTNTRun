package qumassotntrun.player;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import qumassotntrun.QumassoTNTRunPlugin;
import qumassotntrun.Utils;

public class PlayerManager implements Listener {
	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		QumassoTNTRunPlugin.getInstance().playerJoined(e.getPlayer());
	}

	@EventHandler
	private void onDamage(EntityDamageEvent e) {
		e.setCancelled(true);
		e.getEntity().setFireTicks(0);
	}

	@EventHandler
	private void onHunger(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}
}
