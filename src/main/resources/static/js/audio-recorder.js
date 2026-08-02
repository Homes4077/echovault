/**
 * EchoVault Browser Audio Recording Engine
 * Captures user voice via HTML5 MediaRecorder API and posts binary data to Spring Boot
 */

let mediaRecorder = null;
let audioChunks = [];

/**
 * Initiates microphone audio capture
 */
async function startRecording() {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        mediaRecorder = new MediaRecorder(stream);
        audioChunks = [];

        mediaRecorder.ondataavailable = (event) => {
            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        };

        mediaRecorder.onstop = handleRecordingUpload;
        mediaRecorder.start();

        updateRecordingUI(true);
    } catch (err) {
        console.error("Microphone access denied or unrecorded:", err);
        alert("Microphone permission is required to record voice notes.");
    }
}

/**
 * Stops current audio stream session
 */
function stopRecording() {
    if (mediaRecorder && mediaRecorder.state !== "inactive") {
        mediaRecorder.stop();
        // Stop all active tracks on the stream to release the mic
        mediaRecorder.stream.getTracks().forEach(track => track.stop());
        updateRecordingUI(false);
    }
}

/**
 * Packages recorded audio binary into FormData and sends to backend upload endpoint
 */
function handleRecordingUpload() {
    const statusEl = document.getElementById("status");
    if (statusEl) statusEl.innerText = "Processing audio & requesting transcription...";

    const audioBlob = new Blob(audioChunks, { type: "audio/wav" });
    const formData = new FormData();

    const userId = document.getElementById("userId") ? document.getElementById("userId").value : "1";
    const title = document.getElementById("title") ? document.getElementById("title").value : "Untitled Memory";
    const tag = document.getElementById("tag") ? document.getElementById("tag").value : "STORY";

    formData.append("userId", userId);
    formData.append("title", title);
    formData.append("tag", tag);
    formData.append("file", audioBlob, "voice-note.wav");

    fetch("/vault/voice/upload", {
        method: "POST",
        body: formData
    })
    .then(response => {
        if (!response.ok) throw new Error("Upload failed.");
        return response.text();
    })
    .then(result => {
        if (statusEl) statusEl.innerText = "Voice note saved and transcribed successfully!";
    })
    .catch(error => {
        console.error("Error saving voice note:", error);
        if (statusEl) statusEl.innerText = "Failed to upload and transcribe audio.";
    });
}

function updateRecordingUI(isRecording) {
    const startBtn = document.getElementById("startBtn");
    const stopBtn = document.getElementById("stopBtn");
    const statusEl = document.getElementById("status");

    if (isRecording) {
        if (startBtn) startBtn.style.display = "none";
        if (stopBtn) stopBtn.style.display = "inline-block";
        if (statusEl) statusEl.innerText = "🔴 Recording in progress...";
    } else {
        if (startBtn) startBtn.style.display = "inline-block";
        if (stopBtn) stopBtn.style.display = "none";
    }
}
