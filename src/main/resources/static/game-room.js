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
        displayMessage(message);
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

        // Send message to the server via WebSocket
        webSocket.send(JSON.stringify({ name: playerName, text: messageText }));

        // Clear input field
        chatInput.value = '';
    });

    // Function to display chat messages
    function displayMessage(message) {
        const messageElement = document.createElement('div');
        messageElement.textContent = `${message.name}: ${message.text}`;
        chatBox.appendChild(messageElement);
    }
});
