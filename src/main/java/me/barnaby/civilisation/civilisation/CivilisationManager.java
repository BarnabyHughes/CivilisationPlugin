package me.barnaby.civilisation.civilisation;

import me.barnaby.civilisation.config.ConfigManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class CivilisationManager {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final LuckPerms luckPerms;
    private final Map<String, Location> civilisationSpawns = new HashMap<>();
    private final Random random = new Random();

    public CivilisationManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.luckPerms = LuckPermsProvider.get();
        loadCivilisations();
    }

    /**
     * Loads all civilisations from the configuration file.
     */
    private void loadCivilisations() {
        FileConfiguration config = configManager.getConfig();
        if (!config.contains("civilisations")) {
            config.createSection("civilisations");
        }

        // If no civilisation is defined, create an example one.
        if (config.getConfigurationSection("civilisations").getKeys(false).isEmpty()) {
            String exampleCiv = "civilisations.exampleCiv";

            // Example spawn point (defaulting to the world spawn)
            Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
            config.set(exampleCiv + ".spawnPoint.world", spawn.getWorld().getName());
            config.set(exampleCiv + ".spawnPoint.x", spawn.getX());
            config.set(exampleCiv + ".spawnPoint.y", spawn.getY());
            config.set(exampleCiv + ".spawnPoint.z", spawn.getZ());
            config.set(exampleCiv + ".spawnPoint.yaw", spawn.getYaw());
            config.set(exampleCiv + ".spawnPoint.pitch", spawn.getPitch());

            // Example empty player list
            config.set(exampleCiv + ".players", new ArrayList<String>());
            // The associated LuckPerms group for this civilisation
            config.set(exampleCiv + ".rank", "default");
            // New nametag-prefix option; {0} will be replaced with the player's name.
            config.set(exampleCiv + ".nametag-prefix", "&6Example &f");

            plugin.getLogger().info(configManager.getMessage("civilisation.created_example"));
        }

        configManager.saveConfig();

        // Load spawn locations for each civilisation.
        for (String civName : config.getConfigurationSection("civilisations").getKeys(false)) {
            String path = "civilisations." + civName + ".spawnPoint";
            if (config.contains(path)) {
                civilisationSpawns.put(civName, getLocationFromConfig(config, path));
            }
        }
    }

    /**
     * Retrieves the spawn location for a given civilisation.
     */
    public Location getCivilisationSpawn(String civilisation) {
        return civilisationSpawns.get(civilisation);
    }

    /**
     * Determines which civilisation a player belongs to.
     * If the player isn’t already registered, it uses LuckPerms (checking all inherited groups)
     * to see if the player has the group associated with the civilisation.
     * If a match is found, the player is added to that civilisation’s list.
     */
    public String getPlayerCivilisation(Player player) {
        FileConfiguration config = configManager.getConfig();
        // Check if the player is already registered.
        for (String civName : config.getConfigurationSection("civilisations").getKeys(false)) {
            List<String> players = config.getStringList("civilisations." + civName + ".players");
            if (players.contains(player.getName())) {
                return civName;
            }
        }
        // Otherwise, check group membership via LuckPerms.
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            for (String civName : config.getConfigurationSection("civilisations").getKeys(false)) {
                String civRank = config.getString("civilisations." + civName + ".rank");
                if (civRank != null) {
                    boolean hasGroup = user.getInheritedGroups(user.getQueryOptions()).stream()
                            .anyMatch(group -> group.getName().equalsIgnoreCase(civRank));
                    if (hasGroup) {
                        // Add the player to the civilisation's player list.
                        List<String> players = config.getStringList("civilisations." + civName + ".players");
                        if (!players.contains(player.getName())) {
                            players.add(player.getName());
                            config.set("civilisations." + civName + ".players", players);
                            configManager.saveConfig();
                        }
                        return civName;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Teleports a player to their civilisation's spawn point if they belong to one.
     */
    public void teleportToCivilisationSpawn(Player player) {
        String civ = getPlayerCivilisation(player);
        if (civ != null && civilisationSpawns.containsKey(civ)) {
            int teleportRadius = configManager.getConfig().getInt("civilisation-join-radius", 0);
            int addX = random.nextInt(teleportRadius * 2 + 1) - teleportRadius;
            int addZ = random.nextInt(teleportRadius * 2 + 1) - teleportRadius;

            Location spawn = civilisationSpawns.get(civ).clone().add(addX, 0, addZ);
            player.teleport(spawn);
            player.sendMessage(configManager.getMessage("civilisation.welcome", civ));
        }
    }

    /**
     * Applies a nametag prefix for the player based on their civilisation.
     * The prefix is read from the config (e.g. "&7Example {0}"), color codes are applied,
     * and the placeholder "{0}" is replaced with the player's name.
     * The prefix is then applied via a per-player scoreboard team.
     */
    public void applyNametagPrefix(Player player) {
        String civ = getPlayerCivilisation(player);
        if (civ == null) {
            return;
        }
        String prefix = configManager.getConfig().getString("civilisations." + civ + ".nametag-prefix", "");
        if (prefix.isEmpty()) {
            return;
        }
        // Replace {0} with the player's name and apply color codes.
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getMainScoreboard();
        // Create a unique team name for the player; ensure it is 16 characters or fewer.
        String teamName = "nt_" + civ;
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }
        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);

        team.setPrefix(prefix);
        char color = ChatColor.getLastColors(prefix).charAt(1);
        ChatColor color1 = ChatColor.getByChar(color);
        if (color1 != null) team.setColor(color1);
        team.addEntry(player.getName());
    }

    /**
     * Retrieves a location from the configuration.
     */
    private Location getLocationFromConfig(FileConfiguration config, String path) {
        String world = config.getString(path + ".world");
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");

        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    /**
     * Retrieves a list of all registered civilisation names.
     */
    public List<String> getCivilisations() {
        return new ArrayList<>(civilisationSpawns.keySet());
    }
}
