const $ = selector => document.querySelector(selector);
const content = $("#content");
const dialog = $("#editor");
const editorForm = $("#editor-form");
let generation = 0;
let selectedStudy;
let selectedMember;
let selectedMeetings = [];
let modalAction;
let pendingMessage = "";
let busy = false;

const iconPaths = {
    list: "M3 5h2m3 0h13M3 12h2m3 0h13M3 19h2m3 0h13",
    person: "M15.5 8a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0ZM4 21v-2a8 8 0 0 1 16 0v2",
    plus: "M12 4v16M4 12h16",
    arrow: "M6 18 18 6M6 6h12v12",
    info: "M12 10v6m0-9v.1M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0",
    refresh: "M20 10a8 8 0 1 0-.7 6M20 4v6h-6",
    calendar: "M4 6h16v15H4zM4 10h16M8 3v6m8-6v6"
};
function icon(name) {
    return `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="${iconPaths[name]}"/></svg>`;
}
function escapeHtml(value) {
    const entities = {"&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"};
    return String(value ?? "").replace(/[&<>"']/g, char => entities[char]);
}
function button(action, label, glyph, primary = false) {
    return `<button type="button" data-action="${action}" class="${primary ? "primary" : ""}">${glyph ? icon(glyph) : ""}${label}</button>`;
}
function heading(title, glyph, command = "", tag = "h1") {
    return `<div class="section-head"><div class="section-name"><span class="section-symbol">${icon(glyph)}</span><${tag}>${escapeHtml(title)}</${tag}></div>${command}</div>`;
}
function state(title, message = "", retry = false) {
    return `<div class="state" role="status"><span class="state-icon">${icon("info")}</span><div><strong>${escapeHtml(title)}</strong>${message ? `<p>${escapeHtml(message)}</p>` : ""}</div>${retry ? button("refresh", "다시 시도", "refresh") : ""}</div>`;
}

async function request(url, {method = "GET", body, expects = "object"} = {}) {
    let response;
    try {
        response = await fetch(url, {
            method,
            headers: body ? {"Content-Type": "application/json", Accept: "application/json, text/plain"} : {Accept: "application/json, text/plain"},
            body: body ? JSON.stringify(body) : undefined
        });
    } catch {
        throw new Error("서버에 연결할 수 없습니다. 연결 상태를 확인해 주세요.");
    }
    const raw = (await response.text()).trim();
    let data;
    try { data = JSON.parse(raw); } catch { /* Error responses may be plain text. */ }
    if (!response.ok) {
        const type = response.headers.get("content-type") || "";
        const detail = data?.detail || data?.message || data?.error
            || (typeof data === "string" && data)
            || ((type.includes("text/plain") || (type.includes("json") && data === undefined)) && raw);
        throw new Error(detail || `서버에서 요청을 처리하지 못했습니다 (HTTP ${response.status}).`);
    }
    if (expects === "void") return;
    if (!raw || data === null) throw new Error("서버 응답에 데이터가 없습니다.");
    const valid = expects === "list" ? Array.isArray(data) : data && typeof data === "object" && !Array.isArray(data);
    if (!valid) throw new Error("서버 응답 형식을 확인해 주세요.");
    return data;
}
function route() {
    const meeting = /^#study\/([1-9]\d*)\/meeting\/([1-9]\d*)$/.exec(location.hash);
    if (meeting) return {page: "study", id: meeting[1], meetingId: meeting[2]};
    const match = /^#(studies|joined|members|study)(?:\/([1-9]\d*))?$/.exec(location.hash);
    if (!match || (match[1] === "study" && !match[2])) return {page: "studies"};
    return {page: match[1], id: match[2]};
}
function go(hash, message = "") {
    pendingMessage = message;
    if (location.hash === hash) render();
    else location.hash = hash;
}
function lookup(id = "") {
    return `<form class="lookup"><label>회원 ID<input type="number" name="memberId" min="1" max="${Number.MAX_SAFE_INTEGER}" step="1" required value="${escapeHtml(id)}"></label><button>조회</button></form>`;
}
function studyRows(items, joined) {
    const columns = `<div class="column-head" aria-hidden="true"><span>ID</span><span>스터디명</span><span>정원</span></div>`;
    if (!items.length) return columns + state(joined ? "가입한 스터디가 없습니다." : "아직 개설된 스터디가 없습니다.", joined ? "전체 스터디에서 참여할 스터디를 찾아보세요." : "새 스터디를 개설해 첫 모임을 시작하세요.");
    return columns + items.map(study => `<a class="study-row" href="#study/${escapeHtml(study.studyId)}"><span class="number">${escapeHtml(study.studyId)}</span><strong>${escapeHtml(study.title)}</strong><span>정원 ${escapeHtml(study.capacity)}명</span>${icon("arrow")}</a>`).join("");
}
async function indexPage(page, id, version) {
    const joined = page === "joined";
    content.innerHTML = heading("스터디 목록", "list", button("create-study", "스터디 개설", "plus", true))
        + `<nav class="views" aria-label="스터디 목록 보기"><a href="#studies" ${joined ? "" : 'aria-current="page"'}>전체 스터디</a><a href="#joined${joined && id ? "/" + id : ""}" ${joined ? 'aria-current="page"' : ""}>가입한 스터디</a></nav>`
        + (joined ? lookup(id) : "") + '<div id="results"></div>';
    const results = $("#results");
    if (joined && !id) {
        results.innerHTML = state("회원 ID를 입력해 주세요.", "해당 회원이 가입한 스터디를 조회합니다.");
        return;
    }
    results.innerHTML = state("목록을 불러오는 중입니다.");
    try {
        if (joined) {
            await request(`/member/${id}`);
            if (version !== generation) return;
        }
        const studies = await request(joined ? `/study?memberId=${id}` : "/study/list", {expects: "list"});
        if (version === generation) results.innerHTML = studyRows(studies, joined);
    } catch (error) {
        if (version === generation) results.innerHTML = state("스터디 목록을 불러올 수 없습니다.", error.message, true);
    }
}
async function memberPage(id, version) {
    content.innerHTML = heading("회원 프로필", "person", button("create-member", "회원 등록", "plus", true)) + lookup(id) + '<div id="results"></div>';
    const results = $("#results");
    if (!id) {
        results.innerHTML = state("조회할 회원 ID를 입력해 주세요.", "새 회원은 회원 등록 후 발급된 ID로 조회할 수 있습니다.");
        return;
    }
    results.innerHTML = state("프로필을 불러오는 중입니다.");
    try {
        const member = await request(`/member/${id}`);
        if (version !== generation) return;
        selectedMember = member;
        results.innerHTML = `<article class="detail"><div class="meta number">ID ${escapeHtml(member.memberId)}</div><h2>${escapeHtml(member.name)}</h2><p>${escapeHtml(member.description || "등록된 소개가 없습니다.")}</p><div class="actions">${button("edit-member", "소개 수정")}<a class="button" href="#joined/${id}">가입한 스터디 보기</a></div></article>`;
    } catch (error) {
        if (version === generation) results.innerHTML = state("프로필을 불러올 수 없습니다.", error.message, true);
    }
}
function dateTime(value) {
    return escapeHtml(String(value ?? "").replace("T", " ").slice(0, 16));
}
function meetingRows(meetings, meetingId) {
    if (!meetings.length) return state("등록된 일정이 없습니다.");
    return [...meetings].sort((a, b) => a.startsAt.localeCompare(b.startsAt)).map(meeting =>
        `<details class="meeting"${String(meeting.meetingId) === meetingId ? " open" : ""}><summary><time datetime="${escapeHtml(meeting.startsAt)}">${dateTime(meeting.startsAt)}</time><strong>${escapeHtml(meeting.title)}</strong><span>${escapeHtml(meeting.location)}</span></summary>
        <div class="meeting-body"><p class="meta">종료 ${dateTime(meeting.finishesAt)} · 작성자 ID ${escapeHtml(meeting.creatorId)}</p><p>${escapeHtml(meeting.description)}</p><div class="actions"><button data-action="delete-meeting" data-id="${escapeHtml(meeting.meetingId)}">일정 삭제</button></div></div></details>`).join("");
}
async function studyPage(id, version, meetingId) {
    const fallbackHeading = heading("스터디 상세", "list");
    content.innerHTML = `<a class="back" href="#studies">← 스터디 목록</a><div id="results">${fallbackHeading}${state("스터디를 불러오는 중입니다.")}</div>`;
    const results = $("#results");
    try {
        const study = await request(`/study/${id}`);
        if (version !== generation) return;
        selectedStudy = study;
        results.innerHTML = heading(study.title, "list", button("join", "스터디 가입", "plus", true))
            + `<article class="detail"><p>${escapeHtml(study.description || "등록된 소개가 없습니다.")}</p><div class="meta">스터디 ID ${id} · 정원 ${escapeHtml(study.capacity)}명 · 개설자 <a href="#members/${escapeHtml(study.creatorId)}">ID ${escapeHtml(study.creatorId)}</a></div><div class="actions">${button("leave", "스터디 탈퇴")}${button("delete-study", "스터디 삭제")}</div></article>`
            + `<section class="subsection">${heading("모임 일정", "calendar", button("create-meeting", "일정 등록", "plus"), "h2")}<div id="meetings">${state("일정을 불러오는 중입니다.")}</div></section>`;
    } catch (error) {
        if (version === generation) results.innerHTML = fallbackHeading + state("스터디를 불러올 수 없습니다.", error.message, true);
        return;
    }
    try {
        const meetings = await request(`/meeting/list?studyId=${id}`, {expects: "list"});
        if (version !== generation) return;
        selectedMeetings = meetings;
        $("#meetings").innerHTML = meetingRows(meetings, meetingId);
        if (meetingId) {
            const summary = $("#meetings details[open] summary");
            if (summary) summary.focus();
            else $("#notice").textContent = "해당 일정이 스터디의 일정 목록에 없습니다.";
        }
    } catch (error) {
        if (version === generation) $("#meetings").innerHTML = state("일정을 불러올 수 없습니다.", error.message, true);
    }
}
function render() {
    const version = ++generation;
    if (dialog.open) dialog.close();
    const {page, id, meetingId} = route();
    selectedStudy = selectedMember = undefined;
    selectedMeetings = [];
    const member = page === "members";
    const detail = page === "study";
    $("#poster-first").textContent = member ? "Member" : "Study";
    $("#poster-second").textContent = member ? "Profiles" : detail ? "Meetings" : "Groups";
    $("#cover-print").setAttribute("href", member ? "covers.svg#cards" : detail ? "covers.svg#planner" : "covers.svg#books");
    $("#nav-studies").removeAttribute("aria-current");
    $("#nav-members").removeAttribute("aria-current");
    $(member ? "#nav-members" : "#nav-studies").setAttribute("aria-current", "page");
    document.title = (member ? "회원 프로필" : detail ? "스터디 상세" : "스터디 목록") + " · Study Member";
    $("#notice").textContent = pendingMessage;
    pendingMessage = "";
    if (member) return memberPage(id, version);
    if (detail) return studyPage(id, version, meetingId);
    return indexPage(page, id, version);
}

function field(name, label, type = "text", value = "") {
    if (type === "textarea") return `<label>${label} (선택)<textarea name="${name}" maxlength="255">${escapeHtml(value)}</textarea></label>`;
    const limits = type === "number" ? `min="1" step="1" max="${name === "capacity" ? 2147483647 : Number.MAX_SAFE_INTEGER}"` : type === "text" ? 'maxlength="255" pattern=".*\\S.*"' : "";
    return `<label>${label}<input name="${name}" type="${type}" ${limits} required></label>`;
}
const description = value => field("description", "소개", "textarea", value);
const memberIdField = () => field("memberId", "회원 ID", "number");
function openEditor(title, submit, fields, action) {
    $("#dialog-title").textContent = title;
    $("#submit").textContent = submit;
    $("#fields").innerHTML = fields;
    $("#form-error").textContent = "";
    modalAction = action;
    dialog.showModal();
}
async function mutate(url, method, body, expects) {
    return request(url, {method, body, expects});
}
function act(action, id) {
    const studyId = selectedStudy?.studyId;
    const studyHash = `#study/${studyId}`;
    const studyTitle = escapeHtml(selectedStudy?.title);
    switch (action) {
        case "refresh": return render();
        case "create-member":
            return openEditor("회원 등록", "등록", field("name", "이름") + description(), async data => {
                const member = await mutate("/member", "POST", data);
                return [`#members/${member.memberId}`, `회원 등록을 완료했습니다. 회원 ID는 ${member.memberId}입니다.`];
            });
        case "edit-member":
            return openEditor("회원 소개 수정", "저장", description(selectedMember.description), async data => {
                const member = await mutate(`/member/${selectedMember.memberId}`, "PATCH", data);
                return [`#members/${member.memberId}`, "회원 소개를 저장했습니다."];
            });
        case "create-study":
            return openEditor("스터디 개설", "개설", field("title", "스터디명") + field("creatorId", "개설자 회원 ID", "number") + field("capacity", "정원", "number") + description(), async data => {
                const study = await mutate("/study", "POST", data);
                return [`#study/${study.studyId}`, "스터디를 개설했습니다."];
            });
        case "join":
        case "leave": {
            const join = action === "join";
            const verb = join ? "가입" : "탈퇴";
            const prompt = join ? `‘${studyTitle}’에 가입할 회원 ID를 입력하세요.` : `‘${studyTitle}’에서 탈퇴할 회원 ID를 입력하세요.`;
            return openEditor(`스터디 ${verb}`, verb, `<p>${prompt}</p>` + memberIdField(), async data => {
                const study = join
                    ? await mutate(`/study/${studyId}/member`, "POST", data)
                    : await mutate(`/study/${studyId}/member/${data.memberId}`, "DELETE");
                return [`#joined/${data.memberId}`, `회원 ID ${data.memberId}의 ‘${study.title}’ ${verb} 처리를 완료했습니다.`];
            });
        }
        case "delete-study":
            return openEditor("스터디를 삭제할까요?", "삭제", `<p>‘${studyTitle}’ 스터디의 가입 내역과 모임 일정도 함께 삭제됩니다.</p>`, async () => {
                await mutate(`/study/${studyId}`, "DELETE", undefined, "void");
                return ["#studies", "스터디를 삭제했습니다."];
            });
        case "create-meeting":
            return openEditor("모임 일정 등록", "등록", field("title", "일정명") + field("memberId", "작성자 회원 ID", "number") + field("startsAt", "시작 일시", "datetime-local") + field("finishesAt", "종료 일시", "datetime-local") + field("location", "장소") + field("description", "내용", "textarea"), async data => {
                if (data.finishesAt <= data.startsAt) throw new Error("종료 일시는 시작 일시보다 늦어야 합니다.");
                const meeting = await mutate(`/meeting?studyId=${studyId}`, "POST", data);
                return [`#study/${meeting.studyId}/meeting/${meeting.meetingId}`, "모임 일정을 등록했습니다."];
            });
        case "delete-meeting": {
            const meeting = selectedMeetings.find(item => String(item.meetingId) === id);
            if (!meeting) return;
            return openEditor("일정을 삭제할까요?", "삭제", `<p>‘${escapeHtml(meeting.title)}’ 일정을 삭제합니다.</p>`, async () => {
                await mutate(`/meeting/${id}`, "DELETE", undefined, "void");
                return [studyHash, "일정을 삭제했습니다."];
            });
        }
    }
}
content.addEventListener("click", event => {
    const control = event.target.closest("[data-action]");
    if (control) act(control.dataset.action, control.dataset.id);
});
content.addEventListener("submit", event => {
    if (!event.target.matches(".lookup")) return;
    event.preventDefault();
    const id = Number(event.target.elements.memberId.value);
    go(`#${route().page}/${id}`);
});
$("#cancel").addEventListener("click", () => dialog.close());
dialog.addEventListener("cancel", event => { if (busy) event.preventDefault(); });
editorForm.addEventListener("submit", async event => {
    event.preventDefault();
    if (busy) return;
    const version = generation;
    const data = Object.fromEntries(new FormData(editorForm));
    for (const input of editorForm.querySelectorAll("input,textarea")) {
        data[input.name] = input.type === "number" ? input.valueAsNumber : input.value.trim();
    }
    busy = true;
    $("#submit").disabled = $("#cancel").disabled = true;
    $("#form-error").textContent = "";
    try {
        const [hash, message] = await modalAction(data);
        if (version === generation) {
            dialog.close();
            go(hash, message);
        }
    } catch (error) {
        if (version === generation) $("#form-error").textContent = error.message;
    } finally {
        busy = false;
        $("#submit").disabled = $("#cancel").disabled = false;
    }
});
$(".skip").addEventListener("click", event => {
    event.preventDefault();
    $("#workspace").focus();
});
window.addEventListener("hashchange", () => {
    render();
    $("#workspace").focus({preventScroll: true});
    window.scrollTo(0, 0);
});
render();
