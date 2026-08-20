document.addEventListener('DOMContentLoaded', () => {
    checkAuthGuard();
    applyFamilyRestrictions();
    ensureGhostNavExists();

    const registerForm = document.getElementById('registerForm');
    const loginForm = document.getElementById('loginForm');

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
                    if (data.id || data.userId) localStorage.setItem('userId', data.id || data.userId);
                }

                alert('Registration successful! Redirecting to login...');
                window.location.href = '/login.html';
            } catch (err) {
                console.error('Registration Error:', err);
                alert('Network error during registration.');
            }
        });
    }

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const emailInput = document.getElementById('email');
            const passwordInput = document.getElementById('password');
            const viewModeInput = document.getElementById('viewMode');

            const email = emailInput ? emailInput.value.trim() : '';
            const password = passwordInput ? passwordInput.value : '';
            const viewMode = viewModeInput ? viewModeInput.value : 'USER';

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password, viewMode })
                });

                if (!response.ok) {
                    let errorMessage = 'Invalid credentials';
                    try {
                        const errData = await response.json();
                        errorMessage = errData.error || errData.message || errorMessage;
                    } catch (_) {
                        errorMessage = await response.text() || errorMessage;
                    }
                    alert(`Login failed: ${errorMessage}`);
                    return;
                }

                const data = await response.json();
                const token = data.token || data.jwtToken;

                if (token) {
                    localStorage.setItem('jwtToken', token);
                    if (data.role) localStorage.setItem('userRole', data.role);
                    if (data.id || data.userId) localStorage.setItem('userId', data.id || data.userId);

                    window.location.href = data.redirectUrl || '/dashboard.html';
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
 * Dynamically injects Ghost Chat link into navbar if missing
 */
function ensureGhostNavExists() {
    const navContainer = document.querySelector('nav .flex.gap-6') || document.getElementById('navLinks');
    if (navContainer && !navContainer.querySelector('a[href*="ghost"]')) {
        const ghostLink = document.createElement('a');
        ghostLink.href = '/ghost-chat.html';
        ghostLink.className = 'text-amber-400 hover:text-amber-300 transition flex items-center gap-1 font-medium text-sm';
        ghostLink.innerHTML = '👻 Ghost AI';
        navContainer.appendChild(ghostLink);
    }
}

/**
 * Emergency Read-Only Guard for Family Members
 */
function applyFamilyRestrictions() {
    const userRole = localStorage.getItem('userRole');

    if (userRole === 'ROLE_FAMILY' || userRole === 'ROLE_FAMILY_MEMBER' || userRole === 'FAMILY') {
        const currentPath = window.location.pathname;

        const restrictedPages = ['write-letter.html', 'settings.html', 'admin.html'];
        if (restrictedPages.some(page => currentPath.includes(page))) {
            window.location.href = '/memorial.html';
            return;
        }

        const elementsToHide = [
            '.edit-btn',
            '.delete-btn',
            '.compose-link',
            'a[href*="write-letter"]',
            '#writeLetterNav',
            '#composeBtn',
            '#deleteModal',
            '[data-action="edit"]',
            '[data-action="delete"]'
        ];

        elementsToHide.forEach(selector => {
            document.querySelectorAll(selector).forEach(el => {
                el.style.display = 'none';
            });
        });

        if (!document.getElementById('family-access-banner')) {
            const banner = document.createElement('div');
            banner.id = 'family-access-banner';
            banner.className = 'bg-amber-500/10 border-b border-amber-500/30 text-amber-400 p-2 text-center text-xs font-semibold uppercase tracking-wider sticky top-0 z-50 backdrop-blur-md flex justify-center items-center gap-4';
            banner.innerHTML = `
                <span>🔒 Emergency Read-Only Family Access Active</span>
                <a href="/ghost-chat.html" style="color: #fbbf24; text-decoration: underline; font-weight: bold;">Talk to Ghost Persona →</a>
            `;
            document.body.prepend(banner);
        }
    }
}

/**
 * Global Session Guard & Admin Access Verification
 */
function checkAuthGuard() {
    const token = localStorage.getItem('jwtToken');
    const userRole = localStorage.getItem('userRole');
    const currentPath = window.location.pathname;

    const publicPages = [
        '/login.html', 
        '/register.html', 
        '/index.html', 
        '/emergency-unlock.html', 
        '/memorial.html', 
        '/ghost-chat.html',
        '/'
    ];

    const isPublicPage = publicPages.some(page => currentPath.endsWith(page));

    // Redirect unauthenticated users
    if (!token && !isPublicPage) {
        window.location.href = '/login.html';
        return;
    }

    // Protect Admin paths from non-admin logged-in users
    if (currentPath.includes('/admin/') && !['ROLE_ADMIN', 'ADMIN'].includes(userRole)) {
        alert('Access denied: System Administrator privileges required.');
        window.location.href = '/dashboard.html';
    }
}

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

function logout() {
    localStorage.clear();
    window.location.href = '/login.html';
}
