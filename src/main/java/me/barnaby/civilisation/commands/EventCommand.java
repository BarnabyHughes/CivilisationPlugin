package me.barnaby.civilisation.commands;

import me.barnaby.civilisation.config.ConfigManager;
import me.barnaby.civilisation.event.EventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles the /event command, allowing staff to trigger events for civilisations.
 */
public class EventCommand implements CommandExecutor {
    private final EventManager eventManager;
    private final ConfigManager configManager;

    public EventCommand(EventManager eventManager, ConfigManager configManager) {
        this.eventManager = eventManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("civilisation.event.create")) {
            sender.sendMessage(configManager.getMessage("general.no_permission"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(configManager.getMessage("general.invalid_usage", "/event <civilisation/all> <event>"));
            return true;
        }

        // Determine target civilisation (or all)
        String civ = args[0].equalsIgnoreCase("all") ? null : args[0];

        // Trigger the event
        eventManager.triggerEvent(civ, args[1]);

        // Send success message
        sender.sendMessage(configManager.getMessage("event.success", args[1], (civ == null ? "everyone" : civ)));
        return true;
    }
}
