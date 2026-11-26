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

function getActivityName(activityId) {
    return activities.find(activity => String(activity.id) === activityId)?.name
        ?? "den valgte aktivitet";
}

function getUserErrorMessage(errorResponse, fallback) {
    const backendMessage = errorResponse?.message;

    if (errorResponse?.errors?.length) {
        const validationMessages = errorResponse.errors.map(error => {
            if (error === "Name is required") return "Indtast deltagerens navn.";
            if (error === "Email is required") return "Indtast en e-mailadresse.";
            if (error === "Email must be valid") return "Indtast en gyldig e-mailadresse.";
            if (error === "Festival ID is required") return "Vælg en festival.";
            if (error === "At least one activity must be selected") {
                return "Vælg mindst én aktivitet.";
            }
            return null;
        }).filter(Boolean);

        if (validationMessages.length) {
            return validationMessages.join(" ");
        }
    }

    if (backendMessage === "Invalid input") {
        return "Kontrollér oplysningerne, og prøv igen.";
    }

    let match = backendMessage?.match(/^Activity (\d+) is full$/);
    if (match) {
        return `${getActivityName(match[1])} er desværre fuld. Vælg en anden aktivitet.`;
    }

    match = backendMessage?.match(/^Participant is already registered for activity (\d+)$/);
    if (match) {
        return `Deltageren er allerede tilmeldt ${getActivityName(match[1])}.`;
    }

    if (backendMessage === "Duplicate activity IDs are not allowed") {
        return "Den samme aktivitet er valgt flere gange. Opdatér valget, og prøv igen.";
    }
    if (backendMessage === "At least one activity must be selected") {
        return "Vælg mindst én aktivitet.";
    }

    return fallback;
}

async function readErrorResponse(response, fallback) {
    try {
        return getUserErrorMessage(await response.json(), fallback);
    } catch {
        return fallback;
    }
}

async function loadFestivals() {
    const response = await fetch("/api/festivals");
    if (!response.ok) {
        throw new Error("Festivalerne kunne ikke indlæses. Prøv igen senere.");
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
        throw new Error("Aktiviteterne kunne ikke indlæses. Prøv igen senere.");
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
        text.className = "activity-option-details";

        const details = document.createElement("small");
        details.textContent = `${formatDateTime(activity.startTime)} – ${formatDateTime(activity.endTime)} · ${activity.availablePlaces} ledige pladser`;

        text.append(name, details);

        label.append(checkbox, text);
        activityOptions.append(label);
    }
}

function formatDateTime(dateTime) {
    return new Intl.DateTimeFormat("da-DK", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(dateTime));
}

festivalSelect.addEventListener("change", () => {
    loadActivities().catch(error => {
        console.error("Could not load activities:", error);
        showMessage(error.message || "Aktiviteterne kunne ikke indlæses.", true);
    });
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
            showMessage(await readErrorResponse(
                participantResponse,
                "Deltageren kunne ikke registreres. Kontrollér oplysningerne, og prøv igen."
            ), true);
            return;
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
            showMessage(await readErrorResponse(
                activityResponse,
                "Aktivitetstilmeldingen kunne ikke gennemføres. Prøv igen."
            ), true);
            return;
        }

        const selectedFestivalId = festivalSelect.value;
        form.reset();
        festivalSelect.value = selectedFestivalId;
        try {
            await loadActivities();
            showMessage("Deltageren er registreret og tilmeldt valgte aktiviteter.");
        } catch (error) {
            console.error("Could not refresh activity availability:", error);
            showMessage(
                "Tilmeldingen blev gennemført, men ledige pladser kunne ikke opdateres. Genindlæs aktiviteterne for at se den aktuelle status.",
                true
            );
        }
    } catch (error) {
        console.error("Could not complete registration:", error);
        showMessage("Der opstod en teknisk fejl. Kontrollér forbindelsen, og prøv igen.", true);
    }
});

loadFestivals().catch(error => {
    console.error("Could not load festivals:", error);
    showMessage(error.message || "Festivalerne kunne ikke indlæses.", true);
});
