package me.barnaby.civilisation.commands;

import me.barnaby.civilisation.Civilisation;
import me.barnaby.civilisation.config.ConfigManager;
import me.barnaby.civilisation.util.ChatUtils;
import me.barnaby.civilisation.util.StaffUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the /notify command, allowing players to notify staff.
 */
public class NotifyCommand implements CommandExecutor {
    private final ConfigManager configManager;

    public NotifyCommand(Civilisation civilisation) {
        this.configManager = civilisation.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("general.not_a_player"));
            return true;
        }

        // Determine the notification message
        String notificationMessage = (args.length == 0)
                ? configManager.getMessage("notify.default_message")
                : String.join(" ", args);

        String formattedMessage = configManager.getMessage("notify.alert", player.getName(), notificationMessage);

        // Notify all online staff
        StaffUtil.getOnlineStaff().forEach(staff -> {
            staff.sendMessage("");
            staff.sendMessage(formattedMessage);
            ChatUtils.sendClickableMessage(staff,
                    configManager.getMessage("notify.teleport_click"),
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/teleport " + player.getName()),
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(configManager.getMessage("notify.teleport_hover"))));
            staff.sendMessage("");
        });

        // Notify the sender
        player.sendMessage(configManager.getMessage("notify.success", notificationMessage));

        return true;
    }
}
