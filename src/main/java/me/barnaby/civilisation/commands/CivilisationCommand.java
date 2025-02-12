package me.barnaby.civilisation.commands;

import me.barnaby.civilisation.Civilisation;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CivilisationCommand implements CommandExecutor {

    private final Civilisation civilisation;

    public CivilisationCommand(Civilisation civilisation) {
        this.civilisation = civilisation;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // No arguments provided - show plugin info and help.
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "---------------------------");
            sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Civilisation Plugin "
            + ChatColor.YELLOW + "Version: " + civilisation.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Developed by: Barnaby!");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "---------------------------");
            return true;
        }
        // One argument provided
        else if (args.length == 1) {
            // The reload subcommand
            if (args[0].equalsIgnoreCase("reload")) {
                // Check for the proper permission
                if (!sender.hasPermission("civilisation.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to reload this plugin.");
                    return true;
                }
                civilisation.reloadCivilisationsConfig();
                sender.sendMessage(ChatColor.GREEN + "Civilisation messages reloaded successfully!");
                sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Some changes still might require a restart or rejoin!");
                return true;
            }
            // If the argument isn't recognized, show an error message.
            else {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /" + label + " for help.");
                return false;
            }
        }
        // If too many arguments are provided, show a usage message.
        sender.sendMessage(ChatColor.RED + "Incorrect usage. Use /" + label + " for help.");
        return false;
    }
}
