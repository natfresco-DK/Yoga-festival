const form = document.querySelector("#registration-form");
const festivalSelect = document.querySelector("#festivalId");
const activityOptions = document.querySelector("#activity-options");
const message = document.querySelector("#registration-message");

let activities = [];

function showMessage(text, isError = false) {
    message.textContent = text;
    message.classList.toggle("error", isError);
    message.hidden = false;
}

async function loadFestivals() {
    const response = await fetch("/api/festivals");
    if (!response.ok) {
        throw new Error("Could not load festivals");
    }

    const festivals = await response.json();
    for (const festival of festivals) {
        const option = document.createElement("option");
        option.value = festival.id;
        option.textContent = `${festival.name} (${festival.year})`;
        festivalSelect.append(option);
    }
}

async function loadActivities() {
    const festivalId = festivalSelect.value;
    if (!festivalId) {
        activities = [];
        activityOptions.replaceChildren();
        activityOptions.append("Vælg en festival for at se aktiviteter.");
        return;
    }

    const response = await fetch("/api/activities");
    if (!response.ok) {
        throw new Error("Could not load activities");
    }

    const allActivities = await response.json();
    activities = allActivities.filter(
        activity => String(activity.festivalId) === String(festivalId)
    );
    renderActivityOptions();
}

function renderActivityOptions() {
    activityOptions.replaceChildren();

    if (activities.length === 0) {
        activityOptions.append("Ingen aktiviteter fundet for denne festival.");
        return;
    }

    for (const activity of activities) {
        const label = document.createElement("label");
        label.className = "activity-option";

        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.name = "activity";
        checkbox.value = activity.id;

        const text = document.createElement("span");
        text.textContent = `${activity.name} – ${activity.room ?? "Lokale ikke angivet"}`;

        label.append(checkbox, text);
        activityOptions.append(label);
    }
}

festivalSelect.addEventListener("change", () => {
    loadActivities().catch(error => showMessage(error.message, true));
});

form.addEventListener("submit", async event => {
    event.preventDefault();

    const activityIds = [...document.querySelectorAll('input[name="activity"]:checked')]
        .map(input => Number(input.value));

    if (activityIds.length === 0) {
        showMessage("Vælg mindst én aktivitet.", true);
        return;
    }

    const participantPayload = {
        name: document.querySelector("#name").value.trim(),
        email: document.querySelector("#email").value.trim(),
        phoneNumber: document.querySelector("#phoneNumber").value.trim() || null,
        festivalId: Number(festivalSelect.value)
    };

    try {
        const participantResponse = await fetch("/api/participants/registrations", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(participantPayload)
        });

        if (!participantResponse.ok) {
            const error = await participantResponse.json().catch(() => null);
            throw new Error(error?.message ?? "Participant could not be registered");
        }

        const responseText = await participantResponse.text();
        let participantRegistration;

        try {
            participantRegistration = JSON.parse(responseText);
        } catch {
            throw new Error("The server did not return valid JSON");
        }
        const activityResponse = await fetch("/api/activity-registrations", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                festivalRegistrationId: participantRegistration.id,
                activityIds
            })
        });

        if (!activityResponse.ok) {
            throw new Error("Activity registrations could not be saved");
        }

        showMessage("Deltageren er registreret og tilmeldt valgte aktiviteter.");
        form.reset();
        activityOptions.replaceChildren("Vælg en festival for at se aktiviteter.");
    } catch (error) {
        showMessage(error.message, true);
    }
});

loadFestivals().catch(error => showMessage(error.message, true));
