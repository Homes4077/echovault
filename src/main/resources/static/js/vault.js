document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('jwtToken');
    if (!token && !window.location.pathname.includes('login.html') && !window.location.pathname.includes('register.html')) {
        window.location.href = '/login.html';
        return;
    }

    // Load Vault Items
    if (document.getElementById('vaultContainer')) {
        loadVaultItems();
    }

    // Photo Upload Handler
    const photoForm = document.getElementById('photoUploadForm');
    if (photoForm) {
        photoForm.addEventListener('submit', uploadPhoto);
    }
});

async function loadVaultItems() {
    const token = localStorage.getItem('jwtToken');
    const authHeader = { 'Authorization': `Bearer ${token}` };

    try {
        // Fetch Voice Notes
        const voiceRes = await fetch('/api/voice-notes', { headers: authHeader });
        if (voiceRes.ok) {
            const voiceNotes = await voiceRes.json();
            renderVaultSection('voiceNotesList', voiceNotes);
        }

        // Fetch Scheduled Letters
        const letterRes = await fetch('/api/letters', { headers: authHeader });
        if (letterRes.ok) {
            const letters = await letterRes.json();
            renderVaultSection('lettersList', letters);
        }
    } catch (err) {
        console.error('Error loading vault contents:', err);
    }
}

async function uploadPhoto(e) {
    e.preventDefault();
    const token = localStorage.getItem('jwtToken');
    const fileInput = document.getElementById('photoInput');
    if (!fileInput || !fileInput.files[0]) return;

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    formData.append('caption', document.getElementById('caption')?.value || '');

    try {
        const response = await fetch('/api/photos/upload', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData
        });

        if (!response.ok) {
            const err = await response.text();
            alert(`Upload failed: ${err}`);
            return;
        }

        alert('Photo uploaded successfully!');
        window.location.reload();
    } catch (err) {
        console.error('Photo Upload Error:', err);
    }
}

function renderVaultSection(containerId, items) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!items || items.length === 0) {
        container.innerHTML = '<p class="empty-msg">No items found.</p>';
        return;
    }

    container.innerHTML = items.map(item => `
        <div class="vault-card">
            <h4>${item.title || item.name || 'Untitled'}</h4>
            <p>${item.description || item.content || ''}</p>
        </div>
    `).join('');
}
