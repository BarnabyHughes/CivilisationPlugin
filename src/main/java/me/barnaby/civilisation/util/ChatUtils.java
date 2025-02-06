package me.barnaby.civilisation.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ChatUtils {

    public static void sendClickableMessage(Player player, String message, ClickEvent clickEvent, HoverEvent hoverEvent) {
        TextComponent textComponent = new net.md_5.bungee.api.chat.TextComponent(
                ChatColor.translateAlternateColorCodes('&', message));
        textComponent.setClickEvent(clickEvent);
        textComponent.setHoverEvent(hoverEvent);
        player.spigot().sendMessage(textComponent);
    }

    public static String format(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
