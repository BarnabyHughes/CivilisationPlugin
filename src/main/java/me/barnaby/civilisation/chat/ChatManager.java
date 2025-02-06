package me.barnaby.civilisation.chat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ChatManager {
    private static final Map<Player, ChatType> playerChatChannels = new HashMap<>();

    public static ChatType getChatChannel(Player player) {
        return playerChatChannels.getOrDefault(player, ChatType.GLOBAL); // Default to GLOBAL
    }

    public static void setChatChannel(Player player, ChatType chatType) {
        playerChatChannels.put(player, chatType);
    }
}
