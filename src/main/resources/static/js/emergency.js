document.addEventListener('DOMContentLoaded', () => {
    // 1. Setup Security Challenge Form
    const recoveryForm = document.getElementById('recoveryQuestionForm');
    if (recoveryForm) {
        recoveryForm.addEventListener('submit', handleRecoveryQuestionSubmit);
    }

    // 2. Family Access Unlock Form
    const unlockForm = document.getElementById('emergencyUnlockForm') || document.getElementById('unlockForm');
    if (unlockForm) {
        unlockForm.addEventListener('submit', handleEmergencyUnlockSubmit);
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

    const token = localStorage.getItem('jwtToken') || localStorage.getItem('token');
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
 * Handles emergency family access unlock verification
 * @param {Event} event 
 */
async function handleEmergencyUnlockSubmit(event) {
    event.preventDefault();

    const emailInput = document.getElementById('ownerEmail') || document.getElementById('unlockEmail');
    const answerInput = document.getElementById('accessCode') || document.getElementById('unlockAnswer');
    const statusBox = document.getElementById('unlockStatus') || document.getElementById('setupStatus');

    const userEmail = emailInput ? emailInput.value.trim() : '';
    const accessCode = answerInput ? answerInput.value.trim() : '';

    if (!userEmail || !accessCode) {
        displayStatus(statusBox, 'error', 'Both owner email and answer are required.');
        return;
    }

    try {
        const response = await fetch('/api/emergency/unlock', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ userEmail, accessCode })
        });

        const text = await response.text();
        let data = {};
        try {
            data = text ? JSON.parse(text) : {};
        } catch (e) {
            data = { message: text };
        }

        if (response.ok) {
            displayStatus(statusBox, 'success', data.message || 'Emergency family access granted!');

            // Store the temporary family emergency token
            if (data.token) {
                localStorage.setItem('jwtToken', data.token);
                localStorage.setItem('userRole', data.role || 'ROLE_FAMILY_MEMBER');
            }

            // Redirect family member to dashboard after 1.5 seconds
            setTimeout(() => {
                window.location.href = '/dashboard.html';
            }, 1500);
        } else {
            throw new Error(data.error || data.message || 'Emergency unlock failed');
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
