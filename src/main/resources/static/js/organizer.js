const activityList = document.querySelector("#activity-list");
const participantList = document.querySelector("#participant-list");
const selectedActivityLabel = document.querySelector("#selected-activity-label");
const message = document.querySelector("#organizer-message");

const refreshIntervalMs = 15000;
let activities = [];
let selectedActivityId = null;
let participantRequestId = 0;
let refreshInProgress = false;

function showError(text) {
    message.textContent = text;
    message.hidden = false;
}

function clearError() {
    message.textContent = "";
    message.hidden = true;
}

function formatDateTime(dateTime) {
    return new Intl.DateTimeFormat("da-DK", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(dateTime));
}

async function fetchJson(url, errorMessage) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(errorMessage);
    }
    return response.json();
}

function renderActivities() {
    activityList.replaceChildren();

    if (activities.length === 0) {
        activityList.append("Der er ingen aktiviteter at vise.");
        return;
    }

    for (const activity of activities) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "organizer-activity";
        button.classList.toggle("selected", String(activity.id) === selectedActivityId);
        button.setAttribute("aria-pressed", String(String(activity.id) === selectedActivityId));

        const name = document.createElement("strong");
        name.textContent = activity.name;

        const time = document.createElement("span");
        time.textContent = `${formatDateTime(activity.startTime)} – ${formatDateTime(activity.endTime)}`;

        const capacity = document.createElement("small");
        capacity.textContent =
            `Kapacitet: ${activity.capacity} · Tilmeldte: ${activity.confirmedRegistrations} · Ledige: ${activity.availablePlaces}`;

        button.append(name, time, capacity);
        button.addEventListener("click", () => selectActivity(activity.id));
        activityList.append(button);
    }
}

async function loadParticipants(activityId, showLoading = false) {
    const requestId = ++participantRequestId;
    if (showLoading) {
        participantList.replaceChildren("Indlæser deltagere …");
    }

    const participants = await fetchJson(
        `/api/activities/${encodeURIComponent(activityId)}/participants`,
        "Deltagerlisten kunne ikke indlæses."
    );

    if (requestId !== participantRequestId || String(activityId) !== selectedActivityId) {
        return;
    }

    participantList.replaceChildren();
    if (participants.length === 0) {
        participantList.append("Ingen bekræftede deltagere er tilmeldt denne aktivitet.");
        return;
    }

    for (const participant of participants) {
        const card = document.createElement("article");
        card.className = "participant-card";

        const name = document.createElement("h3");
        name.textContent = participant.name;

        const email = document.createElement("p");
        email.textContent = participant.email;
        card.append(name, email);

        if (participant.phoneNumber) {
            const phone = document.createElement("p");
            phone.textContent = `Telefon: ${participant.phoneNumber}`;
            card.append(phone);
        }

        participantList.append(card);
    }
}

async function selectActivity(activityId) {
    selectedActivityId = String(activityId);
    const activity = activities.find(item => String(item.id) === selectedActivityId);
    selectedActivityLabel.textContent = activity
        ? `${activity.name} · ${formatDateTime(activity.startTime)} – ${formatDateTime(activity.endTime)}`
        : "Valgt aktivitet";
    renderActivities();

    try {
        clearError();
        await loadParticipants(activityId, true);
    } catch (error) {
        console.error("Could not load activity participants:", error);
        showError(error.message || "Deltagerlisten kunne ikke indlæses.");
    }
}

async function refreshOverview() {
    const previousSelection = selectedActivityId;
    activities = await fetchJson("/api/activities", "Aktiviteterne kunne ikke indlæses.");
    renderActivities();

    if (previousSelection && activities.some(item => String(item.id) === previousSelection)) {
        await loadParticipants(previousSelection);
    } else if (previousSelection) {
        selectedActivityId = null;
        selectedActivityLabel.textContent = "Vælg en aktivitet for at se dens deltagere.";
        participantList.replaceChildren("Den valgte aktivitet findes ikke længere.");
        renderActivities();
    }
}

async function refreshOrganizerPage() {
    if (refreshInProgress) {
        return;
    }

    refreshInProgress = true;
    try {
        await refreshOverview();
        clearError();
    } catch (error) {
        console.error("Could not refresh organizer overview:", error);
        showError(error.message || "Oversigten kunne ikke opdateres.");
    } finally {
        refreshInProgress = false;
    }
}

refreshOrganizerPage();
window.setInterval(refreshOrganizerPage, refreshIntervalMs);
