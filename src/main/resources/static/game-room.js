document.addEventListener('DOMContentLoaded', function() {
    const chatInput = document.getElementById('chat-input');
    const sendButton = document.getElementById('send-btn');
    const chooseWordButton = document.getElementById('choose-word-btn');
    const guessWordButton = document.getElementById('guess-word-btn');
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
        // Check if the received message is valid JSON
        try {
            console.log(event.data);
            const message = JSON.parse(event.data);
            
            if (message.type === 'chat') {
                displayChatMessage(message);
            }  
            else if (message.type === 'choose_word') {
                handleChooseWord(message);
            } 
            else if (message.type === 'guess') {
                handleGuess(message);
            }
        } catch (error) {
            console.error('Error parsing incoming message:', error);
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
        webSocket.send(JSON.stringify({ type: 'chat', name: playerName, text: messageText }));
        chatInput.value = '';
    });

    // Event listener for choosing a word
    chooseWordButton.addEventListener('click', function() {
        // Implement logic to handle choosing a word
        // For example:
        const chosenWord = prompt('Enter the word you want to choose:');
        if (chosenWord !== null && chosenWord.trim() !== '') {
            webSocket.send(JSON.stringify({ type: 'choose_word', name: playerName, word: chosenWord }));
        }
    });

    // Event listener for guessing a word
    guessWordButton.addEventListener('click', function() {
        // Implement logic to handle guessing a word
        // For example:
        const guessedWord = prompt('Enter your guess:');
        if (guessedWord !== null && guessedWord.trim() !== '') {
            webSocket.send(JSON.stringify({ type: 'guess', name: playerName, word: guessedWord }));
        }
    });

    // Function to display chat messages
    function displayChatMessage(message) {
        const messageElement = document.createElement('div');
        messageElement.textContent = `${message.name}: ${message.text}`;
        chatBox.appendChild(messageElement);
    }

    // Function to handle choosing a word
    function handleChooseWord(message) {
        alert(`Received choose word message: ${message.word}`);
    }

    // Function to handle guessing a word
    function handleGuess(message) {
        // Implement logic to handle receiving guess message from the server
        // For example:
        alert(`Received guess message: ${message.result}`);
    }
});
