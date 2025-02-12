package me.barnaby.civilisation.commands;

import me.barnaby.civilisation.Civilisation;
import me.barnaby.civilisation.config.ConfigManager;
import me.barnaby.civilisation.util.ChatUtils;
import me.barnaby.civilisation.util.StaffUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Handles the /report command, allowing players to report others or the server.
 */
public class ReportCommand implements CommandExecutor {
    private final ConfigManager configManager;

    public ReportCommand(Civilisation civilisation) {
        this.configManager = civilisation.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the sender is a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("general.not_a_player"));
            return true;
        }

        // Ensure proper command usage
        if (args.length < 2) {
            player.sendMessage(configManager.getMessage("general.invalid_usage", "/report <player/server> <reason>"));
            return true;
        }

        String targetName = args[0]; // Target name ("server" or player)
        String reason = Arrays.stream(args, 1, args.length).collect(Collectors.joining(" "));

        // Determine if this is a server report
        boolean isServerReport = targetName.equalsIgnoreCase("server");

        // Validate player target if not reporting the server
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (!isServerReport && targetPlayer == null) {
            player.sendMessage(configManager.getMessage("general.player_not_found", targetName));
            return true;
        }

        // Format the report message
        String formattedMessage = isServerReport
                ? configManager.getMessage("report.server", player.getName(), reason)
                : configManager.getMessage("report.player", player.getName(), targetName, reason);

        // Notify all online staff
        StaffUtil.getOnlineStaff().forEach(staff -> {
            staff.sendMessage("");
            staff.sendMessage(formattedMessage);

            // Add clickable teleport message if reporting a player
            if (!isServerReport) {
                ChatUtils.sendClickableMessage(staff,
                        configManager.getMessage("report.teleport_click"),
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/teleport " + targetPlayer.getName()),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(configManager.getMessage("report.teleport_hover"))));
            }

            staff.sendMessage("");
        });

        // Notify the reporter
        player.sendMessage(configManager.getMessage("report.success"));

        return true;
    }
}

