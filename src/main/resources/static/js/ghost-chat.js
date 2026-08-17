document.addEventListener('DOMContentLoaded', () => {
    const chatForm = document.getElementById('chatForm');
    const chatInput = document.getElementById('chatInput');
    const chatBox = document.getElementById('chatBox');

    if (!chatForm) return;

    chatForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const prompt = chatInput.value.trim();
        if (!prompt) return;

        appendMessage('User', prompt);
        chatInput.value = '';

        const token = localStorage.getItem('jwtToken');
        if (!token) {
            appendMessage('System', 'Authentication token missing. Please log in again.');
            return;
        }

        try {
            const response = await fetch('/api/ghost-chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ prompt })
            });

            if (!response.ok) {
                const errText = await response.text();
                appendMessage('System', `Error (${response.status}): ${errText || 'Failed to fetch response.'}`);
                return;
            }

            const data = await response.json();
            appendMessage('Ghost', data.response || data.message || 'No response body received.');
        } catch (err) {
            console.error('Ghost Chat Error:', err);
            appendMessage('System', 'Failed to communicate with the server.');
        }
    });

    function appendMessage(sender, text) {
        if (!chatBox) return;
        const msgDiv = document.createElement('div');
        msgDiv.className = `chat-message ${sender.toLowerCase()}`;
        msgDiv.innerHTML = `<strong>${sender}:</strong> ${escapeHtml(text)}`;
        chatBox.appendChild(msgDiv);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    function escapeHtml(str) {
        return str.replace(/[&<>"']/g, (m) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
    }
});
