package me.barnaby.civilisation.config;

import me.barnaby.civilisation.commands.ChatCommand;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ConfigManager handles the creation, loading, and saving of configuration files,
 * including settings for chat formats, airdrops, events, and customizable messages.
 */
public class ConfigManager {
    private final Plugin plugin;
    private File configFile;
    private FileConfiguration config;
    private final LuckPerms luckPerms;

    // Stores rank formats for quick access
    private final Map<String, String> rankFormats = new HashMap<>();

    public ConfigManager(Plugin plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
        loadConfig();
    }

    /**
     * Loads the configuration file and initializes required sections.
     */
    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        // Create a new config file if it doesn't exist
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Load all config sections
        loadRankFormats();
        loadAirdropConfig();
        loadEventsConfig();
        loadMessagesConfig();
    }

    /**
     * Loads rank formats and stores them in memory for quick access.
     */
    private void loadRankFormats() {
        if (!config.contains("chat-format")) {
            config.createSection("chat-format");
        }

        for (Group group : luckPerms.getGroupManager().getLoadedGroups()) {
            String rankName = group.getName().toLowerCase();
            String defaultFormat = "&7[" + "&e" + rankName + "&7] &f<player>: &7<message>";

            if (!config.contains("chat-format." + rankName)) {
                config.set("chat-format." + rankName, defaultFormat);
            }

            rankFormats.put(rankName, config.getString("chat-format." + rankName));
        }

        saveConfig();
    }

    /**
     * Loads airdrop settings and generates example types if not present.
     */
    private void loadAirdropConfig() {
        if (!config.contains("airdrop")) {
            config.createSection("airdrop");

            config.set("airdrop.interval", 7200); // Default: Every 2 hours
            config.set("airdrop.region.world", "world");
            config.set("airdrop.region.x1", -500);
            config.set("airdrop.region.z1", -500);
            config.set("airdrop.region.x2", 500);
            config.set("airdrop.region.z2", 500);

            if (!config.contains("airdrop.types")) {
                ConfigurationSection typesSection = config.createSection("airdrop.types");

                // Add Example Airdrop Types
                createExampleAirdropType(typesSection, "common", 40);
                createExampleAirdropType(typesSection, "rare", 20);
                createExampleAirdropType(typesSection, "legendary", 10);
                createExampleAirdropType(typesSection, "mystic", 5);
            }
        }
        saveConfig();
    }

    /**
     * Loads event settings and generates example events if not present.
     */
    private void loadEventsConfig() {
        if (!config.contains("events")) {
            config.createSection("events");

            ConfigurationSection eventsSection = config.getConfigurationSection("events");

            addExampleEvent(eventsSection, "festival_of_strength", "Festival of Strength", "GOOD",
                    Map.of("STRENGTH", 1, "SPEED", 1), 600);

            addExampleEvent(eventsSection, "suns_wrath", "Sun’s Wrath", "BAD",
                    Map.of(), 300, true);

            addExampleEvent(eventsSection, "draining_aura", "Draining Aura", "BAD",
                    Map.of("MINING_FATIGUE", 1), 600);
        }

        saveConfig();
    }

    /**
     * Loads the messages section in config, ensuring future customization of plugin messages.
     * If a message key is missing, it will be added with a default value.
     */
    private void loadMessagesConfig() {
        if (!config.contains("messages")) {
            config.createSection("messages");
        }

        ConfigurationSection messagesSection = config.getConfigurationSection("messages");

        setDefaultMessage(messagesSection, "general.no_permission", "&cYou do not have permission to do that.");
        setDefaultMessage(messagesSection, "general.not_a_player", "&cYou must be a player to run this!");
        setDefaultMessage(messagesSection, "general.unknown_command", "&cUnknown command. Type &e/help &cfor a list of commands.");
        setDefaultMessage(messagesSection, "general.player_not_found", "&cPlayer '{0}' is not online.");
        setDefaultMessage(messagesSection, "general.invalid_usage", "&cInvalid usage. Correct format: {0}");

        setDefaultMessage(messagesSection, "report.server", "&c&lREPORT &8> &f{0} has reported the &cserver &ffor: &6{1}");
        setDefaultMessage(messagesSection, "report.player", "&c&lREPORT &8> &f{0} has reported &c{1} &ffor: &6{2}");
        setDefaultMessage(messagesSection, "report.teleport_click", "&7&o(Click to teleport)");
        setDefaultMessage(messagesSection, "report.teleport_hover", "&dClick to teleport to the reported player.");
        setDefaultMessage(messagesSection, "report.success", "&aYour report has been sent to staff!");

        setDefaultMessage(messagesSection, "notify.default_message", "something is about to happen");
        setDefaultMessage(messagesSection, "notify.alert", "&d&lNOTIFY &8> &f{0} has notified: &d{1}");
        setDefaultMessage(messagesSection, "notify.teleport_click", "&7&o(Click to teleport)");
        setDefaultMessage(messagesSection, "notify.teleport_hover", "&dClick to teleport to the notifier.");
        setDefaultMessage(messagesSection, "notify.success", "&aYou have notified staff: &d{0}");

        setDefaultMessage(messagesSection, "chat.invalid", "&cInvalid chat type! Use: global, local, civilisation, or staff.");
        setDefaultMessage(messagesSection, "chat.staff_no_permission", "&cYou do not have permission to use staff chat.");
        setDefaultMessage(messagesSection, "chat.civilisation_no_membership", "&cYou are not part of any civilisation!");
        setDefaultMessage(messagesSection, "chat.success", "&aYou have switched to &d{0} &achat.");

        setDefaultMessage(messagesSection, "airdrop.spawned", "&aA &d{0} &aairdrop has been spawned!");
        setDefaultMessage(messagesSection, "airdrop.announcement", "&6&lAirdrop &8> &fA {0} airdrop has landed near &eX: {1} Z: {2}");
        setDefaultMessage(messagesSection, "airdrop.teleport_click", "&c[STAFF] Click here to teleport to the airdrop!");
        setDefaultMessage(messagesSection, "airdrop.teleport_hover", "&dClick to teleport to the airdrop.");

        setDefaultMessage(messagesSection, "civilisation.welcome", "&6Welcome to your civilisation: &e{0}");
        setDefaultMessage(messagesSection, "civilisation.no_membership", "&cYou are not part of any civilisation!");

        setDefaultMessage(messagesSection, "chat.global", "&6&lGLOBAL &8> &f{0}");
        setDefaultMessage(messagesSection, "chat.local", "&2&lLOCAL &8> &f{0}");
        setDefaultMessage(messagesSection, "chat.staff", "&d[STAFF] {0}");
        setDefaultMessage(messagesSection, "chat.staff_no_permission", "&cYou do not have permission to use the staff chat.");
        setDefaultMessage(messagesSection, "chat.civilisation", "&6[CIV] {0}");
        setDefaultMessage(messagesSection, "chat.civilisation_no_membership", "&cYou are not part of any civilisation!");

        setDefaultMessage(messagesSection, "event.start", "&e⚡ {0} has begun for {1}!");
        setDefaultMessage(messagesSection, "event.success", "&aSuccessfully triggered event {0} for {1}.");


        saveConfig();
    }


    /**
     * Ensures a message exists in the config. If missing, it sets a default value.
     * @param section The ConfigurationSection where the message should be stored.
     * @param key The message key.
     * @param defaultValue The default message value if not set.
     */
    private void setDefaultMessage(ConfigurationSection section, String key, String defaultValue) {
        if (!section.contains(key)) {
            section.set(key, defaultValue);
        }
    }

    public String getMessage(String key, Object... args) {
        String message = config.getString("messages." + key, "&c[Missing message: " + key + "]");

        // Replace placeholders {0}, {1}, {2}, etc.
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i].toString());
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }


    /**
     * Creates a new example airdrop type with predefined loot.
     */
    private void createExampleAirdropType(ConfigurationSection parentSection, String typeName, int chance) {
        ConfigurationSection typeSection = parentSection.createSection(typeName);
        typeSection.set("chance", chance);

        ConfigurationSection itemsSection = typeSection.createSection("items");

        addExampleItem(itemsSection, "DIAMOND", 16, 32, 40);
        addExampleItem(itemsSection, "NETHERITE_SCRAP", 1, 5, 20);
        addExampleItem(itemsSection, "GOLDEN_APPLE", 2, 5, 50);
    }

    /**
     * Adds an example event to the config.
     */
    private void addExampleEvent(ConfigurationSection parent, String key, String name, String type, Map<String, Integer> effects, int duration) {
        addExampleEvent(parent, key, name, type, effects, duration, false);
    }

    /**
     * Adds an example event with optional special effect handling.
     */
    private void addExampleEvent(ConfigurationSection parent, String key, String name, String type, Map<String, Integer> effects, int duration, boolean specialEffect) {
        if (!parent.contains(key)) {
            ConfigurationSection eventSection = parent.createSection(key);
            eventSection.set("name", name);
            eventSection.set("type", type);
            eventSection.set("duration", duration);

            if (!effects.isEmpty()) {
                ConfigurationSection effectsSection = eventSection.createSection("effects");
                effects.forEach(effectsSection::set);
            }

            if (specialEffect) {
                eventSection.set("special_effect", "SUNS_WRATH");
            }
        }
    }

    /**
     * Adds an example item to the airdrop configuration.
     */
    private void addExampleItem(ConfigurationSection section, String item, int min, int max, int chance) {
        addExampleItem(section, item, min, max, chance, null, null);
    }

    /**
     * Adds an example item with optional name and enchantments.
     */
    private void addExampleItem(ConfigurationSection section, String item, int min, int max, int chance, String name, Map<String, Integer> enchants) {
        ConfigurationSection itemSection = section.createSection(item);
        itemSection.set("amount-min", min);
        itemSection.set("amount-max", max);
        itemSection.set("chance-of-inclusion", chance);

        if (name != null) {
            itemSection.set("name", name);
        }

        if (enchants != null && !enchants.isEmpty()) {
            List<String> enchantList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                enchantList.add(entry.getKey() + ": " + entry.getValue());
            }
            itemSection.set("enchants", enchantList);
        }
    }

    /**
     * Saves the configuration file.
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Gets the loaded configuration.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Gets the chat format for a specified rank.
     */
    public String getRankFormat(String rank) {
        return rankFormats.getOrDefault(rank.toLowerCase(), "&7<player>: &f<message>");
    }
}


