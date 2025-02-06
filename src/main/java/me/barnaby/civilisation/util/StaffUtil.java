package me.barnaby.civilisation.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StaffUtil {

    public static List<Player> getOnlineStaff() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.removeIf(player -> !player.hasPermission("civilisation.notified"));
        return players;
    }

}
