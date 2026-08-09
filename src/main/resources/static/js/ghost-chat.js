// Utility function to strip Gemini citation and grounding span tags
function sanitizeAiMessage(text) {
    if (!text) return '';
    return text
        .replace(/\[span_\d+\]\((?:start|end)_span\)/g, '')
        .replace(/\[span_\d+\]/g, '')
        .replace(/\s+/g, ' ')
        .trim();
}

document.addEventListener('DOMContentLoaded', () => {
    const chatForm = document.getElementById('chat-form');
    const chatInput = document.getElementById('chat-input');
    const chatContainer = document.getElementById('chat-container');

    if (!chatForm || !chatInput || !chatContainer) {
        console.warn('Chat elements not found in DOM.');
        return;
    }

    chatForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const query = chatInput.value.trim();
        if (!query) return;

        // 1. Display User Message
        appendMessage('user', query);
        chatInput.value = '';

        // 2. Display Loading Indicator
        const loadingBubble = appendMessage('ghost', 'Ghost is thinking...');
        loadingBubble.style.fontStyle = 'italic';
        loadingBubble.style.opacity = '0.7';

        try {
            // 3. Send API Request to Spring Boot Backend
            const token = localStorage.getItem('token');
            const response = await fetch('/api/ghost/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
                },
                body: JSON.stringify({ prompt: query })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            
            // Extract reply payload
            const rawReply = data.reply || data.response || data;
            
            // 4. Sanitize text to remove span tags
            const cleanReply = sanitizeAiMessage(
                typeof rawReply === 'string' ? rawReply : JSON.stringify(rawReply)
            );

            // 5. Render Clean Reply
            loadingBubble.textContent = cleanReply;
            loadingBubble.style.fontStyle = 'normal';
            loadingBubble.style.opacity = '1';

        } catch (error) {
            console.error('Ghost Engine Error:', error);
            loadingBubble.textContent = 'Error connecting to Ghost Assistant Engine.';
            loadingBubble.style.color = '#ef4444';
        }

        // Auto scroll chat to bottom
        chatContainer.scrollTop = chatContainer.scrollHeight;
    });

    // Helper to append message bubbles to the container
    function appendMessage(sender, text) {
        const msgBubble = document.createElement('div');
        msgBubble.classList.add('message-bubble', `${sender}-bubble`);
        msgBubble.textContent = text;
        chatContainer.appendChild(msgBubble);
        chatContainer.scrollTop = chatContainer.scrollHeight;
        return msgBubble;
    }
});
