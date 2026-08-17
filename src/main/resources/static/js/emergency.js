document.addEventListener('DOMContentLoaded', () => {
    const recoveryForm = document.getElementById('recoveryQuestionForm');

    if (recoveryForm) {
        recoveryForm.addEventListener('submit', handleRecoveryQuestionSubmit);
    }
});

/**
 * Handles security challenge setup for vault owners
 * @param {Event} event 
 */
async function handleRecoveryQuestionSubmit(event) {
    event.preventDefault();

    const questionInput = document.getElementById('recoveryQuestion');
    const answerInput = document.getElementById('recoveryAnswer');
    const statusBox = document.getElementById('setupStatus');

    const question = questionInput ? questionInput.value.trim() : '';
    const answer = answerInput ? answerInput.value.trim() : '';

    if (!question || !answer) {
        displayStatus(statusBox, 'error', 'Both question and answer are required.');
        return;
    }

    const token = localStorage.getItem('jwtToken');
    if (!token) {
        alert('Session expired. Please log in again.');
        window.location.href = '/login.html';
        return;
    }

    try {
        const response = await fetch('/api/emergency/recovery-question', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ question, answer })
        });

        // Safely parse JSON or handle empty response bodies
        const text = await response.text();
        let data = {};
        try {
            data = text ? JSON.parse(text) : {};
        } catch (e) {
            data = { message: text };
        }

        if (response.ok) {
            displayStatus(statusBox, 'success', data.message || 'Protocol Saved!');
            if (answerInput) answerInput.value = '';
        } else {
            throw new Error(data.error || data.message || 'Failed to save protocol');
        }
    } catch (err) {
        displayStatus(statusBox, 'error', err.message);
    }
}

/**
 * Renders status alerts dynamically
 */
function displayStatus(element, type, message) {
    if (!element) {
        alert(message);
        return;
    }
    element.style.display = 'block';
    element.className = `status-msg status-${type}`;
    element.textContent = message;
}
