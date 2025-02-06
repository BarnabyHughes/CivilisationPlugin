package me.barnaby.civilisation.civilisation;

import me.barnaby.civilisation.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages civilisations, including their spawn points and player memberships.
 */
public class CivilisationManager {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final Map<String, Location> civilisationSpawns = new HashMap<>();

    public CivilisationManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
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

        if (config.getConfigurationSection("civilisations").getKeys(false).isEmpty()) {
            String exampleCiv = "civilisations.exampleCiv";

            // Example spawn point (default at world spawn)
            Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
            config.set(exampleCiv + ".spawnPoint.world", spawn.getWorld().getName());
            config.set(exampleCiv + ".spawnPoint.x", spawn.getX());
            config.set(exampleCiv + ".spawnPoint.y", spawn.getY());
            config.set(exampleCiv + ".spawnPoint.z", spawn.getZ());
            config.set(exampleCiv + ".spawnPoint.yaw", spawn.getYaw());
            config.set(exampleCiv + ".spawnPoint.pitch", spawn.getPitch());

            // Example empty player list
            config.set(exampleCiv + ".players", new String[]{});

            plugin.getLogger().info(configManager.getMessage("civilisation.created_example"));
        }

        configManager.saveConfig();

        for (String civName : config.getConfigurationSection("civilisations").getKeys(false)) {
            String path = "civilisations." + civName + ".spawnPoint";
            if (config.contains(path)) {
                civilisationSpawns.put(civName, getLocationFromConfig(config, path));
            }
        }
    }

    /**
     * Retrieves the spawn location for a given civilisation.
     *
     * @param civilisation The name of the civilisation.
     * @return The spawn location of the civilisation.
     */
    public Location getCivilisationSpawn(String civilisation) {
        return civilisationSpawns.get(civilisation);
    }

    /**
     * Determines which civilisation a player belongs to.
     *
     * @param player The player to check.
     * @return The name of the player's civilisation, or null if they are not in one.
     */
    public String getPlayerCivilisation(Player player) {
        FileConfiguration config = configManager.getConfig();
        for (String civName : config.getConfigurationSection("civilisations").getKeys(false)) {
            List<String> players = config.getStringList("civilisations." + civName + ".players");
            if (players.contains(player.getName())) {
                return civName;
            }
        }
        return null;
    }

    /**
     * Teleports a player to their civilisation's spawn point if they belong to one.
     *
     * @param player The player to teleport.
     */
    public void teleportToCivilisationSpawn(Player player) {
        String civ = getPlayerCivilisation(player);
        if (civ != null && civilisationSpawns.containsKey(civ)) {
            player.teleport(civilisationSpawns.get(civ));
            player.sendMessage(configManager.getMessage("civilisation.welcome", civ));
        }
    }

    /**
     * Retrieves a location from the configuration.
     *
     * @param config The configuration file.
     * @param path The configuration path.
     * @return The location stored in the configuration.
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
     * Retrieves a list of all registered civilisations.
     *
     * @return A list of civilisation names.
     */
    public List<String> getCivilisations() {
        return new ArrayList<>(civilisationSpawns.keySet());
    }
}

