/**
 * EchoVault Ghost Engine Interrogation Interface
 * Asynchronously posts user query and renders reconstructed response grounded in DB entries
 */

function sendQuery() {
    const input = document.getElementById("userInput");
    const chatBox = document.getElementById("chatBox");

    if (!input || !chatBox) return;

    const queryText = input.value.trim();
    if (!queryText) return;

    // Render User Message Bubble
    appendMessage(queryText, "msg-user");
    input.value = "";

    // Render Loading Indicator
    const loadingDiv = appendMessage("Consulting preserved memories...", "msg-ghost");

    const vaultOwnerId = document.getElementById("vaultOwnerId") ? document.getElementById("vaultOwnerId").value : 1;
    const queriedById = document.getElementById("queriedById") ? document.getElementById("queriedById").value : 1;

    fetch("/ghost/query", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            vaultOwnerId: parseInt(vaultOwnerId),
            queriedById: parseInt(queriedById),
            queryText: queryText
        })
    })
    .then(response => {
        if (!response.ok) throw new Error("Failed to consult Ghost Engine.");
        return response.json();
    })
    .then(data => {
        // Remove loading state
        loadingDiv.remove();

        // Render Gemini Reconstructed Answer
        const ghostMsgEl = appendMessage(data.responseText, "msg-ghost");

        // Attach Source Attributions if available
        if (data.sourcesUsed) {
            const sourceSpan = document.createElement("span");
            sourceSpan.className = "source-tag";
            sourceSpan.style.cssText = "display: block; font-size: 0.75rem; color: #38bdf8; margin-top: 6px;";
            sourceSpan.innerText = "Ground Truth Sources: " + data.sourcesUsed;
            ghostMsgEl.appendChild(sourceSpan);
        }
    })
    .catch(error => {
        console.error("Ghost Engine error:", error);
        loadingDiv.innerText = "Unable to process query at this time.";
    });
}

function appendMessage(text, className) {
    const chatBox = document.getElementById("chatBox");
    const msgDiv = document.createElement("div");
    msgDiv.className = `msg ${className}`;
    msgDiv.innerText = text;
    chatBox.appendChild(msgDiv);
    chatBox.scrollTop = chatBox.scrollHeight;
    return msgDiv;
}

// Allow Enter key press to submit query
document.addEventListener("DOMContentLoaded", () => {
    const input = document.getElementById("userInput");
    if (input) {
        input.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                sendQuery();
            }
        });
    }
});
