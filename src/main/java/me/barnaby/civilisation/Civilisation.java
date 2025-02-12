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
    private AirdropManager airdropManager;
    private EventManager eventManager;

    /**
     * Called when the plugin is enabled. Initializes all managers, commands, and events.
     */
    @Override
    public void onEnable() {
        configManager = new ConfigManager(this, LuckPermsProvider.get());

        // Reinitialize other managers that depend on the configuration
        civilisationManager = new CivilisationManager(this, configManager);
        airdropManager = new AirdropManager(this);
        eventManager = new EventManager(this);
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(
                new PlayerListeners(this, LuckPermsProvider.get()), this
        );

        // Register commands
        registerCommands();
    }

    public void reloadCivilisationsConfig() {
        System.out.println("Reloading configuration and components...");

        reloadConfig();
        configManager.loadConfig();

    }


    /**
     * Registers all plugin commands.
     */
    private void registerCommands() {
        getCommand("notify").setExecutor(new NotifyCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("chat").setExecutor(new ChatCommand(this));
        getCommand("civilisation").setExecutor(new CivilisationCommand(this));



        getCommand("airdrop").setExecutor(new AirdropCommand(this));
        getCommand("event").setExecutor(new EventCommand(this));
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

    public AirdropManager getAirdropManager() {
        return airdropManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }
}

