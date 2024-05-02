document.addEventListener('DOMContentLoaded', function() {
    const chatInput = document.getElementById('chat-input');
    const sendButton = document.getElementById('send-btn');
    const chatBox = document.getElementById('chat-box');
    let webSocket;

    // Get the player's name from the query parameter in the URL
    const urlParams = new URLSearchParams(window.location.search);
    const playerName = urlParams.get('name');

    // Establish WebSocket connection
    webSocket = new WebSocket('ws://localhost:8080/websocket');

    // WebSocket event listeners
    webSocket.onopen = function(event) {
        console.log('WebSocket connection opened.');
    };

    webSocket.onmessage = function(event) {
        // Handle incoming messages from the server
        const message = JSON.parse(event.data);
        
        if (message.type === 'chat') {
            // Display chat messages in the chat box
            displayChatMessage(message);
        } else if (message.type === 'start') {
            // Start of a new game
            handleGameStart(message);
        } else if (message.type === 'turn') {
            // Player's turn to guess
            handlePlayerTurn(message);
        } else if (message.type === 'result') {
            // Result of a guess
            handleGuessResult(message);
        }
    };

    webSocket.onerror = function(error) {
        // Handle WebSocket errors
        console.error('WebSocket error:', error);
    };

    webSocket.onclose = function(event) {
        // Handle WebSocket closure
        console.log('WebSocket connection closed.');
    };

    // Event listener for sending messages
    sendButton.addEventListener('click', function() {
        const messageText = chatInput.value.trim();
        if (messageText === '') {
            alert('Please enter a message.');
            return;
        }

        // Send chat message to the server via WebSocket
        webSocket.send(JSON.stringify({ type: 'chat', name: playerName, text: messageText }));

        // Clear input field
        chatInput.value = '';
    });

    // Function to display chat messages
    function displayChatMessage(message) {
        const messageElement = document.createElement('div');
        messageElement.textContent = `${message.name}: ${message.text}`;
        chatBox.appendChild(messageElement);
    }

    // Function to handle game start message
    function handleGameStart(message) {
        // Display the word for the player to guess
        const wordToGuess = message.word;
        displayChatMessage({ name: 'Game', text: `Word to guess: ${wordToGuess}` });
    }

    // Function to handle player's turn message
    function handlePlayerTurn(message) {
        // Display whose turn it is to guess
        const currentPlayer = message.player;
        displayChatMessage({ name: 'Game', text: `${currentPlayer}'s turn to guess.` });
    }

    // Function to handle guess result message
    function handleGuessResult(message) {
        // Display the result of the guess
        const guessResult = message.result;
        displayChatMessage({ name: 'Game', text: guessResult });
    }
});
