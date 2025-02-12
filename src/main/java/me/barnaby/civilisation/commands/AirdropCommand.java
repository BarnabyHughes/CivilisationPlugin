package me.barnaby.civilisation.commands;

import me.barnaby.civilisation.Civilisation;
import me.barnaby.civilisation.airdrop.AirdropManager;
import me.barnaby.civilisation.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the /airdrop command, allowing staff to manually spawn an airdrop.
 */
public class AirdropCommand implements CommandExecutor {
    private final AirdropManager airdropManager;
    private final ConfigManager configManager;

    public AirdropCommand(Civilisation civilisation) {
        this.airdropManager = civilisation.getAirdropManager();
        this.configManager = civilisation.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("general.not_a_player"));
            return true;
        }

        if (!player.hasPermission("civilisation.airdrop.create")) {
            player.sendMessage(configManager.getMessage("general.no_permission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(configManager.getMessage("general.invalid_usage", "/airdrop <type>"));
            return true;
        }

        String airdropType = args[0].toLowerCase();
        airdropManager.spawnAirdrop(airdropType);
        player.sendMessage(configManager.getMessage("airdrop.spawned", airdropType.toUpperCase()));

        return true;
    }
}

