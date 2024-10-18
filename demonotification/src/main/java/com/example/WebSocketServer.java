package com.example;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket")
public class WebSocketServer {
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static Map<String, Session> users = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("New WebSocket connection: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (message.startsWith("REGISTER:")) {
            String[] parts = message.split(":", 3);
            if (parts.length == 3) {
                String userId = parts[1];
                String userName = parts[2];
                users.put(userId, session);
                System.out.println("Registered user: " + userName + " (ID: " + userId + ")");
            }
        } else if (message.startsWith("MESSAGE:")) {
            String[] parts = message.split(":", 3);
            if (parts.length == 3) {
                String targetUserId = parts[1];
                String content = parts[2];
                sendMessageToUser(targetUserId, content);
            }
        }
    }

    public static void sendMessageToUser(String userId, String message) {
        Session userSession = users.get(userId);
        if (userSession != null && userSession.isOpen()) {
            try {
                userSession.getBasicRemote().sendText(message + " - " + userId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("User not found or connection closed: " + userId);
        }
    }

    public static void broadcastMessage(String message) {
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Session> getUsers() {
        return users;
    }
}
