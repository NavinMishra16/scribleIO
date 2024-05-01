package com.game.scribleGame.controller;

import java.awt.desktop.SystemEventListener;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Add the new session to the sessions map
        sessions.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the closed session from the sessions map
        sessions.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession senderSession, TextMessage message) throws Exception {
        // Handle incoming messages from clients
        String payload = message.getPayload();
        System.out.println("Received message from client: " + payload);
        sendMessageToSender(senderSession,message);
    }
    private void sendMessageToSender(WebSocketSession senderSession, TextMessage message) throws IOException {
         for(WebSocketSession session:  sessions.values()){
              session.sendMessage(message);
         }
    }
}
