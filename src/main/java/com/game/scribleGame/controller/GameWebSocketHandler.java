package com.game.scribleGame.controller;

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


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Add the new session to the sessions map
        if(player1Session==null)player1Session= session;
        if(player2Session==null && !session.equals(player1Session))player2Session =session;
        sessions.put(session.getId(), session);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the closed session from the sessions map and playerNames map
        sessions.remove(session.getId());
        playerNames.remove(session);
        // Reset game if either player leaves
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

   private void handleChooseMessage(WebSocketSession senderSession, Map<String, String> messageMap) throws IOException{
       String playerName =   messageMap.get("name");
       String chosenWord  = messageMap.get("word");
       System.out.println(playerName+chosenWord);
       if (senderSession == player1Session) {
           System.out.println("Player1 have choose the word "+ chosenWord);
           sendMessageToAllPlayers("Player 1 have choosen the word");
           handleChooseWordByPlayer1(senderSession, playerName, chosenWord);
       } else if (senderSession == player2Session) {
           System.out.println("Player1 have choose the word "+ chosenWord);
           sendMessageToAllPlayers("Player 2 have choosen the word");
           handleChooseWordByPlayer2(senderSession, playerName, chosenWord);
       }
   }

    private void handleChooseWordByPlayer1(WebSocketSession senderSession, String playerName, String chosenWord) throws IOException {
        // Validate the chosen word (optional)

        // Store the chosen word for player 1
        // Inform player 2 that player 1 has chosen a word
        wordChoseByPlayer1 = chosenWord;
        String message = playerName + " has chosen a word. Now it's Player 2's turn to guess.";
        sendMessageToAllPlayers(message);

    }

    private void handleChooseWordByPlayer2(WebSocketSession senderSession, String playerName, String chosenWord) throws IOException {
        // Validate the chosen word (optional)
        // Store the chosen word for player 2
        // Inform both players that the game has started
        wordChoseByPlayer2 = chosenWord;
        String message = playerName + " has chosen a word. Now it's Player 1s turn to guess!";
        sendMessageToAllPlayers(message);

    }

    private void handleGuessMessage(WebSocketSession senderSession, Map<String, String> messageMap) throws IOException {
        String playerName = playerNames.get(senderSession);
        String guessedWord = messageMap.get("word");
        if (senderSession == player1Session) {
            handleGuessByPlayer1(senderSession, playerName, guessedWord);
        } else if (senderSession == player2Session) {
            handleGuessByPlayer2(senderSession, playerName, guessedWord);
        }
    }

    private void handleGuessByPlayer1(WebSocketSession senderSession, String playerName, String guessedWord) throws IOException {
        if (guessedWord.equalsIgnoreCase(wordChoseByPlayer2)) {
            // Player 1 guessed correctly
            String message = playerName + " guessed the word '" + wordChoseByPlayer2 + "' correctly!";
            sendMessageToAllPlayers(message);
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
        } else {
            // Player 2 guessed incorrectly
            String message = playerName + " guessed the word '" + guessedWord + "' incorrectly.";
            sendMessageToAllPlayers(message);
        }
    }

    private void handleChatMessage(WebSocketSession senderSession, Map<String, String> messageMap) throws IOException {
        // Broadcast the chat message to all sessions
        for (WebSocketSession clientSession : sessions.values()) {
            clientSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageMap)));
        }
    }

    private void resetGame() throws IOException {
        // Reset all game-related variables
        player1Session = null;
        player2Session = null;
        wordChoseByPlayer1 = null ;
        wordChoseByPlayer2 = null;
       // wordToGuess = null;
       //gameStarted = false;
      //  counter = 0;
    }

    private void sendMessageToAllPlayers(String message) throws IOException {
        for (WebSocketSession session : sessions.values()) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
