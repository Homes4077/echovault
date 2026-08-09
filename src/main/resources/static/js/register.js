document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('register-form');
    const alertBox = document.getElementById('alert-box');
    const submitBtn = document.getElementById('btn-register');

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const fullName = document.getElementById('fullName').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();

        showAlert('', 'none');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Creating Account...';

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ fullName, email, password })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Registration failed.');
            }

            showAlert('Account created successfully! Redirecting to login...', 'success');
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 1500);

        } catch (error) {
            showAlert(error.message, 'error');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Create Vault Account';
        }
    });

    function showAlert(message, type) {
        if (type === 'none') {
            alertBox.style.display = 'none';
            return;
        }
        alertBox.textContent = message;
        alertBox.className = `alert alert-${type}`;
        alertBox.style.display = 'block';
    }
});
