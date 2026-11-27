const byId = id => document.getElementById(id);
const endpoints = '/api/v1';
const actors = new Map();
const workbench = document.querySelector('main');
const dateFormat = new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' });
const state = { actor: null, study: null, scope: 'all', page: 0, rosterPage: 0, meetingPage: 0, busy: false };

function feedback(message, isError = false) {
  const status = byId('feedback');
  status.textContent = message;
  status.dataset.error = isError;
  const dialog = document.querySelector('dialog[open] .form-message');
  if (dialog) dialog.textContent = isError ? message : '';
}

async function request(method, path, { body, actor, quiet = false } = {}) {
  const headers = new Headers();
  if (actor != null) headers.set('X-Member-Id', actor);
  if (body != null) headers.set('Content-Type', 'application/json');
  const response = await fetch(endpoints + path, { method, headers, body: body == null ? undefined : JSON.stringify(body) });
  const text = await response.text();
  let payload = null;
  if (text) {
    try { payload = JSON.parse(text); } catch { payload = text; }
  }
  if (!quiet || !response.ok) {
    const status = byId('response-status');
    status.textContent = response.status;
    status.hidden = false;
    status.className = 'pill ' + (response.ok ? 'good' : 'bad');
    byId('response-path').textContent = method + ' ' + endpoints + path;
    const location = response.headers.get('Location');
    const displayed = typeof payload === 'string' ? payload : JSON.stringify(payload, null, 2);
    byId('response-body').textContent = (location ? 'Location: ' + location + '\n\n' : '') + (text ? displayed : '응답 본문 없음');
  }
  if (!response.ok) {
    const message = payload?.message || '요청을 처리하지 못했습니다.';
    throw new Error(response.status + (payload?.code ? ' ' + payload.code : '') + ' · ' + message);
  }
  return payload;
}

function execute(action) {
  if (state.busy) return;
  state.busy = true;
  workbench.setAttribute('aria-busy', 'true');
  feedback('');
  return Promise.resolve().then(action).catch(error => feedback(error.message, true)).finally(() => {
    state.busy = false;
    workbench.setAttribute('aria-busy', 'false');
  });
}

function on(id, event, action) {
  byId(id).addEventListener(event, ev => {
    if (event === 'submit') ev.preventDefault();
    execute(() => action(ev));
  });
}

function currentActor() {
  if (state.actor == null) throw new Error('상단에서 요청자를 선택하세요. 회원 ID로 등록된 회원을 불러올 수 있습니다.');
  return state.actor;
}

function selectActor(id) {
  state.actor = id;
  const profile = actors.get(id);
  const select = byId('actor');
  select.replaceChildren(new Option('선택 안 함', ''));
  for (const saved of actors.values()) select.add(new Option(saved.nickname + ' · #' + saved.memberId, saved.memberId));
  select.value = id ?? '';
  byId('profile-fields').disabled = !profile;
  const fields = byId('profile-form').elements;
  fields.nickname.value = profile?.nickname ?? '';
  fields.bio.value = profile?.bio ?? '';
  clearPrivateLists();
}

function showProfile(profile) {
  const display = byId('public-profile');
  display.hidden = false;
  const name = document.createElement('strong');
  name.textContent = profile.nickname + ' · 회원 #' + profile.memberId;
  const bio = document.createElement('p');
  bio.textContent = profile.bio ?? '자기소개 없음';
  display.replaceChildren(name, bio);
}

function showView(view) {
  const member = view === 'member';
  byId('member-view').hidden = !member;
  byId('study-view').hidden = member;
  byId('member-tab').setAttribute('aria-pressed', member);
  byId('study-tab').setAttribute('aria-pressed', !member);
}

function showScope(scope) {
  state.scope = scope;
  state.page = 0;
  byId('all-studies').setAttribute('aria-pressed', scope === 'all');
  byId('my-studies').setAttribute('aria-pressed', scope === 'mine');
  byId('filter-label').hidden = scope === 'mine';
}

function recruitmentLabel(status) {
  return status === 'OPEN' ? '모집 중' : '모집 종료';
}

function renderStudyCard(study, role) {
  const button = document.createElement('button');
  button.type = 'button';
  button.className = 'study-card';
  button.dataset.studyId = study.studyId;
  button.setAttribute('aria-current', state.study?.studyId === study.studyId);
  const title = document.createElement('span');
  title.textContent = study.title;
  const meta = document.createElement('span');
  meta.className = 'card-meta';
  const pill = document.createElement('span');
  pill.className = 'pill' + (study.recruitmentStatus === 'OPEN' ? ' good' : '');
  pill.textContent = recruitmentLabel(study.recruitmentStatus);
  const count = document.createElement('span');
  count.textContent = study.activeMemberCount + ' / ' + study.capacity + '명 · 남은 ' + study.availableSlots + '자리';
  meta.append(pill, count);
  if (role) {
    const label = document.createElement('span');
    label.textContent = role === 'OWNER' ? '스터디장' : '참여자';
    meta.append(label);
  }
  button.append(title, meta);
  button.addEventListener('click', () => execute(async () => {
    const detail = await request('GET', '/studies/' + study.studyId);
    clearPrivateLists();
    showStudy(detail);
  }));
  return button;
}

async function loadStudies(quiet = false) {
  const query = new URLSearchParams({ page: state.page, size: 5 });
  const mine = state.scope === 'mine';
  const filter = byId('recruitment-filter').value;
  if (!mine && filter) query.set('recruitmentStatus', filter);
  const path = mine ? '/members/me/studies' : '/studies';
  const page = await request('GET', path + '?' + query, { actor: mine ? currentActor() : null, quiet });
  const items = mine ? page.items.map(item => renderStudyCard(item.study, item.role)) : page.items.map(item => renderStudyCard(item));
  const list = byId('study-list');
  list.replaceChildren(...items);
  if (!items.length) {
    const empty = document.createElement('p');
    empty.className = 'blank';
    empty.textContent = '조회된 스터디가 없습니다.';
    list.append(empty);
  }
  byId('page-info').textContent = page.totalPages ? (page.page + 1) + ' / ' + page.totalPages + ' 페이지' : '0건';
  byId('previous-page').disabled = state.page === 0;
  byId('next-page').disabled = state.page + 1 >= page.totalPages;
}

function showStudy(study) {
  state.study = study;
  byId('empty-detail').hidden = true;
  byId('study-detail').hidden = false;
  byId('detail-title').textContent = study.title;
  byId('detail-description').textContent = study.description;
  byId('detail-status').textContent = recruitmentLabel(study.recruitmentStatus);
  byId('detail-status').className = 'pill' + (study.recruitmentStatus === 'OPEN' ? ' good' : '');
  byId('detail-id').textContent = '#' + study.studyId;
  byId('detail-owner').textContent = '#' + study.ownerMemberId;
  byId('detail-capacity').textContent = study.activeMemberCount + ' / ' + study.capacity + '명';
  byId('detail-slots').textContent = study.availableSlots + '자리';
  byId('change-recruitment').textContent = study.recruitmentStatus === 'OPEN' ? '모집 닫기' : '모집 열기';
  for (const card of byId('study-list').querySelectorAll('.study-card')) {
    card.setAttribute('aria-current', Number(card.dataset.studyId) === study.studyId);
  }
}

function studyPath() {
  return '/studies/' + state.study.studyId;
}

function clearPrivateLists() {
  for (const id of ['participants', 'meetings']) byId(id).replaceChildren();
  for (const id of ['more-participants', 'more-meetings']) byId(id).hidden = true;
  state.rosterPage = 0;
  state.meetingPage = 0;
}

function renderRows(container, entries, format, append) {
  if (!append) container.replaceChildren();
  if (!append && !entries.length) {
    const row = document.createElement('p');
    row.className = 'blank';
    row.textContent = '조회 결과가 없습니다.';
    container.append(row);
  }
  for (const entry of entries) {
    const row = document.createElement('p');
    row.textContent = format(entry);
    container.append(row);
  }
}

async function loadParticipants(append = false) {
  const pageNumber = append ? state.rosterPage + 1 : 0;
  const page = await request('GET', studyPath() + '/members?page=' + pageNumber, { actor: currentActor() });
  state.rosterPage = pageNumber;
  renderRows(byId('participants'), page.items, person =>
    person.nickname + ' · #' + person.memberId + ' · ' + (person.role === 'OWNER' ? '스터디장' : '참여자'), append);
  byId('more-participants').hidden = pageNumber + 1 >= page.totalPages;
}

async function loadMeetings(append = false, quiet = false) {
  const pageNumber = append ? state.meetingPage + 1 : 0;
  const page = await request('GET', studyPath() + '/meetings?page=' + pageNumber, { actor: currentActor(), quiet });
  state.meetingPage = pageNumber;
  renderRows(byId('meetings'), page.items, meeting =>
    meeting.title + ' · ' + meeting.location + ' · ' + dateFormat.format(new Date(meeting.startsAt)) + ' ~ ' + dateFormat.format(new Date(meeting.endsAt)), append);
  byId('more-meetings').hidden = pageNumber + 1 >= page.totalPages;
}

async function refreshAfterMembershipChange() {
  clearPrivateLists();
  showStudy(await request('GET', studyPath(), { quiet: true }));
  await loadStudies(true);
}

function fields(event) {
  return Object.fromEntries(new FormData(event.currentTarget));
}

// Capture the submitted fields synchronously because currentTarget is cleared once the event handler returns.
function onForm(id, action) {
  byId(id).addEventListener('submit', event => {
    event.preventDefault();
    const values = fields(event);
    execute(() => action(values, event.target));
  });
}

on('member-tab', 'click', () => showView('member'));
on('study-tab', 'click', async () => { showView('study'); await loadStudies(); });
on('actor', 'change', async event => {
  selectActor(event.target.value ? Number(event.target.value) : null);
  if (!byId('study-view').hidden && state.scope === 'mine') {
    if (state.actor == null) showScope('all');
    state.page = 0;
    await loadStudies();
  }
});

onForm('register-form', async (body, form) => {
  const profile = await request('POST', '/members', { body });
  actors.set(profile.memberId, profile);
  selectActor(profile.memberId);
  showProfile(profile);
  byId('lookup-form').elements.memberId.value = profile.memberId;
  form.reset();
  feedback('회원 #' + profile.memberId + ' 등록 완료');
});
onForm('lookup-form', async ({ memberId }) => {
  const profile = await request('GET', '/members/' + memberId);
  actors.set(profile.memberId, profile);
  selectActor(profile.memberId);
  showProfile(profile);
  feedback('조회한 회원을 요청자로 선택했습니다.');
});
onForm('profile-form', async body => {
  const profile = await request('PUT', '/members/me/profile', { actor: currentActor(), body });
  actors.set(profile.memberId, profile);
  selectActor(profile.memberId);
  showProfile(profile);
  feedback('프로필을 저장했습니다.');
});

on('refresh-studies', 'click', () => loadStudies());
on('all-studies', 'click', async () => { showScope('all'); await loadStudies(); });
on('my-studies', 'click', async () => { currentActor(); showScope('mine'); await loadStudies(); });
on('recruitment-filter', 'change', async () => { state.page = 0; await loadStudies(); });
on('previous-page', 'click', async () => { state.page--; await loadStudies(); });
on('next-page', 'click', async () => { state.page++; await loadStudies(); });

for (const button of document.querySelectorAll('[data-close]')) {
  button.addEventListener('click', () => byId(button.dataset.close).close());
}
on('open-study', 'click', () => { currentActor(); byId('study-dialog').showModal(); });
onForm('study-form', async (values, form) => {
  const body = { ...values, capacity: Number(values.capacity) };
  const study = await request('POST', '/studies', { actor: currentActor(), body });
  byId('study-dialog').close();
  form.reset();
  clearPrivateLists();
  showScope('all');
  byId('recruitment-filter').value = '';
  showStudy(study);
  await loadStudies(true);
  feedback('스터디 #' + study.studyId + '를 개설했습니다.');
});

on('join-study', 'click', async () => {
  await request('POST', studyPath() + '/members', { actor: currentActor() });
  await refreshAfterMembershipChange();
  feedback('스터디에 참여했습니다.');
});
on('leave-study', 'click', async () => {
  await request('DELETE', studyPath() + '/members/me', { actor: currentActor() });
  await refreshAfterMembershipChange();
  feedback('스터디 참여가 종료됐습니다.');
});
on('change-recruitment', 'click', async () => {
  const recruitmentStatus = state.study.recruitmentStatus === 'OPEN' ? 'CLOSED' : 'OPEN';
  showStudy(await request('PUT', studyPath() + '/recruitment', { actor: currentActor(), body: { recruitmentStatus } }));
  await loadStudies(true);
  feedback(recruitmentLabel(recruitmentStatus) + ' 상태로 변경했습니다.');
});
on('show-participants', 'click', () => loadParticipants());
on('more-participants', 'click', () => loadParticipants(true));

on('show-meetings', 'click', () => loadMeetings());
on('more-meetings', 'click', () => loadMeetings(true));
on('open-meeting', 'click', () => { currentActor(); byId('meeting-dialog').showModal(); });
onForm('meeting-form', async (values, form) => {
  const body = { ...values, startsAt: new Date(values.startsAt).toISOString(), endsAt: new Date(values.endsAt).toISOString() };
  await request('POST', studyPath() + '/meetings', { actor: currentActor(), body });
  byId('meeting-dialog').close();
  form.reset();
  await loadMeetings(false, true);
  feedback('모임 일정을 등록했습니다.');
});
