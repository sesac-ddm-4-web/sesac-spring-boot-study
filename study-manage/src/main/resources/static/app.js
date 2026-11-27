"use strict";

const byId = id => document.getElementById(id);
const profileForm = byId("profile");
const meetingForm = byId("meeting");
const detailPanel = byId("detail");
const createDialog = byId("create-dialog");
const studyList = byId("studies");
const dateFormat = new Intl.DateTimeFormat("ko-KR", {
  dateStyle: "medium",
  timeStyle: "short"
});

let activeMember = null;
let selectedStudy = null;
let requestedStudyId = null;
let detailVersion = 0;

function showStatus(message, error = false, element = byId("status")) {
  element.textContent = message;
  element.toggleAttribute("data-error", error);
  element.hidden = !message;
}

async function api(path, method = "GET", data) {
  const options = { method };
  if (data !== undefined) {
    options.headers = { "Content-Type": "application/json" };
    options.body = JSON.stringify(data);
  }
  const response = await fetch("/api" + path, options);
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    const messages = {
      400: "입력값을 확인해 주세요.",
      403: "스터디장만 일정을 등록할 수 있어요.",
      404: "요청한 정보를 찾을 수 없어요.",
      409: "이미 참여 중이거나 정원이 찼는지 확인해 주세요."
    };
    const error = new Error(body.detail || body.message || messages[response.status] ||
      `요청에 실패했어요 (HTTP ${response.status}).`);
    error.status = response.status;
    throw error;
  }
  return response.status === 204 ? null : response.json();
}

async function run(button, action, errorElement) {
  if (button) button.disabled = true;
  showStatus("", false, errorElement);
  try {
    await action();
  } catch (error) {
    showStatus(error.message, true, errorElement);
  } finally {
    if (button) button.disabled = false;
  }
}

function setMember(member) {
  activeMember = member;
  profileForm.elements.name.value = member?.name ?? "";
  profileForm.elements.bio.value = member?.bio ?? "";
  byId("active-member").textContent = member
    ? `${member.name} · 회원 ID ${member.id}`
    : "회원 등록 또는 기존 회원 ID 조회 후 스터디에 참여할 수 있어요.";
  byId("save-member").textContent = member ? "프로필 저장" : "회원 등록";
  byId("reset-member").hidden = !member;
  updateDetailActions();
}

function requireMember() {
  if (activeMember) return activeMember.id;
  byId("lookup-id").focus();
  throw new Error("회원으로 등록하거나 기존 회원 ID를 먼저 조회해 주세요.");
}

function updateDetailActions() {
  if (!selectedStudy) return;
  const isLeader = activeMember?.id === selectedStudy.leaderId;
  byId("leave").hidden = isLeader;
  byId("join").hidden = isLeader;
  meetingForm.hidden = !isLeader;
}

function markSelectedStudy() {
  for (const card of studyList.querySelectorAll(".card")) {
    card.setAttribute("aria-pressed", String(card.dataset.id === String(requestedStudyId)));
  }
}

async function loadStudies() {
  const studies = await api("/studies");
  if (studies.length === 0) {
    const empty = document.createElement("p");
    empty.className = "panel muted";
    empty.textContent = "아직 개설된 스터디가 없어요.";
    studyList.replaceChildren(empty);
    return;
  }
  studyList.replaceChildren(...studies.map(study => {
    const button = document.createElement("button");
    button.className = "card";
    button.dataset.id = study.id;
    const title = document.createElement("strong");
    title.textContent = study.title;
    const capacity = document.createElement("span");
    capacity.className = "meta";
    capacity.textContent = `정원 ${study.capacity}명`;
    button.append(title, capacity);
    button.addEventListener("click", () => run(button, () => loadDetail(study.id)));
    return button;
  }));
  markSelectedStudy();
}

function renderRows(id, items, render, emptyLabel) {
  const rows = items.map(render);
  if (rows.length === 0) {
    const empty = document.createElement("li");
    empty.className = "muted";
    empty.textContent = emptyLabel;
    rows.push(empty);
  }
  byId(id).replaceChildren(...rows);
}

function row(title, subtitle) {
  const item = document.createElement("li");
  const text = document.createElement("span");
  text.textContent = subtitle;
  item.append(title, text);
  return item;
}

async function loadDetail(studyId) {
  const version = ++detailVersion;
  if (requestedStudyId !== studyId) {
    requestedStudyId = studyId;
    selectedStudy = null;
    detailPanel.hidden = true;
    meetingForm.reset();
  }
  markSelectedStudy();

  const path = `/studies/${studyId}`;
  const [study, participants, meetings] = await Promise.all([
    api(path), api(path + "/participants"), api(path + "/meetings")
  ]);
  if (version !== detailVersion) return;

  selectedStudy = study;
  byId("study-title").textContent = study.title;
  byId("study-meta").textContent = `참여 ${participants.length}명 / 정원 ${study.capacity}명`;
  byId("study-description").textContent = study.description;
  renderRows("participants", participants, participant => row(
    participant.name,
    participant.memberId === study.leaderId ? "스터디장" : dateFormat.format(new Date(participant.joinedAt))
  ), "아직 참여자가 없어요.");
  renderRows("meetings", meetings, meeting => row(
    meeting.title, `${dateFormat.format(new Date(meeting.startsAt))} · ${meeting.location}`
  ), "등록된 일정이 없어요.");
  updateDetailActions();
  detailPanel.hidden = false;
}

function onSubmit(form, action, errorElement) {
  form.addEventListener("submit", event => {
    event.preventDefault();
    const fields = Object.fromEntries(new FormData(form));
    run(event.submitter, () => action(fields), errorElement);
  });
}

onSubmit(byId("lookup"), async ({ id }) => {
  setMember(await api(`/members/${Number(id)}`));
  showStatus("회원 프로필을 불러왔어요.");
});

onSubmit(profileForm, async ({ name, bio }) => {
  const member = activeMember;
  const data = { name: name.trim(), bio: bio.trim() };
  setMember(await (member ? api(`/members/${member.id}`, "PUT", data) : api("/members", "POST", data)));
  showStatus(member ? "프로필을 저장했어요." : `회원으로 등록했어요. 회원 ID는 ${activeMember.id}입니다.`);
  if (requestedStudyId !== null) await loadDetail(requestedStudyId);
});

byId("profile-link").addEventListener("click", () => byId("lookup-id").focus());
byId("reset-member").addEventListener("click", () => {
  setMember(null);
  profileForm.elements.name.focus();
});
byId("refresh").addEventListener("click", event => run(event.currentTarget, async () => {
  await loadStudies();
  if (requestedStudyId !== null) await loadDetail(requestedStudyId);
}));
byId("new-study").addEventListener("click", event => run(event.currentTarget, () => {
  requireMember();
  showStatus("", false, byId("create-error"));
  createDialog.showModal();
}));
byId("close-dialog").addEventListener("click", () => createDialog.close());

onSubmit(byId("create-study"), async ({ title, description, capacity }) => {
  const study = await api("/studies", "POST", {
    leaderId: requireMember(), title: title.trim(), description: description.trim(), capacity: Number(capacity)
  });
  createDialog.close();
  byId("create-study").reset();
  showStatus("스터디를 개설했어요.");
  await run(null, async () => {
    await loadStudies();
    await loadDetail(study.id);
    detailPanel.scrollIntoView({ block: "nearest" });
  });
}, byId("create-error"));

byId("join").addEventListener("click", event => run(event.currentTarget, async () => {
  const studyId = selectedStudy.id;
  await api(`/studies/${studyId}/participants`, "POST", { memberId: requireMember() });
  showStatus("스터디에 참여했어요.");
  if (requestedStudyId === studyId) await loadDetail(studyId);
}));

byId("leave").addEventListener("click", event => run(event.currentTarget, async () => {
  const studyId = selectedStudy.id;
  await api(`/studies/${studyId}/participants/${requireMember()}`, "DELETE");
  showStatus("스터디에서 탈퇴했어요.");
  if (requestedStudyId === studyId) await loadDetail(studyId);
}));

onSubmit(meetingForm, async ({ title, location, startsAt }) => {
  const studyId = selectedStudy.id;
  const date = new Date(startsAt);
  if (!(date > new Date())) throw new Error("미래의 모임 시작 시간을 입력해 주세요.");
  await api(`/studies/${studyId}/meetings`, "POST", {
    memberId: requireMember(), title: title.trim(), location: location.trim(), startsAt: date.toISOString()
  });
  showStatus("모임 일정을 등록했어요.");
  if (requestedStudyId === studyId) {
    meetingForm.reset();
    await loadDetail(studyId);
  }
});

run(null, async () => {
  try {
    await loadStudies();
  } catch (error) {
    studyList.replaceChildren();
    throw error;
  }
});
