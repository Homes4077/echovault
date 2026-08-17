document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const loginForm = document.getElementById('loginForm');

    // Handle User Registration
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const fullNameInput = document.getElementById('fullName') || document.getElementById('name');
            const emailInput = document.getElementById('email');
            const passwordInput = document.getElementById('password');

            const fullName = fullNameInput ? fullNameInput.value.trim() : '';
            const email = emailInput ? emailInput.value.trim() : '';
            const password = passwordInput ? passwordInput.value : '';

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ fullName, email, password })
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    alert(`Registration failed: ${errorText || response.statusText}`);
                    return;
                }

                const data = await response.json();
                if (data.token) {
                    localStorage.setItem('jwtToken', data.token);
                    if (data.role) localStorage.setItem('userRole', data.role);
                }

                alert('Registration successful! Redirecting to login...');
                window.location.href = '/login.html';
            } catch (err) {
                console.error('Registration Error:', err);
                alert('Network error during registration.');
            }
        });
    }

    // Handle User Login
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const emailInput = document.getElementById('email');
            const passwordInput = document.getElementById('password');

            const email = emailInput ? emailInput.value.trim() : '';
            const password = passwordInput ? passwordInput.value : '';

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    alert(`Login failed: ${errorText || 'Invalid credentials'}`);
                    return;
                }

                const data = await response.json();
                if (data.token) {
                    localStorage.setItem('jwtToken', data.token);
                    if (data.role) localStorage.setItem('userRole', data.role);
                    window.location.href = '/dashboard.html';
                } else {
                    alert('Login succeeded, but no authorization token was returned.');
                }
            } catch (err) {
                console.error('Login Error:', err);
                alert('Network error during login.');
            }
        });
    }
});

/**
 * Global Session Guard
 * Automatically redirects unauthenticated users away from protected pages.
 */
function checkAuthGuard() {
    const token = localStorage.getItem('jwtToken');
    const publicPages = ['/login.html', '/register.html', '/index.html', '/emergency.html', '/'];
    const currentPath = window.location.pathname;

    const isPublicPage = publicPages.some(page => currentPath.endsWith(page));

    if (!token && !isPublicPage) {
        window.location.href = '/login.html';
    }
}

/**
 * Utility: Get pre-formatted Authorization headers for fetch API calls
 */
function getAuthHeaders(isJson = true) {
    const token = localStorage.getItem('jwtToken');
    const headers = {};
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    if (isJson) {
        headers['Content-Type'] = 'application/json';
    }
    return headers;
}

/**
 * Utility: Clear session and redirect to login
 */
function logout() {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userRole');
    window.location.href = '/login.html';
}
