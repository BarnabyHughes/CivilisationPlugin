package me.barnaby.civilisation.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StaffUtil {

    /**
     * Retrieves a list of online players who have the permission "civilisation.notified".
     *
     * @return a List of Player objects representing the online staff members who meet the permission criteria.
     */
    public static List<Player> getOnlineStaff() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.removeIf(player -> !player.hasPermission("civilisation.notified"));
        return players;
    }

}
