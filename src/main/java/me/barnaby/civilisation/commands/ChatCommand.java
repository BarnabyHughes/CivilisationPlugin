package me.barnaby.civilisation.commands;

import me.barnaby.civilisation.Civilisation;
import me.barnaby.civilisation.chat.ChatManager;
import me.barnaby.civilisation.chat.ChatType;
import me.barnaby.civilisation.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the /chat command, allowing players to switch chat channels.
 */
public class ChatCommand implements CommandExecutor {
    private final Civilisation civilisation;
    private final ConfigManager configManager;

    public ChatCommand(Civilisation civilisation) {
        this.civilisation = civilisation;
        this.configManager = civilisation.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("general.not_a_player"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(configManager.getMessage("general.invalid_usage", "/chat <global/local/civilisation/staff>"));
            return true;
        }

        try {
            ChatType chatType = ChatType.valueOf(args[0].toUpperCase());

            // Ensure only staff can switch to staff chat
            if (chatType == ChatType.STAFF && !player.hasPermission("civilisation.staff")) {
                player.sendMessage(configManager.getMessage("chat.staff_no_permission"));
                return true;
            }

            // Ensure players are in a civilisation before using civilisation chat
            if (chatType == ChatType.CIVILISATION && civilisation.getCivilisationManager().getPlayerCivilisation(player) == null) {
                player.sendMessage(configManager.getMessage("chat.civilisation_no_membership"));
                return true;
            }

            ChatManager.setChatChannel(player, chatType);
            player.sendMessage(configManager.getMessage("chat.success", chatType.name()));

        } catch (IllegalArgumentException e) {
            player.sendMessage(configManager.getMessage("chat.invalid"));
        }

        return true;
    }
}

