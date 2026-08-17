document.addEventListener('DOMContentLoaded', () => {
    const chatForm = document.getElementById('chatForm');
    const chatInput = document.getElementById('chatInput');
    const chatContainer = document.getElementById('chatMessages');

    if (chatForm) {
        chatForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const messageText = chatInput.value.trim();

            if (!messageText) return;

            // Render user message bubble
            appendMessage('user', messageText);
            chatInput.value = '';

            // Render typing indicator
            const loadingBubble = appendMessage('ghost', 'Echoing memory...');

            try {
                const reply = await sendGhostMessage(messageText);
                loadingBubble.textContent = reply;
            } catch (err) {
                loadingBubble.textContent = `Unable to connect: ${err.message}`;
                loadingBubble.classList.add('error-message');
            }
        });
    }
});

/**
 * Sends prompt to Ghost AI endpoint with JWT Authorization
 * @param {string} messageText 
 * @returns {Promise<string>} AI response text
 */
async function sendGhostMessage(messageText) {
    const token = localStorage.getItem('jwtToken');
    
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    const response = await fetch('/api/ghost-chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ message: messageText })
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `Error (${response.status}): Failed response`);
    }

    const data = await response.json();
    return data.reply;
}

/**
 * Helper to append chat messages to the UI container
 */
function appendMessage(sender, text) {
    const chatContainer = document.getElementById('chatMessages');
    if (!chatContainer) return null;

    const msgDiv = document.createElement('div');
    msgDiv.className = `chat-message ${sender}-message`;
    msgDiv.textContent = text;

    chatContainer.appendChild(msgDiv);
    chatContainer.scrollTop = chatContainer.scrollHeight;

    return msgDiv;
}
