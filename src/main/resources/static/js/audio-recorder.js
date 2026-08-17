let mediaRecorder;
let audioChunks = [];

async function startRecording() {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        mediaRecorder = new MediaRecorder(stream);
        audioChunks = [];

        mediaRecorder.ondataavailable = (event) => {
            if (event.data.size > 0) audioChunks.push(event.data);
        };

        mediaRecorder.onstop = async () => {
            const audioBlob = new Blob(audioChunks, { type: 'audio/mp3' });
            await uploadVoiceNote(audioBlob);
        };

        mediaRecorder.start();
        console.log('Recording started...');
    } catch (err) {
        alert('Microphone access denied or unsupported.');
    }
}

function stopRecording() {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop();
        console.log('Recording stopped...');
    }
}

async function uploadVoiceNote(blob) {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        alert('You must be logged in to upload voice notes.');
        return;
    }

    const formData = new FormData();
    formData.append('file', blob, 'voicenote.mp3');
    formData.append('title', document.getElementById('voiceTitle')?.value || 'Voice Note');

    try {
        const response = await fetch('/api/voice-notes/upload', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
                // Content-Type is left unset so the browser generates the boundary
            },
            body: formData
        });

        if (!response.ok) {
            const err = await response.text();
            alert(`Voice note upload failed: ${err}`);
            return;
        }

        alert('Voice note saved successfully!');
    } catch (err) {
        console.error('Upload Error:', err);
        alert('Failed to upload voice note.');
    }
}
