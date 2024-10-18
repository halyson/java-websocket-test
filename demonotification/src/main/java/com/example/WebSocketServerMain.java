package com.example;

import java.util.TimerTask;
import org.glassfish.tyrus.server.Server;
import java.util.Timer;


public class WebSocketServerMain {
    public static void main(String[] args) {
        // Enable detailed logging
        java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINE);
        for (java.util.logging.Handler handler : java.util.logging.Logger.getLogger("").getHandlers()) {
            handler.setLevel(java.util.logging.Level.FINE);
        }

        Server server = new Server("localhost", 8025, "/", null, WebSocketServer.class);

        try {
            server.start();
            System.out.println("WebSocket server started on ws://localhost:8025/websocket");


            // Set up a timer to send notifications every 5 seconds
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    broadcastToAllUsers("Server notification: " + System.currentTimeMillis());
                }
            }, 0, 5000);

            System.out.println("Press any key to stop the server...");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }

    public static void broadcastToAllUsers(String message) {
        for (String userId : WebSocketServer.getUsers().keySet()) {
            WebSocketServer.sendMessageToUser(userId, message);
        }
        System.out.println("Broadcasted message to all users: " + message);
    }
}
