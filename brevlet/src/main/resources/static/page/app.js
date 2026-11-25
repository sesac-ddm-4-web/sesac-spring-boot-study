(() => {
  'use strict';

  // ---- 서버 규칙과 동일하게 맞춘 상수 ----
  const KEY_CHARS = '23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz';
  const KEY_LENGTH = 8;
  const KEY_RE = new RegExp(`^[${KEY_CHARS}]{${KEY_LENGTH}}$`);
  const URL_RE = /^https?:\/\/(?:[\w-]+(?:\.[\w-]+)*\.[a-zA-Z]{2,}|localhost|(?:\d{1,3}\.){3}\d{1,3})(?::\d{1,5})?(?:[/?#]\S*)?$/;
  const MAX_URL_LENGTH = 2048; // CreateShortenUrlRequest.originalUrl @Size(max = 2048)
  // 만료 기간은 선택 사항이며 기본값은 영구(null). 값을 고르면 해당 일수 후 만료된다.
  const MIN_DAYS = 1;
  const MAX_DAYS = 365;
  const RECENT_KEY = 'brevlet.recent';
  const THEME_KEY = 'brevlet.theme';
  const RECENT_LIMIT = 20;

  const $ = (sel) => document.querySelector(sel);
  // 단축 URL은 현재 페이지에 접속한 호스트(도메인 또는 IP:포트)를 기준으로 만든다
  const { protocol, host } = window.location;
  const shortUrlOf = (key) => `${protocol}//${host}/${key}`;

  // ---- 공통 유틸 ----
  const storage = {
    get(key, fallback) {
      try { const v = localStorage.getItem(key); return v == null ? fallback : JSON.parse(v); }
      catch { return fallback; }
    },
    set(key, value) {
      try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* 저장 불가 환경 무시 */ }
    },
  };

  const dateFmt = new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
  const dateTimeFmt = new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  const numFmt = new Intl.NumberFormat('ko-KR');

  function remainText(expiresAt) {
    if (expiresAt == null) return '만료 없음';
    const ms = new Date(expiresAt).getTime() - Date.now();
    if (ms <= 0) return '만료됨';
    if (ms >= 86400000) return `${Math.round(ms / 86400000)}일 남음`;
    const hours = Math.floor(ms / 3600000);
    if (hours >= 1) return `${hours}시간 남음`;
    return `${Math.max(1, Math.floor(ms / 60000))}분 남음`;
  }

  function statusOf(info) {
    // expiresAt이 null이면 영구 링크이므로 만료되지 않는다 (new Date(null)은 1970년이라 반드시 먼저 거른다)
    if (info.expiresAt != null && new Date(info.expiresAt).getTime() <= Date.now()) return { label: '만료됨', cls: 'badge-warn', alive: false };
    return { label: '사용 중', cls: 'badge-success', alive: true };
  }

  let toastTimer;
  function toast(message, isError = false) {
    const el = $('#toast');
    el.textContent = message;
    el.classList.toggle('is-error', isError);
    el.classList.add('is-show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove('is-show'), 2400);
  }

  async function copyText(text) {
    try {
      await navigator.clipboard.writeText(text);
    } catch {
      const ta = document.createElement('textarea');
      ta.value = text;
      ta.setAttribute('readonly', '');
      ta.style.position = 'fixed';
      ta.style.opacity = '0';
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      ta.remove();
    }
    toast('링크를 복사했어요');
  }

  function setLoading(btn, loading) {
    btn.disabled = loading;
    btn.classList.toggle('is-loading', loading);
  }

  // ---- API ----
  class ApiError extends Error {
    constructor(status, message) { super(message); this.status = status; }
  }

  const FALLBACK_MESSAGES = {
    400: '입력값이 올바르지 않습니다. 다시 확인해 주세요.',
    404: '해당 단축 링크를 찾을 수 없습니다.',
    410: '만료된 링크입니다.',
    500: '서버에 문제가 발생했어요. 잠시 후 다시 시도해 주세요.',
  };

  async function request(path, options = {}) {
    let res;
    try {
      res = await fetch(path, { headers: { Accept: 'application/json', ...(options.headers || {}) }, ...options });
    } catch {
      throw new ApiError(0, '서버에 연결할 수 없어요. 네트워크를 확인해 주세요.');
    }
    const text = await res.text();
    if (!res.ok) {
      let message = '';
      // 서버는 일반 문자열 또는 Spring 기본 오류 JSON을 반환할 수 있음
      try {
        const body = JSON.parse(text);
        message = res.status === 400 ? '' : (body.detail || body.message || '');
      } catch {
        message = text.trim().startsWith('<') ? '' : text.trim();
      }
      throw new ApiError(res.status, message || FALLBACK_MESSAGES[res.status] || `요청에 실패했어요 (${res.status})`);
    }
    return text ? JSON.parse(text) : null;
  }

  const api = {
    create: (originalUrl, expiresInDays) => request('/shorten-url', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      // 영구 링크(null)는 필드를 생략한다
      body: JSON.stringify(expiresInDays == null ? { originalUrl } : { originalUrl, expiresInDays }),
    }),
    info: (key) => request(`/shorten-url/${encodeURIComponent(key)}`),
  };

  // 예전 이름(trimlink.*)으로 저장된 값을 새 키로 한 번만 옮긴다
  [['trimlink.recent', RECENT_KEY], ['trimlink.theme', THEME_KEY]].forEach(([from, to]) => {
    try {
      const v = localStorage.getItem(from);
      if (v != null && localStorage.getItem(to) == null) localStorage.setItem(to, v);
      localStorage.removeItem(from);
    } catch { /* 저장 불가 환경 무시 */ }
  });

  // ---- 테마 ----
  const root = document.documentElement;
  const savedTheme = storage.get(THEME_KEY, null);
  if (savedTheme) root.dataset.theme = savedTheme;
  $('#themeToggle').addEventListener('click', () => {
    const current = root.dataset.theme
      || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    const next = current === 'dark' ? 'light' : 'dark';
    root.dataset.theme = next;
    storage.set(THEME_KEY, next);
  });

  // ---- 탭 ----
  const tabs = document.querySelectorAll('.tab');
  function switchTab(name) {
    tabs.forEach((t) => {
      const active = t.dataset.tab === name;
      t.classList.toggle('is-active', active);
      t.setAttribute('aria-selected', String(active));
    });
    $('#panel-create').hidden = name !== 'create';
    $('#panel-lookup').hidden = name !== 'lookup';
    (name === 'create' ? $('#originalUrl') : $('#lookupInput')).focus();
  }
  tabs.forEach((t) => t.addEventListener('click', () => switchTab(t.dataset.tab)));

  // ---- 만료 기간 ----
  let selectedDays = null; // null: 영구(기본), number: 일수, 'custom': 직접 입력
  const chips = document.querySelectorAll('.chip');
  const customWrap = $('#customDaysWrap');
  const customInput = $('#customDays');

  // 현재 선택을 { valid, days }로 해석한다. days가 null이면 영구 링크.
  function currentExpiry() {
    if (selectedDays === null) return { valid: true, days: null };
    if (selectedDays !== 'custom') return { valid: true, days: selectedDays };
    const n = Number(customInput.value);
    const ok = customInput.value.trim() !== '' && Number.isInteger(n) && n >= MIN_DAYS && n <= MAX_DAYS;
    return { valid: ok, days: ok ? n : null };
  }

  function updateExpiryHint() {
    const { valid, days } = currentExpiry();
    const hint = $('#expiryHint');
    if (!valid) {
      hint.textContent = `${MIN_DAYS}일에서 ${MAX_DAYS}일 사이의 정수를 입력해 주세요.`;
      return;
    }
    if (days === null) {
      hint.textContent = '만료 없이 계속 사용할 수 있어요.';
      return;
    }
    const at = new Date(Date.now() + days * 86400000);
    hint.textContent = `${dateFmt.format(at)}까지 사용할 수 있어요.`;
  }

  chips.forEach((chip) => chip.addEventListener('click', () => {
    chips.forEach((c) => {
      const active = c === chip;
      c.classList.toggle('is-active', active);
      c.setAttribute('aria-checked', String(active));
    });
    const v = chip.dataset.days;
    selectedDays = v === 'custom' ? 'custom' : v === 'none' ? null : Number(v);
    customWrap.hidden = v !== 'custom';
    if (v === 'custom') customInput.focus();
    updateExpiryHint();
  }));
  customInput.addEventListener('input', updateExpiryHint);
  customInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') { e.preventDefault(); $('#createForm').requestSubmit(); }
  });
  updateExpiryHint();

  // ---- 링크 만들기 ----
  const urlInput = $('#originalUrl');
  const urlError = $('#urlError');

  function normalizeUrl(raw) {
    const v = raw.trim();
    if (!v) return v;
    return /^[a-z][a-z\d+.-]*:\/\//i.test(v) ? v : `https://${v}`;
  }

  function showUrlError(msg) {
    urlError.textContent = msg;
    urlInput.setAttribute('aria-invalid', msg ? 'true' : 'false');
  }

  urlInput.addEventListener('input', () => { if (urlError.textContent) showUrlError(''); });

  $('#pasteBtn').addEventListener('click', async () => {
    try {
      const text = await navigator.clipboard.readText();
      if (text) { urlInput.value = text.trim(); showUrlError(''); }
      urlInput.focus();
    } catch {
      toast('클립보드 접근이 허용되지 않았어요. 직접 붙여넣어 주세요.', true);
      urlInput.focus();
    }
  });

  $('#createForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const raw = urlInput.value;
    if (!raw.trim()) { showUrlError('단축할 URL을 입력해 주세요.'); urlInput.focus(); return; }
    const url = normalizeUrl(raw);
    // 자동으로 붙인 https://까지 포함한 실제 전송 길이로 검사한다
    if (url.length > MAX_URL_LENGTH) {
      showUrlError(`URL이 너무 길어요. ${numFmt.format(MAX_URL_LENGTH)}자 이하로 입력해 주세요. (현재 ${numFmt.format(url.length)}자)`);
      urlInput.focus();
      return;
    }
    if (!URL_RE.test(url)) {
      showUrlError('올바른 URL 형식이 아니에요. http:// 또는 https://로 시작하는 주소를 입력해 주세요.');
      urlInput.focus();
      return;
    }
    const { valid, days } = currentExpiry();
    if (!valid) { customInput.focus(); toast(`만료 기간은 ${MIN_DAYS}~${MAX_DAYS}일 사이로 입력해 주세요.`, true); return; }
    showUrlError('');

    const btn = $('#createBtn');
    setLoading(btn, true);
    try {
      const data = await api.create(url, days);
      urlInput.value = url;
      showResult(data);
      addRecent(data);
    } catch (err) {
      if (err.status === 400) showUrlError(err.message);
      else toast(err.message, true);
    } finally {
      setLoading(btn, false);
    }
  });

  function showResult(data) {
    const link = shortUrlOf(data.shortenUrlKey);
    $('#resultShort').textContent = link.replace(/^https?:\/\//, '');
    $('#resultShort').href = link;
    $('#resultOpen').href = link;
    $('#resultOrigin').textContent = data.originalUrl;
    $('#resultExpiry').textContent = data.expiresAt == null
      ? '만료 없음 · 영구 링크'
      : `${dateFmt.format(new Date(data.expiresAt))} 만료 · ${remainText(data.expiresAt)}`;
    $('#resultCopy').onclick = () => copyText(link);
    $('#result').hidden = false;
    $('#resultCopy').focus({ preventScroll: true });
    $('#result').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  // ---- 링크 조회 ----
  const lookupInput = $('#lookupInput');
  const lookupError = $('#lookupError');
  let currentInfo = null;

  function extractKey(raw) {
    const v = raw.trim();
    if (KEY_RE.test(v)) return v;
    try {
      const u = new URL(/^https?:\/\//i.test(v) ? v : `https://${v}`);
      const seg = u.pathname.split('/').filter(Boolean).pop() || '';
      return KEY_RE.test(seg) ? seg : null;
    } catch {
      return null;
    }
  }

  lookupInput.addEventListener('input', () => {
    lookupError.textContent = '';
    lookupInput.setAttribute('aria-invalid', 'false');
  });

  async function lookup(key) {
    const btn = $('#lookupBtn');
    setLoading(btn, true);
    try {
      const data = await api.info(key);
      renderInfo(data);
      updateRecent(data);
      return data;
    } catch (err) {
      $('#info').hidden = true;
      lookupError.textContent = err.message;
      lookupInput.setAttribute('aria-invalid', 'true');
      if (err.status === 404) markRecentMissing(key);
      return null;
    } finally {
      setLoading(btn, false);
    }
  }

  $('#lookupForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const raw = lookupInput.value;
    if (!raw.trim()) { lookupError.textContent = '조회할 단축 링크나 키를 입력해 주세요.'; lookupInput.focus(); return; }
    const key = extractKey(raw);
    if (!key) {
      lookupError.textContent = `단축 키는 ${KEY_LENGTH}자리 영문·숫자 조합이에요. 링크를 다시 확인해 주세요.`;
      lookupInput.setAttribute('aria-invalid', 'true');
      return;
    }
    lookup(key);
  });

  function renderInfo(data) {
    currentInfo = data;
    const link = shortUrlOf(data.shortenUrlKey);
    const st = statusOf(data);
    $('#infoShort').textContent = link.replace(/^https?:\/\//, '');
    $('#infoShort').href = link;
    const badge = $('#infoStatus');
    badge.textContent = st.label;
    badge.className = `badge ${st.cls}`;
    $('#infoClicks').textContent = `${numFmt.format(data.redirectCount ?? 0)}회`;
    const permanent = data.expiresAt == null;
    $('#infoExpires').textContent = permanent ? '없음 (영구)' : dateFmt.format(new Date(data.expiresAt));
    $('#infoExpires').title = permanent ? '' : dateTimeFmt.format(new Date(data.expiresAt));
    $('#infoRemain').textContent = remainText(data.expiresAt);
    $('#infoOrigin').textContent = data.originalUrl;
    $('#infoOrigin').href = data.originalUrl;
    $('#info').hidden = false;
  }

  $('#infoCopy').addEventListener('click', () => currentInfo && copyText(shortUrlOf(currentInfo.shortenUrlKey)));
  $('#infoRefresh').addEventListener('click', () => currentInfo && lookup(currentInfo.shortenUrlKey).then((d) => d && toast('최신 정보로 갱신했어요')));

  function openLookup(key) {
    switchTab('lookup');
    lookupInput.value = shortUrlOf(key);
    lookup(key);
    $('.main-card').scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  // ---- 최근 만든 링크 (이 브라우저에만 저장) ----
  let recent = storage.get(RECENT_KEY, []);
  if (!Array.isArray(recent)) recent = [];
  // 삭제 기능이 있던 시절에 저장된 deletedAt 값은 더 이상 쓰지 않으므로 정리한다
  recent = recent.filter((r) => r && typeof r === 'object');
  recent.forEach((r) => { delete r.deletedAt; });

  function saveRecent() { storage.set(RECENT_KEY, recent.slice(0, RECENT_LIMIT)); }

  function addRecent(data) {
    recent = [{ ...data, createdAt: new Date().toISOString() }, ...recent.filter((r) => r.shortenUrlKey !== data.shortenUrlKey)].slice(0, RECENT_LIMIT);
    saveRecent();
    renderRecent();
  }

  function updateRecent(data) {
    const idx = recent.findIndex((r) => r.shortenUrlKey === data.shortenUrlKey);
    if (idx < 0) return;
    recent[idx] = { ...recent[idx], ...data, missing: false };
    saveRecent();
    renderRecent();
  }

  function markRecentMissing(key) {
    const item = recent.find((r) => r.shortenUrlKey === key);
    if (item) { item.missing = true; saveRecent(); renderRecent(); }
  }

  function el(tag, attrs = {}, children = []) {
    const node = document.createElement(tag);
    Object.entries(attrs).forEach(([k, v]) => {
      if (k === 'class') node.className = v;
      else if (k === 'text') node.textContent = v;
      else if (k.startsWith('on')) node.addEventListener(k.slice(2), v);
      else node.setAttribute(k, v);
    });
    children.forEach((c) => c && node.appendChild(c));
    return node;
  }

  function renderRecent() {
    const list = $('#recentList');
    list.replaceChildren();
    $('#recentSection').hidden = recent.length === 0;
    recent.forEach((item) => {
      const link = shortUrlOf(item.shortenUrlKey);
      const st = item.missing ? { label: '찾을 수 없음', cls: 'badge-danger', alive: false } : statusOf(item);
      const li = el('li', { class: `recent-item${st.alive ? '' : ' is-dead'}` }, [
        el('div', { class: 'recent-main' }, [
          el('a', { class: 'recent-short', href: link, target: '_blank', rel: 'noopener', text: link.replace(/^https?:\/\//, '') }),
          el('div', { class: 'recent-origin', title: item.originalUrl, text: item.originalUrl }),
          el('div', { class: 'recent-meta' }, [
            el('span', { class: `badge ${st.cls}`, text: st.label }),
            el('span', { text: `클릭 ${numFmt.format(item.redirectCount ?? 0)}회` }),
            st.alive ? el('span', { text: remainText(item.expiresAt) }) : null,
          ]),
        ]),
        el('div', { class: 'recent-actions' }, [
          st.alive ? el('button', { class: 'btn btn-ghost', type: 'button', text: '복사', onclick: () => copyText(link) }) : null,
          el('button', { class: 'btn btn-ghost', type: 'button', text: '상세', onclick: () => openLookup(item.shortenUrlKey) }),
          el('button', {
            class: 'btn btn-ghost', type: 'button', text: '숨기기', title: '목록에서만 숨깁니다',
            onclick: () => { recent = recent.filter((r) => r.shortenUrlKey !== item.shortenUrlKey); saveRecent(); renderRecent(); },
          }),
        ]),
      ]);
      list.appendChild(li);
    });
  }

  $('#clearRecent').addEventListener('click', () => {
    recent = [];
    saveRecent();
    renderRecent();
    toast('목록을 지웠어요. 링크는 그대로 사용할 수 있어요.');
  });

  async function refreshRecent() {
    await Promise.all(recent.map(async (item) => {
      try {
        const data = await api.info(item.shortenUrlKey);
        Object.assign(item, data, { missing: false });
      } catch (err) {
        if (err.status === 404) item.missing = true;
      }
    }));
    saveRecent();
    renderRecent();
  }

  // ---- 초기화 ----
  $('#year').textContent = new Date().getFullYear();
  renderRecent();
  if (recent.length) refreshRecent();
  urlInput.focus();
})();
