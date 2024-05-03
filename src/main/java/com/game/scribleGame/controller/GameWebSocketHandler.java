package com.game.scribleGame.controller;

import java.awt.desktop.SystemEventListener;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sun.source.doctree.SystemPropertyTree;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> playerNames = new ConcurrentHashMap<>();
   // private String wordToGuess;
    private WebSocketSession player1Session;
    private WebSocketSession player2Session;
    private String wordChoseByPlayer1 ;
    private String wordChoseByPlayer2 ;
    private int counter = 0 ;
    private final int gameTurns = 2 ;
    private int currentTurns = 0;
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Add the new session to the sessions map
        if(player1Session==null)player1Session= session;
        if(player2Session==null && !session.equals(player1Session))player2Session =session;
        sessions.put(session.getId(), session);
        startGame();

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the closed session from the sessions map and playerNames map
        sessions.remove(session.getId());
        playerNames.remove(session);
        // Reset game if either player leave
        resetGame();
    }

    @Override
    protected void handleTextMessage(WebSocketSession senderSession, TextMessage message) throws Exception {
        // Handle incoming messages from clients
        String payload = message.getPayload();
        System.out.println("Received message from client: " + payload);

        // Convert payload to messageMap
        Map<String, String> messageMap = objectMapper.readValue(payload, new TypeReference<Map<String, String>>() {});

        // Check if the payload contains the "type" field
        if (messageMap.containsKey("type")) {
            String messageType = messageMap.get("type");

            // Check the type of message
            switch (messageType) {
                case "chat":
                    handleChatMessage(senderSession, messageMap);
                    break;
                case "choose_word":
                    handleChooseMessage(senderSession, messageMap);
                    break;
                case "guess":
                    handleGuessMessage(senderSession,messageMap);
                default:
                    System.out.println("Unknown message type: " + messageType);
            }
        }
    }

    private void handleChooseMessage(WebSocketSession senderSession, Map<String, String> messageMap) throws IOException {
        String playerName = messageMap.get("name");
        String chosenWord = messageMap.get("word");
        playerNames.put(senderSession,playerName);
        System.out.println("messageMap "+ playerName);
        if (senderSession == player1Session && counter % 2 == 0) {
            String playerName1 = (String)senderSession.getAttributes().get("name");
            System.out.println("Player1 name : " + playerName1);
            // Player 1's turn to choose
            System.out.println("Player 1 have choose the word " + chosenWord);
            sendMessageToAllPlayers("Player1 turn to choose the Word");
            handleChooseWordByPlayer1(senderSession, playerName, chosenWord);
        } else if (senderSession == player2Session && counter % 2 == 1) {
            // Player 2's turn to choose
            System.out.println("Player 2 have choose the word " + chosenWord);
            sendMessageToAllPlayers("Player2 turn to choose the Word");
            handleChooseWordByPlayer2(senderSession, playerName, chosenWord);
        } else {
            // It's not their turn to choose
            String message = "It's not your turn to choose!";
            senderSession.sendMessage(new TextMessage(message));
        }
    }

    private void handleChooseWordByPlayer1(WebSocketSession senderSession, String playerName, String chosenWord) throws IOException {

        wordChoseByPlayer1 = chosenWord;
        String message = playerName +" has chosen a word. Now it's Player 2's turn to guess.";
        sendMessageToAllPlayers(message);

    }

    private void handleChooseWordByPlayer2(WebSocketSession senderSession, String playerName, String chosenWord) throws IOException {
        wordChoseByPlayer2 = chosenWord;
        String message =  playerName +" has chosen a word. Now it's Player1's turn to guess!";
        sendMessageToAllPlayers(message);
    }
    private void handleGuessMessage(WebSocketSession senderSession, Map<String, String> messageMap) throws IOException, InterruptedException {
        String playerName = playerNames.get(senderSession);
        String guessedWord = messageMap.get("word");

        if ((senderSession == player1Session && counter % 2 == 1) || (senderSession == player2Session && counter % 2 == 0)) {
            // Player 1's turn to guess (when counter is odd) or Player 2's turn to guess (when counter is even)
            if (senderSession == player1Session) {
                handleGuessByPlayer1(senderSession, playerName, guessedWord);
            } else if (senderSession == player2Session) {
                handleGuessByPlayer2(senderSession, playerName, guessedWord);
            }
            counter++;
            currentTurns++;
            isGameOver();
        }
        else {
            // It's not their turn to guess
            String message = "It's not your turn to guess!";
            senderSession.sendMessage(new TextMessage(message));
        }
    }

    private void handleGuessByPlayer1(WebSocketSession senderSession, String playerName, String guessedWord) throws IOException {
        if (guessedWord.equalsIgnoreCase(wordChoseByPlayer2)) {
            // Player 1 guessed correctly
            String message = playerName + " guessed the word '" + wordChoseByPlayer2 + "' correctly!";
            sendMessageToAllPlayers(message);
            String MessagePack = "Player1 turn to choose the Word";
            sendMessageToAllPlayers(MessagePack);
        } else {
            // Player 1 guessed incorrectly
            String message = playerName + " guessed the word '" + guessedWord + "' incorrectly.";
            sendMessageToAllPlayers(message);
        }
    }


    private void handleGuessByPlayer2(WebSocketSession senderSession, String playerName, String guessedWord) throws IOException {
        if (guessedWord.equalsIgnoreCase(wordChoseByPlayer1)) {
            // Player 2 guessed correctly
            String message = playerName + " guessed the word '" + wordChoseByPlayer1 + "' correctly!";
            sendMessageToAllPlayers(message);
            String MessagePack = "Player2 turn to choose the Word";
            sendMessageToAllPlayers(MessagePack);
        } else {
            String message = playerName + " guessed the word '" + guessedWord + "' incorrectly.";
            sendMessageToAllPlayers(message);
        }
    }

    private void handleChatMessage(WebSocketSession senderSession, Map<String, String> messageMap) throws IOException {

        for (WebSocketSession clientSession : sessions.values()) {
            clientSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageMap)));
        }
    }

    private void startGame() throws IOException {
        if (sessions.size() == 1) {
            System.out.println("Player1 joined");
            sendMessageToAllPlayers("Player 1 joined the game. Waiting for Player 2...");
        } else if (sessions.size() == 2) {
            System.out.println("Player2 joined");
            sendMessageToAllPlayers("Player 2 joined the game. Player 1, it's your turn to choose a word!");
        }
    }

    private void isGameOver() throws IOException, InterruptedException {
         if(currentTurns>gameTurns){
             String existMessage = "The Turns are exhausted Game Over";
             sendMessageToAllPlayers(existMessage);
             Thread.sleep(3000);
             resetGame();
         }
    }
    private void resetGame() throws IOException {
        // Reset all game-related variables
        player1Session = null;
        player2Session = null;
        wordChoseByPlayer1 = null ;
        wordChoseByPlayer2 = null;
        currentTurns = 0 ;
        counter = 0;
    }

    private void sendMessageToAllPlayers(String message) throws IOException {
        for (WebSocketSession session : sessions.values()) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
