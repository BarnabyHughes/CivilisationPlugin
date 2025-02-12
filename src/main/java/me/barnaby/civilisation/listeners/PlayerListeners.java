package me.barnaby.civilisation.listeners;

import me.barnaby.civilisation.Civilisation;
import me.barnaby.civilisation.chat.ChatManager;
import me.barnaby.civilisation.chat.ChatType;
import me.barnaby.civilisation.civilisation.CivilisationManager;
import me.barnaby.civilisation.config.ConfigManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handles player-related events, including chat formatting and civilisation handling.
 */
public class PlayerListeners implements Listener {
    private final ConfigManager configManager;
    private final LuckPerms luckPerms;
    private final CivilisationManager civilisationManager;

    public PlayerListeners(Civilisation civilisation, LuckPerms luckPerms) {
        this.configManager = civilisation.getConfigManager();
        this.luckPerms = luckPerms;
        this.civilisationManager = civilisation.getCivilisationManager();
    }

    /**
     * Handles player chat events, applying chat formatting and filters based on chat type.
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return; // Should not happen

        ChatType chatType = ChatManager.getChatChannel(player);
        String primaryGroup = user.getPrimaryGroup();
        String chatFormat = configManager.getRankFormat(primaryGroup)
                .replace("<player>", player.getName())
                .replace("<message>", event.getMessage());

        event.setCancelled(true);

        switch (chatType) {
            case GLOBAL -> sendChatMessage("chat.global", chatFormat);
            case LOCAL -> sendLocalMessage(player, chatFormat);
            case STAFF -> sendStaffMessage(player, chatFormat);
            case CIVILISATION -> sendCivilisationMessage(player, chatFormat);
        }
    }

    /**
     * Handles player join events and teleports them to their civilisation's spawn point if applicable.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Only teleport on first join (adjust as needed)
        if (!player.hasPlayedBefore()) {
            civilisationManager.teleportToCivilisationSpawn(player);
        }
    }

    /**
     * Sends a chat message globally.
     */
    private void sendChatMessage(String messageKey, String message) {
        Bukkit.broadcastMessage(configManager.getMessage(messageKey, message));
    }

    /**
     * Sends a local chat message to players within a configured distance of the sender.
     */
    private void sendLocalMessage(Player player, String message) {
        int localChatDistance = configManager.getConfig().getInt("chat.local_distance", 100);
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.getWorld().equals(player.getWorld()) &&
                    onlinePlayer.getLocation().distance(player.getLocation()) <= localChatDistance) {
                onlinePlayer.sendMessage(configManager.getMessage("chat.local", message));
            }
        });
    }

    /**
     * Sends a staff chat message to all players with the appropriate permission.
     */
    private void sendStaffMessage(Player player, String message) {
        if (!player.hasPermission("civilisation.staff")) {
            player.sendMessage(configManager.getMessage("chat.staff_no_permission"));
            return;
        }
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.hasPermission("civilisation.staff")) {
                onlinePlayer.sendMessage(configManager.getMessage("chat.staff", message));
            }
        });
    }

    /**
     * Sends a civilisation chat message to all players in the same civilisation.
     */
    private void sendCivilisationMessage(Player player, String message) {
        String civ = civilisationManager.getPlayerCivilisation(player);
        if (civ == null) {
            player.sendMessage(configManager.getMessage("chat.civilisation_no_membership"));
            return;
        }
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (civ.equals(civilisationManager.getPlayerCivilisation(onlinePlayer))) {
                onlinePlayer.sendMessage(configManager.getMessage("chat.civilisation", message));
            }
        });
    }
}
