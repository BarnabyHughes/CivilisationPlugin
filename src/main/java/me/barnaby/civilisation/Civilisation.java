package me.barnaby.civilisation;

import me.barnaby.civilisation.airdrop.AirdropManager;
import me.barnaby.civilisation.civilisation.CivilisationManager;
import me.barnaby.civilisation.commands.*;
import me.barnaby.civilisation.config.ConfigManager;
import me.barnaby.civilisation.event.EventManager;
import me.barnaby.civilisation.listeners.PlayerListeners;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the Civilisation plugin.
 * Handles initialization of config, commands, event listeners, and managers.
 */
public class Civilisation extends JavaPlugin {

    private ConfigManager configManager;
    private CivilisationManager civilisationManager;

    /**
     * Called when the plugin is enabled. Initializes all managers, commands, and events.
     */
    @Override
    public void onEnable() {
        // Initialize Configuration Manager
        configManager = new ConfigManager(this, LuckPermsProvider.get());

        // Initialize Civilisation Manager
        civilisationManager = new CivilisationManager(this, configManager);

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(
                new PlayerListeners(configManager, LuckPermsProvider.get(), civilisationManager), this
        );

        // Register commands
        registerCommands();
    }

    /**
     * Registers all plugin commands.
     */
    private void registerCommands() {
        getCommand("notify").setExecutor(new NotifyCommand(this));
        getCommand("report").setExecutor(new ReportCommand(configManager));
        getCommand("chat").setExecutor(new ChatCommand(this));

        // Initialize and register Airdrop and Event commands with their respective managers
        AirdropManager airdropManager = new AirdropManager(this, configManager);
        EventManager eventManager = new EventManager(this, configManager, civilisationManager);

        getCommand("airdrop").setExecutor(new AirdropCommand(airdropManager, configManager));
        getCommand("event").setExecutor(new EventCommand(eventManager, configManager));
    }

    /**
     * Provides access to the Civilisation Manager.
     *
     * @return the instance of CivilisationManager
     */
    public CivilisationManager getCivilisationManager() {
        return civilisationManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}

