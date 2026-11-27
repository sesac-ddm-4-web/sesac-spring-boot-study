'use strict';

/* =========================================================
 * Study Hub - API 테스트용 프론트엔드
 * 서버 검증 결과를 그대로 보기 위해 클라이언트 검증은 일부러 하지 않음
 * ========================================================= */

const TOKEN_KEY = 'studyhub.token';

const storage = {
  get(key) { try { return localStorage.getItem(key); } catch { return null; } },
  set(key, value) { try { localStorage.setItem(key, value); } catch { /* 무시 */ } },
  remove(key) { try { localStorage.removeItem(key); } catch { /* 무시 */ } },
};

const state = {
  token: storage.get(TOKEN_KEY),
  me: null,
  view: 'studies',
  prevView: 'studies',
  studyId: null,
  list: { page: 0, size: 6, status: '' },
};

/* ---------- 유틸 ---------- */
const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];

const ESCAPE = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ESCAPE[c]);

const WEEK = ['일', '월', '화', '수', '목', '금', '토'];
const pad = (n) => String(n).padStart(2, '0');
const fmtDate = (s) => {
  if (!s) return '';
  const d = new Date(s);
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())}`;
};
const fmtTime = (s) => {
  const d = new Date(s);
  return `(${WEEK[d.getDay()]}) ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};
// datetime-local 값(2026-12-05T19:00) → LocalDateTime 형식(2026-12-05T19:00:00)
const toLocalDateTime = (v) => (v ? (v.length === 16 ? `${v}:00` : v) : null);

/* ---------- API ---------- */
class ApiError extends Error {
  constructor(status, data) {
    super(data?.message || `요청에 실패했어요. (${status})`);
    this.status = status;
    this.data = data;
  }
}

async function api(path, { method = 'GET', body } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (state.token) headers.Authorization = `Bearer ${state.token}`;

  const res = await fetch(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (res.status === 204) return null;

  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = null; }

  if (!res.ok) throw new ApiError(res.status, data);
  return data;
}

/* ---------- 토스트 / 에러 ---------- */
function toast(message, { type = 'success', code = '', details = [] } = {}) {
  const el = document.createElement('div');
  el.className = `toast toast--${type}`;
  el.innerHTML = `
    ${code ? `<span class="toast__code">${esc(code)}</span>` : ''}
    <strong>${esc(message)}</strong>
    ${details.length ? `<ul>${details.map((d) => `<li>${esc(d)}</li>`).join('')}</ul>` : ''}`;
  $('#toasts').appendChild(el);
  setTimeout(() => {
    el.classList.add('is-leaving');
    setTimeout(() => el.remove(), 300);
  }, type === 'error' ? 5000 : 3000);
}

function showError(e) {
  if (!(e instanceof ApiError)) {
    toast('서버에 연결할 수 없어요. 서버가 켜져 있는지 확인해 주세요.', { type: 'error' });
    return;
  }
  // 토큰 없음/만료 → 로그인 화면으로 (로그인 실패 401은 제외)
  if (e.status === 401 && e.data?.code !== 'LOGIN_FAILED') {
    if (state.token) {
      clearSession();
      toast('로그인이 만료되었어요. 다시 로그인해 주세요.', { type: 'error', code: `401 ${e.data?.code ?? ''}` });
    } else {
      toast('로그인이 필요해요.', { type: 'error', code: '401 UNAUTHORIZED' });
    }
    go('auth');
    return;
  }
  const details = (e.data?.errors ?? []).map((x) => `${x.field}: ${x.reason}`);
  const code = e.data?.code ? `${e.status} ${e.data.code}` : String(e.status);
  toast(e.message, { type: 'error', code, details });
}

/* ---------- 세션 ---------- */
function clearSession() {
  state.token = null;
  state.me = null;
  storage.remove(TOKEN_KEY);
  renderHeader();
}

async function loadMe() {
  if (!state.token) {
    state.me = null;
    renderHeader();
    return;
  }
  try {
    state.me = await api('/api/members/me');
  } catch (e) {
    if (e instanceof ApiError && e.status === 401) clearSession();
  }
  renderHeader();
}

async function login(email, password) {
  const { accessToken } = await api('/api/auth/login', { method: 'POST', body: { email, password } });
  state.token = accessToken;
  storage.set(TOKEN_KEY, accessToken);
  await loadMe();
  toast(`${state.me?.name ?? ''}님, 환영해요!`);
  go('studies');
}

function requireLogin() {
  if (state.token) return true;
  toast('로그인이 필요해요.', { type: 'error' });
  go('auth');
  return false;
}

/* ---------- 공통 조각 ---------- */
function avatarHtml(person, size = 'md') {
  const initial = esc((person?.name || '?').slice(0, 1));
  if (person?.imageUrl) {
    return `<img class="avatar avatar--${size}" src="${esc(person.imageUrl)}" alt="" data-initial="${initial}">`;
  }
  return `<span class="avatar avatar--${size}">${initial}</span>`;
}

const statusBadge = (status) => (status === 'RECRUITING'
  ? '<span class="badge badge--open">모집중</span>'
  : '<span class="badge badge--closed">마감</span>');

const roleBadge = (role) => (role === 'LEADER'
  ? '<span class="badge badge--leader">스터디장</span>'
  : '<span class="badge">멤버</span>');

function capacityBar(current, capacity) {
  const pct = Math.min(100, Math.round((current / capacity) * 100));
  const full = current >= capacity;
  return `
    <div class="capacity">
      <div class="capacity__bar"><span class="${full ? 'is-full' : ''}" style="width:${pct}%"></span></div>
      <span class="capacity__text">${current} / ${capacity}명${full ? ' · 정원 마감' : ''}</span>
    </div>`;
}

/* ---------- 화면 전환 ---------- */
const render = {};

function go(view, params = {}) {
  Object.assign(state, params);
  state.view = view;
  $$('.view').forEach((v) => { v.hidden = v.dataset.view !== view; });
  $$('.nav__item').forEach((b) => {
    b.classList.toggle('is-active', b.dataset.nav === view);
  });
  window.scrollTo(0, 0);
  render[view]?.();
}

function openStudy(studyId) {
  if (state.view !== 'detail') state.prevView = state.view;
  go('detail', { studyId });
}

function renderHeader() {
  const box = $('#userBox');
  if (state.me) {
    box.innerHTML = `
      <span class="chip">${avatarHtml(state.me, 'sm')}${esc(state.me.name)}</span>
      <button class="btn btn--ghost" id="logoutBtn">로그아웃</button>`;
  } else {
    box.innerHTML = '<button class="btn btn--primary" data-nav="auth">로그인</button>';
  }
}

/* ---------- 스터디 목록 ---------- */
function studyCard(s) {
  return `
    <button class="card study-card" data-study="${s.id}">
      <div class="study-card__top">${statusBadge(s.status)}<span class="muted small">${fmtDate(s.createdAt)}</span></div>
      <h3>${esc(s.name)}</h3>
      <p class="muted">스터디장 ${esc(s.leader.name)}</p>
      ${capacityBar(s.currentMemberCount, s.capacity)}
    </button>`;
}

render.studies = async () => {
  const { page, size, status } = state.list;
  const query = new URLSearchParams({ page, size });
  if (status) query.set('status', status);

  $$('#statusFilter button').forEach((b) => b.classList.toggle('is-active', b.dataset.status === status));

  const grid = $('#studyGrid');
  try {
    const data = await api(`/api/studies?${query}`);
    grid.innerHTML = data.content.length
      ? data.content.map(studyCard).join('')
      : '<p class="empty">아직 스터디가 없어요. 첫 스터디를 만들어 보세요!</p>';
    $('#pageInfo').textContent = data.totalPages ? `${data.page + 1} / ${data.totalPages}` : '0 / 0';
    $('#prevPage').disabled = data.page === 0;
    $('#nextPage').disabled = !data.hasNext;
  } catch (e) {
    grid.innerHTML = '<p class="empty">스터디 목록을 불러오지 못했어요.</p>';
    showError(e);
  }
};

/* ---------- 스터디 상세 ---------- */
function memberList(members) {
  if (!members.length) return '<p class="empty">참여자가 없어요.</p>';
  return `<ul class="list">${members.map((m) => `
    <li class="list__item">
      ${avatarHtml(m, 'sm')}
      <div class="list__body">
        <span><strong>${esc(m.name)}</strong>${m.memberId === state.me?.id ? '<span class="me-tag">나</span>' : ''}</span>
        <span class="muted small">${fmtDate(m.joinedAt)} 참여</span>
      </div>
      ${roleBadge(m.role)}
    </li>`).join('')}</ul>`;
}

function scheduleList(schedules) {
  if (!schedules.length) return '<p class="empty">등록된 일정이 없어요.</p>';
  return `<ul class="list">${schedules.map((sc) => {
    const d = new Date(sc.startAt);
    return `
      <li class="list__item">
        <div class="date-chip"><span>${d.getMonth() + 1}월</span><strong>${d.getDate()}</strong></div>
        <div class="list__body">
          <strong>${esc(sc.title)}</strong>
          <span class="muted small">${fmtTime(sc.startAt)} – ${fmtTime(sc.endAt)}${sc.place ? ` · ${esc(sc.place)}` : ''}</span>
        </div>
      </li>`;
  }).join('')}</ul>`;
}

const scheduleForm = () => `
  <form id="scheduleForm" class="inline-form">
    <h3>일정 등록 <span class="muted small">(스터디장만 보여요)</span></h3>
    <input name="title" type="text" placeholder="일정 제목 (예: 1주차 JPA 기초)">
    <input name="place" type="text" placeholder="장소 (선택)">
    <div class="two">
      <label>시작<input name="startAt" type="datetime-local"></label>
      <label>종료<input name="endAt" type="datetime-local"></label>
    </div>
    <button class="btn btn--primary" type="submit">등록하기</button>
  </form>`;

function detailHtml(study, members, schedules) {
  const myId = state.me?.id;
  const isLeader = myId != null && study.leader.id === myId;
  const isMember = members?.some((m) => m.memberId === myId) ?? false;

  let action;
  if (!state.token) action = '<button class="btn btn--primary" data-nav="auth">로그인하고 참여하기</button>';
  else if (isLeader) action = '<span class="badge badge--leader">내가 스터디장</span>';
  else if (isMember) action = '<button class="btn btn--danger" data-action="leave">탈퇴하기</button>';
  else action = '<button class="btn btn--primary" data-action="join">참여하기</button>';

  const locked = '<p class="empty">로그인하면 볼 수 있어요.</p>';

  return `
    <article class="card detail-head">
      <div class="detail-head__meta">${statusBadge(study.status)}<span class="muted small">개설 ${fmtDate(study.createdAt)}</span></div>
      <div class="detail-head__row">
        <div>
          <h1>${esc(study.name)}</h1>
          <p class="muted">스터디장 <strong>${esc(study.leader.name)}</strong></p>
        </div>
        <div>${action}</div>
      </div>
      ${study.description ? `<p class="detail-head__desc">${esc(study.description)}</p>` : ''}
      ${capacityBar(study.currentMemberCount, study.capacity)}
    </article>

    <div class="detail-grid">
      <section class="card">
        <h2>참여자<span class="count">${members ? members.length : ''}</span></h2>
        ${members ? memberList(members) : locked}
      </section>
      <section class="card">
        <h2>일정<span class="count">${schedules ? schedules.length : ''}</span></h2>
        ${schedules ? scheduleList(schedules) : locked}
        ${isLeader ? scheduleForm() : ''}
      </section>
    </div>`;
}

render.detail = async () => {
  const root = $('#detail');
  const id = state.studyId;
  try {
    const study = await api(`/api/studies/${id}`);
    let members = null;
    let schedules = null;
    if (state.token) {
      [members, schedules] = await Promise.all([
        api(`/api/studies/${id}/members`),
        api(`/api/studies/${id}/schedules`),
      ]);
    }
    if (state.view === 'detail' && state.studyId === id) {
      root.innerHTML = detailHtml(study, members, schedules);
    }
  } catch (e) {
    root.innerHTML = '<p class="empty">스터디를 불러오지 못했어요.</p>';
    showError(e);
  }
};

/* ---------- 내 스터디 ---------- */
function myStudyCard(s) {
  return `
    <button class="card study-card" data-study="${s.studyId}">
      <div class="study-card__top">${statusBadge(s.status)}${roleBadge(s.myRole)}</div>
      <h3>${esc(s.name)}</h3>
      <p class="muted">${fmtDate(s.joinedAt)} 참여</p>
      ${capacityBar(s.currentMemberCount, s.capacity)}
    </button>`;
}

render.my = async () => {
  if (!requireLogin()) return;
  const grid = $('#myGrid');
  try {
    const list = await api('/api/members/me/studies');
    grid.innerHTML = list.length
      ? list.map(myStudyCard).join('')
      : '<p class="empty">아직 참여한 스터디가 없어요.</p>';
  } catch (e) {
    grid.innerHTML = '';
    showError(e);
  }
};

/* ---------- 내 프로필 ---------- */
render.profile = async () => {
  if (!requireLogin()) return;
  const root = $('#profile');
  try {
    const me = await api('/api/members/me');
    state.me = me;
    renderHeader();
    root.innerHTML = `
      <div class="page-head">
        <div>
          <h1>내 프로필</h1>
          <p class="muted">다른 회원에게는 이름, 사진, 자기소개만 보여요.</p>
        </div>
      </div>
      <div class="profile-grid">
        <article class="card profile-card">
          ${avatarHtml(me, 'lg')}
          <h2>${esc(me.name)}</h2>
          <p class="muted">${esc(me.email)}</p>
          <p class="profile-card__desc">${me.description ? esc(me.description) : '<span class="muted">아직 자기소개가 없어요.</span>'}</p>
          <p class="muted small">${fmtDate(me.createdAt)} 가입</p>
        </article>
        <form id="profileForm" class="card form-card">
          <h2>프로필 수정</h2>
          <label>프로필 이미지 URL<input name="imageUrl" type="text" value="${esc(me.imageUrl ?? '')}" placeholder="https://..."></label>
          <p class="hint">비워서 저장하면 이미지가 삭제돼요.</p>
          <label>자기소개<textarea name="description" rows="5">${esc(me.description ?? '')}</textarea></label>
          <button class="btn btn--primary" type="submit">저장하기</button>
        </form>
      </div>`;
  } catch (e) {
    showError(e);
  }
};

/* ---------- 폼 처리 ---------- */
const formHandlers = {
  loginForm: (d) => login(d.email, d.password),

  signupForm: async (d) => {
    await api('/api/auth/signup', { method: 'POST', body: d });
    toast('회원가입이 완료되었어요.');
    await login(d.email, d.password);
  },

  createForm: async (d, form) => {
    const res = await api('/api/studies', {
      method: 'POST',
      body: {
        name: d.name,
        description: d.description,
        capacity: d.capacity === '' ? null : Number(d.capacity),
      },
    });
    form.reset();
    form.hidden = true;
    toast('스터디가 만들어졌어요.');
    openStudy(res.studyId);
  },

  scheduleForm: async (d) => {
    await api(`/api/studies/${state.studyId}/schedules`, {
      method: 'POST',
      body: {
        title: d.title,
        place: d.place || null,
        startAt: toLocalDateTime(d.startAt),
        endAt: toLocalDateTime(d.endAt),
      },
    });
    toast('일정이 등록되었어요.');
    render.detail();
  },

  profileForm: async (d) => {
    await api('/api/members/me/profile', { method: 'PATCH', body: d });
    toast('프로필이 저장되었어요.');
    render.profile();
  },
};

document.addEventListener('submit', async (e) => {
  const form = e.target;
  const handler = formHandlers[form.id];
  if (!handler) return;
  e.preventDefault();

  const button = form.querySelector('button[type="submit"]');
  if (button) button.disabled = true;
  try {
    await handler(Object.fromEntries(new FormData(form)), form);
  } catch (err) {
    showError(err);
  } finally {
    if (button) button.disabled = false;
  }
});

/* ---------- 클릭 처리 ---------- */
async function runAction(button, fn) {
  button.disabled = true;
  try {
    await fn();
  } catch (err) {
    showError(err);
    button.disabled = false;
  }
}

document.addEventListener('click', async (e) => {
  const t = e.target.closest('[data-nav], [data-study], [data-action], [data-status], #toggleCreate, #prevPage, #nextPage, #logoutBtn');
  if (!t) return;

  if (t.dataset.nav) return go(t.dataset.nav);
  if (t.dataset.study) return openStudy(Number(t.dataset.study));

  if (t.dataset.status !== undefined) {
    state.list = { ...state.list, status: t.dataset.status, page: 0 };
    return render.studies();
  }

  switch (t.id) {
    case 'toggleCreate': {
      const form = $('#createForm');
      form.hidden = !form.hidden;
      if (!form.hidden) form.querySelector('input').focus();
      return;
    }
    case 'prevPage':
      state.list.page = Math.max(0, state.list.page - 1);
      return render.studies();
    case 'nextPage':
      state.list.page += 1;
      return render.studies();
    case 'logoutBtn':
      clearSession();
      toast('로그아웃했어요.');
      return go('studies');
    default:
      break;
  }

  switch (t.dataset.action) {
    case 'back':
      return go(state.prevView || 'studies');
    case 'join':
      return runAction(t, async () => {
        const res = await api(`/api/studies/${state.studyId}/members`, { method: 'POST' });
        toast(`참여 완료! 현재 ${res.currentMemberCount}명이에요.`);
        await render.detail();
      });
    case 'leave':
      if (!window.confirm('이 스터디에서 탈퇴할까요?')) return;
      return runAction(t, async () => {
        await api(`/api/studies/${state.studyId}/members/me`, { method: 'DELETE' });
        toast('스터디에서 탈퇴했어요.');
        await render.detail();
      });
    default:
      break;
  }
});

/* 프로필 이미지 URL이 깨지면 이니셜로 대체 */
document.addEventListener('error', (e) => {
  const img = e.target;
  if (img instanceof HTMLImageElement && img.classList.contains('avatar')) {
    const span = document.createElement('span');
    span.className = img.className;
    span.textContent = img.dataset.initial || '?';
    img.replaceWith(span);
  }
}, true);

/* ---------- 시작 ---------- */
(async () => {
  await loadMe();
  go('studies');
})();
