document.addEventListener('DOMContentLoaded', function() {
    const playerNameInput = document.getElementById('player-name');
    const startGameButton = document.getElementById('start-game-btn');

    startGameButton.addEventListener('click', function() {
        const playerName = playerNameInput.value.trim();
        if (playerName === '') {
            alert('Please enter your name.');
            return;
        }


        window.location.href = '/game-room.html?name=' + encodeURIComponent(playerName);
    });
});
