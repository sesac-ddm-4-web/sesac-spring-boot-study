"use strict";

const createPanel = document.querySelector("#create-panel");
const lookupPanel = document.querySelector("#lookup-panel");
const original = document.querySelector("#original-url");
const keyInput = document.querySelector("#lookup-key");
const rawEditor = document.querySelector("#raw-editor");
const rawBody = document.querySelector("#raw-body");
const periods = document.querySelector(".periods");

function showPage() {
    const active = location.hash === "#lookup" ? "#lookup" : "#create";
    createPanel.hidden = active !== "#create";
    lookupPanel.hidden = active !== "#lookup";
    for (const link of document.querySelectorAll("nav a")) {
        if (link.hash === active) link.setAttribute("aria-current", "page");
        else link.removeAttribute("aria-current");
    }
}

function formBody() {
    const days = periods.querySelector(":checked").value;
    const body = { originalUrl: original.value };
    if (days !== "") body.expiresInDays = Number(days);
    return JSON.stringify(body, null, 2);
}

function updateDraft() {
    rawBody.value = formBody();
}

rawEditor.addEventListener("toggle", () => {
    original.disabled = rawEditor.open;
    periods.disabled = rawEditor.open;
});
original.addEventListener("input", updateDraft);
periods.addEventListener("change", updateDraft);
updateDraft();

function notice(panel, message, error = false) {
    const element = panel.querySelector(".notice");
    element.classList.toggle("error", error);
    element.textContent = message;
}

const expiryFormat = new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium", timeStyle: "long"
});

function displayLink(panel, data) {
    const card = panel.querySelector(".result");
    const link = new URL("/" + encodeURIComponent(data.shortenUrlKey), location.origin);
    card.querySelector(".open-link").href = link.href;
    const expiry = data.expiresAt ? expiryFormat.format(new Date(data.expiresAt)) : null;
    if (panel === createPanel) {
        card.querySelector(".short-address").textContent = link.host + link.pathname;
        card.querySelector(".expiry").textContent = expiry ? expiry + " 만료" : "만료 없음";
    } else {
        card.querySelector(".destination").textContent = data.originalUrl;
        card.querySelector(".expiry").textContent = expiry ?? "만료 없음";
        card.querySelector(".count").textContent = data.redirectCount + "회";
    }
    card.hidden = false;
}

async function sendRequest(panel, path, options) {
    const button = panel.querySelector('button[type="submit"]');
    if (button.disabled) return;
    button.disabled = true;
    panel.querySelector(".result").hidden = true;
    const trace = panel.querySelector(".trace");
    trace.querySelector(".trace-empty").hidden = true;
    trace.querySelector(".exchange").hidden = false;
    const requestLines = [options.method + " " + path];
    for (const [name, value] of Object.entries(options.headers ?? {})) {
        requestLines.push(name + ": " + value);
    }
    trace.querySelector(".request-meta").textContent = requestLines.join("\n");
    trace.querySelector(".request-body").textContent = options.body ?? "(본문 없음)";
    const responseMeta = trace.querySelector(".response-meta");
    const responseBody = trace.querySelector(".response-body");
    responseMeta.textContent = "응답 대기 중";
    responseBody.textContent = "";
    notice(panel, "요청을 보내는 중입니다.");
    let response;
    try {
        response = await fetch(path, options);
        const status = ("HTTP " + response.status + " " + response.statusText).trimEnd();
        const responseLines = [status];
        for (const [name, value] of response.headers) responseLines.push(name + ": " + value);
        responseMeta.textContent = responseLines.join("\n");
        const text = await response.text();
        responseBody.textContent = text;
        if (!response.ok) {
            notice(panel, status + " · 서버 응답을 확인해 주세요.", true);
            trace.open = true;
            return;
        }
        try {
            const data = JSON.parse(text);
            if (typeof data?.shortenUrlKey !== "string") throw new Error();
            displayLink(panel, data);
            notice(panel, "");
        } catch {
            notice(panel, status + " · 링크 정보를 표시할 수 없습니다. 응답 본문을 확인해 주세요.", true);
            trace.open = true;
        }
    } catch {
        const message = response ? "응답 본문을 다 받지 못했습니다." : "서버 응답을 받지 못했습니다. 연결을 확인해 주세요.";
        if (!response) responseMeta.textContent = "네트워크 오류 · HTTP 응답 없음";
        notice(panel, message, true);
        trace.open = true;
    } finally {
        button.disabled = false;
    }
}

for (const panel of [createPanel, lookupPanel]) {
    panel.append(document.querySelector("#trace-template").content.cloneNode(true));
}
createPanel.querySelector("form").addEventListener("submit", event => {
    event.preventDefault();
    sendRequest(createPanel, "/shorten-url", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: rawEditor.open ? rawBody.value : formBody()
    });
});
lookupPanel.querySelector("form").addEventListener("submit", event => {
    event.preventDefault();
    sendRequest(lookupPanel, "/shorten-url/" + encodeURIComponent(keyInput.value), {
        method: "GET", cache: "no-store"
    });
});

createPanel.querySelector(".copy").addEventListener("click", async () => {
    const url = createPanel.querySelector(".open-link").href;
    try {
        await navigator.clipboard.writeText(url);
        notice(createPanel, "단축 링크를 복사했습니다.");
    } catch {
        window.prompt("아래 단축 링크를 복사해 주세요.", url);
    }
});

window.addEventListener("hashchange", showPage);
showPage();
