/**
 * Voice Note Recording & Upload Manager
 * EchoVault Application
 */

let mediaRecorder = null;
let audioChunks = [];
let recordedAudioBlob = null;
let isRecording = false;
let speechRecognition = null;

/**
 * Initialize Web Speech API for real-time transcript generation if supported by browser.
 */
function initSpeechRecognition() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
        console.warn('[EchoVault] Web Speech API is not supported in this browser.');
        return null;
    }

    const recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.lang = 'en-US';

    recognition.onresult = (event) => {
        const transcriptElement = document.getElementById('transcript') || document.querySelector('textarea');
        if (!transcriptElement) return;

        let fullTranscript = '';
        for (let i = 0; i < event.results.length; i++) {
            fullTranscript += event.results[i][0].transcript;
        }
        transcriptElement.value = fullTranscript;
    };

    recognition.onerror = (event) => {
        console.warn('[EchoVault] Speech Recognition notice:', event.error);
    };

    return recognition;
}

/**
 * Request microphone access, start MediaRecorder, and trigger Speech Recognition.
 */
async function startRecording() {
    try {
        // Reset recording data
        audioChunks = [];
        recordedAudioBlob = null;

        // Obtain microphone stream from browser
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

        // Determine supported audio container format (WebM, MP4, or OGG)
        let mimeType = 'audio/webm';
        if (!MediaRecorder.isTypeSupported('audio/webm')) {
            if (MediaRecorder.isTypeSupported('audio/mp4')) {
                mimeType = 'audio/mp4';
            } else if (MediaRecorder.isTypeSupported('audio/ogg')) {
                mimeType = 'audio/ogg';
            }
        }

        mediaRecorder = new MediaRecorder(stream, { mimeType });

        // Store incoming audio data chunks
        mediaRecorder.ondataavailable = (event) => {
            if (event.data && event.data.size > 0) {
                audioChunks.push(event.data);
            }
        };

        mediaRecorder.onstop = async () => {
            recordedAudioBlob = new Blob(audioChunks, { type: mediaRecorder.mimeType || 'audio/webm' });

            // Render preview player if element exists
            const audioPreview = document.getElementById('audioPreview') || document.querySelector('audio');
            if (audioPreview) {
                const audioUrl = URL.createObjectURL(recordedAudioBlob);
                audioPreview.src = audioUrl;
                audioPreview.style.display = 'block';
                audioPreview.load();
            }

            // Stop hardware microphone tracks to turn off recording indicator
            stream.getTracks().forEach(track => track.stop());

            updateStatusUI('Recording captured. Uploading to vault...', 'uploading');
            await uploadVoiceNote(recordedAudioBlob);
        };

        // Start collecting audio chunks every 200ms
        mediaRecorder.start(200);
        isRecording = true;

        // Start speech-to-text transcription engine if available
        if (!speechRecognition) {
            speechRecognition = initSpeechRecognition();
        }
        if (speechRecognition) {
            try { speechRecognition.start(); } catch (e) { /* engine already running */ }
        }

        updateRecordButtonUI(true);
        updateStatusUI('Recording in progress... Speak clearly.', 'recording');
        console.log(`[EchoVault] Recording started with format: ${mediaRecorder.mimeType}`);

    } catch (err) {
        console.error('[EchoVault] Microphone error:', err);
        updateStatusUI('Microphone access denied or unavailable.', 'error');
        alert('Could not access microphone. Please enable microphone permissions in your browser settings.');
    }
}

/**
 * Stop current audio recording session and speech recognition.
 */
function stopRecording() {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop();
        isRecording = false;

        if (speechRecognition) {
            try { speechRecognition.stop(); } catch (e) { /* recognition already stopped */ }
        }

        updateRecordButtonUI(false);
        console.log('[EchoVault] Recording stopped by user.');
    }
}

/**
 * Send recorded audio blob and metadata to Spring Boot REST endpoint.
 * @param {Blob} existingBlob - Audio Blob to upload
 */
async function uploadVoiceNote(existingBlob = null) {
    const blobToUpload = existingBlob || recordedAudioBlob;

    if (!blobToUpload) {
        updateStatusUI('No audio recording found to upload.', 'error');
        return;
    }

    // Retrieve authentication token from storage
    const token = localStorage.getItem('jwtToken') || localStorage.getItem('token');
    if (!token) {
        updateStatusUI('Session expired. Redirecting to login...', 'error');
        alert('You must be logged in to save voice notes.');
        window.location.href = '/login.html';
        return;
    }

    // DOM inputs matching backend controller DTO fields
    const titleElement = document.getElementById('title') || document.querySelector('input[type="text"]');
    const tagElement = document.getElementById('tag') || document.getElementById('contextTag') || document.querySelector('select');
    const transcriptElement = document.getElementById('transcript') || document.querySelector('textarea');

    const titleValue = titleElement ? titleElement.value.trim() : '';
    // Ensure tag matches standard database Enum values (e.g. MOTIVATION instead of MOTIVATIONAL)
    let tagValue = tagElement && tagElement.value ? tagElement.value.trim().toUpperCase() : 'MOTIVATION';
    if (tagValue === 'MOTIVATIONAL') {
        tagValue = 'MOTIVATION'; // Remap to valid PostgreSQL Enum constraint value
    }
    const transcriptValue = transcriptElement ? transcriptElement.value.trim() : '';

    if (!titleValue) {
        updateStatusUI('Please enter a Title / Identifier before recording.', 'error');
        alert('Please fill out the Title field before uploading your voice note.');
        return;
    }

    // Build FormData payload
    const formData = new FormData();
    const fileExt = blobToUpload.type.includes('mp4') ? 'mp4' : (blobToUpload.type.includes('ogg') ? 'ogg' : 'webm');

    formData.append('file', blobToUpload, `voicenote.${fileExt}`);
    formData.append('title', titleValue);
    formData.append('tag', tagValue);
    formData.append('contextTag', tagValue); // Fallback parameter name
    formData.append('transcript', transcriptValue);

    try {
        const response = await fetch('/api/voice-notes', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
                // Note: Do NOT manually set Content-Type header when using FormData.
            },
            body: formData
        });

        if (!response.ok) {
            const errText = await response.text();
            console.error(`[EchoVault] Voice Note upload failed (${response.status}):`, errText);

            if (response.status === 403 || response.status === 401) {
                updateStatusUI('Authentication error (403/401). Please log in again.', 'error');
                alert('Session expired or invalid token. Please log in again.');
            } else {
                updateStatusUI(`Upload failed (${response.status}): ${errText || 'Server Error'}`, 'error');
            }
            return;
        }

        const data = await response.json().catch(() => ({ message: 'Saved successfully' }));
        console.log('[EchoVault] Voice note saved successfully:', data);
        updateStatusUI('Voice memory saved successfully to your vault!', 'success');

    } catch (err) {
        console.error('[EchoVault] Network error during upload:', err);
        updateStatusUI('Network error. Unable to connect to server.', 'error');
    }
}

/**
 * Update recording button CSS classes and label text.
 * @param {boolean} active
 */
function updateRecordButtonUI(active) {
    const recordBtn = document.getElementById('recordBtn');
    const recordText = document.getElementById('recordText');
    const recordIcon = document.getElementById('recordIcon');

    if (!recordBtn) return;

    if (active) {
        recordBtn.classList.add('recording');
        if (recordText) recordText.textContent = 'Stop Recording';
        if (recordIcon) recordIcon.textContent = '⏹️';
    } else {
        recordBtn.classList.remove('recording');
        if (recordText) recordText.textContent = 'Record Again';
        if (recordIcon) recordIcon.textContent = '🔴';
    }
}

/**
 * Display status alert banner messages on screen.
 * @param {string} msg
 * @param {string} type - 'success', 'error', 'recording', 'uploading'
 */
function updateStatusUI(msg, type) {
    const statusAlert = document.getElementById('statusAlert') || document.getElementById('uploadStatus');
    if (statusAlert) {
        statusAlert.className = `status-msg status-${type === 'recording' || type === 'uploading' ? 'success' : type}`;
        statusAlert.textContent = msg;
        statusAlert.style.display = 'block';
    }
    console.log(`[EchoVault Status] [${type.toUpperCase()}] ${msg}`);
}

// Bind DOM event listeners once document is ready
document.addEventListener('DOMContentLoaded', () => {
    const recordBtn = document.getElementById('recordBtn');
    if (recordBtn) {
        recordBtn.addEventListener('click', () => {
            if (!isRecording) {
                startRecording();
            } else {
                stopRecording();
            }
        });
    }
});