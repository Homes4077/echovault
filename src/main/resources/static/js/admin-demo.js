/**
 * EchoVault Admin Demo Presentation Controllers
 * Triggers API events immediately to bypass time/date wait periods during project evaluations
 */

function triggerDemoDeliveries() {
    const outputEl = document.getElementById("demoOutput");
    if (outputEl) outputEl.innerText = "Executing time-locked email checks...";

    fetch("/admin/demo/trigger-deliveries", {
        method: "POST"
    })
    .then(response => response.text())
    .then(message => {
        if (outputEl) outputEl.innerText = message;
    })
    .catch(error => {
        console.error("Demo delivery trigger error:", error);
        if (outputEl) outputEl.innerText = "Failed to trigger SendGrid delivery pipeline.";
    });
}

function triggerDemoInactivity() {
    const outputEl = document.getElementById("demoOutput");
    if (outputEl) outputEl.innerText = "Executing account inactivity checks...";

    fetch("/admin/demo/trigger-inactivity", {
        method: "POST"
    })
    .then(response => response.text())
    .then(message => {
        if (outputEl) outputEl.innerText = message;
    })
    .catch(error => {
        console.error("Demo inactivity trigger error:", error);
        if (outputEl) outputEl.innerText = "Failed to trigger Twilio inactivity alerts.";
    });
}
