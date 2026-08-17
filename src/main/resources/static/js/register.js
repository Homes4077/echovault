document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const alertBox = document.getElementById('alert-box');

    if (!registerForm) return;

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        showAlert('', 'none');

        const submitBtn = registerForm.querySelector('button[type="submit"]');
        const fullName = document.getElementById('fullName').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const requestAdmin = document.getElementById('requestAdmin')?.checked;

        if (password.length < 6) {
            showAlert('Registration failed: Password must be at least 6 characters.', 'error');
            return;
        }

        const payload = {
            fullName: fullName,
            email: email,
            password: password,
            role: requestAdmin ? 'ROLE_ADMIN' : 'ROLE_USER'
        };

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerText = 'Creating Account...';
        }

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            let responseData = {};
            const contentType = response.headers.get('content-type');
            
            if (contentType && contentType.includes('application/json')) {
                responseData = await response.json();
            } else {
                const rawText = await response.text();
                if (rawText) responseData = { message: rawText };
            }

            if (!response.ok) {
                const errorMsg = responseData.error || responseData.message || 'Invalid details provided.';
                showAlert(`Registration failed: ${errorMsg}`, 'error');
                return;
            }

            // Ensure clean storage state so user must log in explicitly
            localStorage.removeItem('jwtToken');

            showAlert('Account created successfully! Redirecting to login...', 'success');
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 1200);

        } catch (err) {
            console.error('Registration error:', err);
            showAlert('Network error occurred. Please check your server connection.', 'error');
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerText = 'Create Vault Account';
            }
        }
    });

    function showAlert(message, type) {
        if (!alertBox) return;
        alertBox.className = `alert alert-${type}`;
        alertBox.innerText = message;
        alertBox.style.display = type === 'none' ? 'none' : 'block';
    }
});
