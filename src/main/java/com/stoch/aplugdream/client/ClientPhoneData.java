package com.stoch.aplugdream.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side storage for phone messages.
 * Messages are received via network packets from the server
 * and displayed in the SmartphoneScreen GUI.
 */
public class ClientPhoneData {
    private static final List<String> messages = new ArrayList<>();
    private static final int MAX_MESSAGES = 50;
    private static int unreadCount = 0;

    public static void addMessage(String message) {
        messages.add(0, message); // newest first
        if (messages.size() > MAX_MESSAGES) {
            messages.remove(messages.size() - 1);
        }
        unreadCount++;
    }

    public static List<String> getMessages() {
        return messages;
    }

    public static int getUnreadCount() {
        return unreadCount;
    }

    public static void markAllRead() {
        unreadCount = 0;
    }

    public static void clearMessages() {
        messages.clear();
        unreadCount = 0;
    }
}
