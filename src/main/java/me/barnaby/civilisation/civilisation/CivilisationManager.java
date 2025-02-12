package me.barnaby.civilisation.civilisation;

import me.barnaby.civilisation.config.ConfigManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Manages civilisations, including their spawn points and player memberships.
 */
public class CivilisationManager {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final LuckPerms luckPerms;
    private final Map<String, Location> civilisationSpawns = new HashMap<>();
    private final Random random = new Random();

    /**
     * Constructor now takes a LuckPerms instance.
     */
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
            // Set the associated LuckPerms group for this civilisation (for example, "default")
            config.set(exampleCiv + ".rank", "default");

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
     *
     * @param civilisation The name of the civilisation.
     * @return The spawn location of the civilisation.
     */
    public Location getCivilisationSpawn(String civilisation) {
        return civilisationSpawns.get(civilisation);
    }

    /**
     * Determines which civilisation a player belongs to. First it checks if the player’s name is already
     * stored in a civilisation’s player list. If not, it uses LuckPerms to check if the player has the group
     * (as defined by the civilisation’s "rank" key) and, if so, adds the player to that civilisation.
     *
     * @param player The player to check.
     * @return The name of the player's civilisation, or null if they are not in one.
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
     *
     * @param player The player to teleport.
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
     * Retrieves a location from the configuration.
     *
     * @param config The configuration file.
     * @param path   The configuration path.
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
     * Retrieves a list of all registered civilisation names.
     *
     * @return A list of civilisation names.
     */
    public List<String> getCivilisations() {
        return new ArrayList<>(civilisationSpawns.keySet());
    }
}

