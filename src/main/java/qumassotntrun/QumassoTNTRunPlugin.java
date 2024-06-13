package qumassotntrun;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import qumassotntrun.arena.ArenaManager;
import qumassotntrun.arena.ArenaModel;
import qumassotntrun.items.ItemManager;
import qumassotntrun.player.PlayerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class QumassoTNTRunPlugin extends JavaPlugin implements ArenaConfigurer, Listener {
    private static QumassoTNTRunPlugin plugin;

    public static QumassoTNTRunPlugin getInstance() {
        return plugin;
    }

    private ArenaManager arenaManager = new ArenaManager();
    private List<ArenaModel> models;
    private PlayerManager playerManager = new PlayerManager();
    private Location spawn;
    private ItemManager itemManager = new ItemManager();

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(ArenaModel.class);
        saveDefaultConfig();
        plugin = this;
        getServer().getPluginManager().registerEvents(arenaManager, this);
        models = (List<ArenaModel>) getConfig().getList("arenas");
        if (models == null) models = new ArrayList<>();
        if (models != null) models.forEach(model -> arenaManager.addArena(model));
        arenaManager.save();
        getCommand("setcorner").setExecutor(this);
        getCommand("setarenaspawn").setExecutor(this);
        getCommand("setspawn").setExecutor(this);
        getCommand("createarena").setExecutor(this);
        spawn = getConfig().getLocation("spawn");
        getServer().getPluginManager().registerEvents(playerManager, this);
        getServer().getPluginManager().registerEvents(itemManager, this);
    }

    public void giveArenaItems(Player p) {
        itemManager.giveArenaItems(p);
    }

    public void giveInitialItems(Player p) {
        itemManager.giveInitialItems(p);
    }

    public Location getSpawn() {
        return spawn;
    }

    public void openArenaMenu(Player p) {
        arenaManager.openArenasList(p);
    }

    public void leaveFromArena(Player p) {
        arenaManager.onLeaveFromArena(p);
        if (spawn != null) Utils.cleanup(p, "You leaved from the arena", spawn);
        else Utils.cleanup(p, "You leaved from the arena", p.getLocation());
        giveInitialItems(p);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command");
            return true;
        }
        Player p = (Player) sender;
        if (!p.isOp()) {
            p.sendMessage("Only operators can execute this command");
            return true;
        }
        if (label.equalsIgnoreCase("setcorner")) {
            if (args.length < 2) {
                systemError("Too few arguments", p);
                return true;
            }
            Optional<ArenaModel> model = getArena(args[0]);
            if (model.isEmpty()) {
                systemError("Such arena does not exist", p);
                return true;
            }
            if (args[1].equalsIgnoreCase("1")) {
                setFirstCornerForArena(args[0], p.getLocation());
                systemMessage("You successfully set the first corner for " + args[0], p);
                return true;
            }
            else if (args[1].equalsIgnoreCase("2")) {
                setSecondCornerForArena(args[0], p.getLocation());
                systemMessage("You successfully set the second corner for " + args[0], p);
                return true;
            }
            else {
                systemError("Second argument is either 1 or 2", p);
                return true;
            }
        } else if (label.equalsIgnoreCase("setarenaspawn")) {
            if (args.length < 1) {
                systemError("Too few arguments", p);
                return true;
            }
            Optional<ArenaModel> model = getArena(args[0]);
            if (model.isEmpty()) {
                systemError("Such arena does not exist", p);
                return true;
            }
            setArenaSpawnForArena(args[0], p.getLocation());
        } else if (label.equalsIgnoreCase("createarena")) {
            if (args.length < 4) {
                systemError("Too few arguments", p);
                return true;
            }
            String arenaName = args[0];
            try {
                int minPlayers = Integer.parseInt(args[1]), maxPlayers = Integer.parseInt(args[2]), floorHeight = Integer.parseInt(args[3]);
                createArena(arenaName, minPlayers, maxPlayers, floorHeight);
            } catch (Exception e) {
                systemError("Last two arguments should be integer", p);
                e.printStackTrace();
            }
        } else if (label.equalsIgnoreCase("setspawn")) {
            getConfig().set("spawn", p.getLocation());
            saveConfig();
            systemMessage("You successfully set the global spawn", p);
        }
        return true;
    }

    public Item spawnDoubleJump(Location loc) {
        return itemManager.spawnDoubleJump(loc);
    }

    public int getSecondsBeforeStart() {
        return  10;
    }

    @Override
    public void onDisable() {
        arenaManager.restore();
    }

    private Optional<ArenaModel> getArena(String name) {
        for (ArenaModel model : models) if (model.getName().equals(name)) return Optional.of(model);
        return Optional.empty();
    }

    @Override
    public boolean arenaExists(String name) {
        return getArena(name).isPresent();
    }

    public void playerJoined(Player p) {
        Location spawn = p.getLocation();
        if (QumassoTNTRunPlugin.getInstance().getSpawn() != null) Utils.cleanup(p, "Welcome to TNT Run!", QumassoTNTRunPlugin.getInstance().getSpawn());
        else Utils.cleanup(p, "Welcome to TNT Run!", spawn);
        QumassoTNTRunPlugin.getInstance().giveInitialItems(p);
    }

    @Override
    public void createArena(String name, int minPlayers, int maxPlayer, int floorheight) {
        ArenaModel model = new ArenaModel(name, minPlayers, maxPlayer, floorheight);
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i).getName().equals(name)) {
                models.remove(models.get(i));
                Bukkit.broadcast(Component.text("Arena %s already exists. Deleting...".formatted(name)));
                break;
            }
        }
        arenaManager.addArena(model);
        Bukkit.broadcast(Component.text("Arena %s has been created".formatted(name)));
        models.add(model);
        saveChanges();
    }

    @Override
    public void setArenaSpawnForArena(String arena, Location loc) {
        Optional<ArenaModel> model = getArena(arena);
        if (!model.isPresent()) return;
        model.get().setArenaSpawn(loc);
        saveChanges();
        Bukkit.getOnlinePlayers().forEach(p -> systemMessage("Successfully set the spawn for arena " + arena, p));
    }

    @Override
    public void setFirstCornerForArena(String arena, Location loc) {
        Optional<ArenaModel> model = getArena(arena);
        if (!model.isPresent()) return;
        model.get().setFirstCorner(loc);
        saveChanges();
        Bukkit.getOnlinePlayers().forEach(p -> systemMessage("Successfully set the first corner for arena " + arena, p));
        checkArenaForSaving(arena);
    }

    private void saveChanges() {
        getConfig().set("arenas", models);
        saveConfig();
    }

    @Override
    public void setSecondCornerForArena(String arena, Location loc) {
        Optional<ArenaModel> model = getArena(arena);
        if (!model.isPresent()) return;
        model.get().setSecondCorner(loc);
        saveChanges();
        Bukkit.getOnlinePlayers().forEach(p -> systemMessage("Successfully set the second corner for arena " + arena, p));
        checkArenaForSaving(arena);
    }

    private void checkArenaForSaving(String arenaName) {
        arenaManager.checkArenaForSaving(arenaName);
    }

    public void systemMessage(String message, Player receiver) {
        Component text = Component.text("[TNTRun] ");
        text = text.append(Component.text(message));
        receiver.sendMessage(text);
    }

    public void systemError(String message, Player receiver) {
        Component text = Component.text("[TNTRun] ");
        text = text.append(Component.text(message).color(TextColor.color(255, 0, 0)));
        receiver.sendMessage(text);
    }

    @EventHandler
    private void onChat(AsyncChatEvent e) {
        Component text = Component.text("[TNTRun] ");
        text = text.append(e.message());
        e.message(text);
    }
}
