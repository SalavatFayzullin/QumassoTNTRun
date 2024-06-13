package qumassotntrun;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Utils {
	public static void cleanup(Player p, String message, Location location) {
		p.getInventory().clear();
		p.setHealth(20D);
		p.setFireTicks(0);
		if (message != null && !message.isEmpty()) QumassoTNTRunPlugin.getInstance().systemMessage(message, p);
		if (location == null) location = p.getLocation();
		p.teleport(location);
		p.setGameMode(GameMode.ADVENTURE);
	}
}
