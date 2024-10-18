package com.example;

import javax.swing.*;
import javax.websocket.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ClientEndpoint
public class WebSocketClient extends JFrame {
    private JTextArea messageArea;
    private Session session;
    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
    private boolean reconnectScheduled = false;
    private String userId;
    private String userName;

    public WebSocketClient(String userId, String userName) {
        super("WebSocket Client - " + userName);
        this.userId = userId;
        this.userName = userName;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        connectToWebSocket();
    }

    private void connectToWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = "ws://localhost:8025/websocket";
            session = container.connectToServer(this, new URI(uri));
            messageArea.append("Connected to " + uri + "\n");
            registerUser();
            reconnectScheduled = false;
        } catch (Exception e) {
            e.printStackTrace();
            messageArea.append("Error connecting to WebSocket server: " + e.getMessage() + "\n");
            reconnectScheduled = false;
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (!reconnectScheduled) {
            reconnectScheduled = true;
            executorService.schedule(this::connectToWebSocket, 5, TimeUnit.SECONDS);
            messageArea.append("Scheduling reconnection in 5 seconds...\n");
        }
    }

    private void registerUser() {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText("REGISTER:" + userId + ":" + userName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        SwingUtilities.invokeLater(() -> messageArea.append("WebSocket connection opened\n"));
    }

    @OnMessage
    public void onMessage(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append("Message:" + message + "\n"));
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append("WebSocket connection closed: " + closeReason.getReasonPhrase() + "\n");
            scheduleReconnect();
        });
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append("WebSocket error: " + throwable.getMessage() + "\n");
            if (throwable.getCause() != null) {
                messageArea.append("Caused by: " + throwable.getCause().getMessage() + "\n");
            }
            scheduleReconnect();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WebSocketClient("user1", "Alice").setVisible(true);
            new WebSocketClient("user2", "Bob").setVisible(true);
        });
    }
}

