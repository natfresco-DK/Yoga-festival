// Enkel frontend-prototype. Ingen server eller database er tilkoblet.
const STORAGE_KEY = "yogafestival_demo_activities";
const form = document.querySelector("#activity-form");
const list = document.querySelector("#activity-list");
const count = document.querySelector("#activity-count");
const message = document.querySelector("#form-message");

let activities = [];

async function loadActivities() {
    try {
        const response = await fetch("/api/activities");

        if (!response.ok) {
            throw new Error(`Server returned ${response.status}`);
        }

        const loadedActivities = await response.json();

        activities = loadedActivities.map(activity => ({
            name: activity.name ?? activity.name ?? "",
            description: activity.description ?? "",
            instructor: activity.instructor ?? "",
            room: activity.room ?? "",
            itemsToBring: activity.itemsToBring ?? "",
            start: activity.startTime ?? activity.start ?? "",
            end: activity.endTime ?? activity.end ?? "",
            capacity: activity.capacity ?? 0
        }));

        renderActivities();
    } catch (error) {
        console.error("Could not load activities:", error);
        showMessage("Aktiviteterne kunne ikke indlæses.", true);
        renderActivities();
    }
}

function formatDate(dateString) {
    return new Intl.DateTimeFormat("da-DK", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(dateString));
}

function addTextElement(parent, tag, text, className = "") {
    const element = document.createElement(tag);
    if (className) element.className = className;
    element.textContent = text;
    parent.append(element);
    return element;
}

function renderActivities() {
    count.textContent = String(activities.length);
    list.replaceChildren();

    if (activities.length === 0) {
        const empty = addTextElement(list, "div", "", "empty-state");
        addTextElement(empty, "p", "Ingen aktiviteter endnu");
        addTextElement(empty, "small", "Opret din første aktivitet i formularen.");
        return;
    }

    for (const activity of activities) {
        const card = addTextElement(list, "article", "", "activity-card");
        addTextElement(card, "h3", activity.name);
        addTextElement(card,"p", `Beskrivelse: ${activity.description}`);
        addTextElement(card, "p", `Underviser: ${activity.instructor}`);
        addTextElement(card, "p", `Lokale: ${activity.room}`);
        addTextElement(card, "p", `medbring: ${activity.itemsToBring}`);
        addTextElement(card, "p", `${formatDate(activity.start)} – ${formatDate(activity.end)}`);
        addTextElement(card, "span", `${activity.capacity} pladser`, "capacity");
    }
}

function showMessage(text, isError = false) {
    message.textContent = text;
    message.classList.toggle("error", isError);
    message.hidden = false;
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const data = new FormData(form);

    const activity = {
        name: data.get("title").trim(),
        description: data.get("description").trim(),
        instructor: data.get("instructor").trim(),
        room: data.get("room").trim(),
        itemsToBring: data.get("itemsToBring").trim(),
        startTime: data.get("start"),
        endTime: data.get("end"),
        capacity: Number(data.get("capacity")),
        festivalId: 1
    };

    const response = await fetch("/api/activities", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(activity)
    });

    if (!response.ok) {
        showMessage("Aktiviteten kunne ikke gemmes.", true);
        return;
    }

    showMessage("Aktiviteten er gemt.");
    form.reset();
    await loadActivities();
});
loadActivities();
