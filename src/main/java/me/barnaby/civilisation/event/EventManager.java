package me.barnaby.civilisation.event;

import me.barnaby.civilisation.Civilisation;
import me.barnaby.civilisation.civilisation.CivilisationManager;
import me.barnaby.civilisation.config.ConfigManager;
import me.barnaby.civilisation.util.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages random civilization events, applying effects and announcements.
 */
public class EventManager {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final CivilisationManager civilisationManager;
    private final Random random = new Random();

    public EventManager(Civilisation plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.civilisationManager = plugin.getCivilisationManager();
        startEventTimer();
    }

    /**
     * Starts the automatic event trigger, running every X ticks.
     */
    private void startEventTimer() {
        int interval = configManager.getConfig().getInt("event.interval", 7200); // Default: 2 hours
        new BukkitRunnable() {
            @Override
            public void run() {
                triggerRandomEvents();
            }
        }.runTaskTimer(plugin, interval * 20L, interval * 20L); // Convert seconds to ticks
    }

    /**
     * Triggers a random event for each civilization and a neutral event for all players.
     */
    private void triggerRandomEvents() {
        for (String civ : civilisationManager.getCivilisations()) {
            String event = getRandomEventForType("GOOD", "BAD");
            if (event != null) {
                triggerEvent(civ, event);
            }
        }

        String neutralEvent = getRandomEventForType("NEUTRAL");
        if (neutralEvent != null) {
            triggerEvent(null, neutralEvent);
        }
    }

    /**
     * Triggers a specific event for a civilization or all players.
     * @param civilisation The civilization affected (null for global events).
     * @param eventName The event to trigger.
     */
    public void triggerEvent(String civilisation, String eventName) {
        ConfigurationSection eventSection = configManager.getConfig().getConfigurationSection("events." + eventName);
        if (eventSection == null) return;

        String displayName = eventSection.getString("name", eventName);
        int duration = eventSection.getInt("duration", 300);

        // Get affected players
        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(p -> civilisation == null || civilisationManager.getPlayerCivilisation(p).equals(civilisation))
                .collect(Collectors.toList());

        // Apply potion effects
        applyEffects(eventSection, players, duration);

        // Special handling for Sun's Wrath
        if ("SUNS_WRATH".equals(eventSection.getString("special_effect", ""))) {
            applySunsWrath(players);
        }

        // Announce event
        Bukkit.broadcastMessage(configManager.getMessage("event.start", displayName, (civilisation == null ? "everyone" : civilisation)));
    }

    /**
     * Applies potion effects to players from the event configuration.
     */
    private void applyEffects(ConfigurationSection eventSection, List<Player> players, int duration) {
        if (eventSection.contains("effects")) {
            ConfigurationSection effects = eventSection.getConfigurationSection("effects");
            for (String effectKey : effects.getKeys(false)) {
                PotionEffectType effectType = PotionEffectType.getByName(effectKey.toUpperCase());
                if (effectType != null) {
                    int amplifier = effects.getInt(effectKey);
                    for (Player player : players) {
                        player.addPotionEffect(new PotionEffect(effectType, duration * 20, amplifier - 1));
                    }
                }
            }
        }
    }

    /**
     * Applies Sun’s Wrath effect, dealing damage unless players are in shade or water.
     */
    private void applySunsWrath(List<Player> players) {
        new BukkitRunnable() {
            int counter = 10; // 5 minutes (10 intervals of 30s)

            @Override
            public void run() {
                if (counter-- <= 0) {
                    cancel();
                    return;
                }

                for (Player player : players) {
                    if (player.getLocation().getBlock().getLightFromSky() > 0) {
                        player.damage(1.0);
                    }
                }
            }
        }.runTaskTimer(plugin, 600, 600);
    }

    /**
     * Gets a random event from the available event types.
     * @param types The types of events to choose from (GOOD, BAD, NEUTRAL).
     * @return A random event name or null if none are available.
     */
    private String getRandomEventForType(String... types) {
        List<String> validEvents = new ArrayList<>();
        ConfigurationSection eventsSection = configManager.getConfig().getConfigurationSection("events");
        if (eventsSection == null) return null;

        for (String key : eventsSection.getKeys(false)) {
            String eventType = eventsSection.getString(key + ".type", "");
            if (Arrays.asList(types).contains(eventType)) {
                validEvents.add(key);
            }
        }

        return validEvents.isEmpty() ? null : validEvents.get(random.nextInt(validEvents.size()));
    }
}
