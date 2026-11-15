const API_URL = '/api/shorten-urls';

const FIELD_LABELS = {
    originalUrl: '원본 URL',
    channel: '채널',
};

// ===== API 호출 =====

class ApiError extends Error {
    constructor(body) {
        const fieldErrors = body?.errors ?? [];
        const message = fieldErrors.length > 0
            ? fieldErrors.map(e => `${FIELD_LABELS[e.field] ?? e.field}: ${e.reason}`).join('\n')
            : body?.message ?? '요청에 실패했습니다. 잠시 후 다시 시도해주세요.';
        super(message);
    }
}

async function request(url, options = {}) {
    const response = await fetch(url, options);
    const body = await response.json().catch(() => null);
    if (!response.ok) {
        throw new ApiError(body);
    }
    return body;
}

const createShortenUrl = (originalUrl, channel) => request(API_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ originalUrl, channel }),
});

const getShortenUrls = (originalUrl) => {
    const query = originalUrl ? `?originalUrl=${encodeURIComponent(originalUrl)}` : '';
    return request(API_URL + query);
};

// ===== 화면 도우미 =====

const $ = (id) => document.getElementById(id);

function showError(element, message) {
    element.textContent = message;
    element.hidden = false;
}

function hideError(element) {
    element.hidden = true;
}

function cell(text, className) {
    const td = document.createElement('td');
    td.textContent = text;
    if (className) td.className = className;
    return td;
}

function linkCell(url) {
    const td = document.createElement('td');
    const a = document.createElement('a');
    a.href = url;
    a.textContent = url;
    a.target = '_blank';
    a.rel = 'noopener';
    td.appendChild(a);
    return td;
}

function formatDate(dateTime) {
    return new Date(dateTime).toLocaleString('ko-KR', {
        month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
    });
}

// ===== 1. 단축 URL 만들기 =====

$('create-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const button = event.submitter;
    const originalUrl = $('original-url').value.trim();
    const channel = $('channel').value.trim();

    hideError($('create-error'));
    $('create-result').hidden = true;
    button.disabled = true;

    try {
        const result = await createShortenUrl(originalUrl, channel);
        $('result-link').href = result.shortenUrl;
        $('result-link').textContent = result.shortenUrl;
        $('create-result').hidden = false;
        $('channel').value = '';

        $('compare-url').value = result.originalUrl;
        await Promise.all([loadCompare(result.originalUrl), loadRecent()]);
    } catch (error) {
        showError($('create-error'), error.message);
    } finally {
        button.disabled = false;
    }
});

$('copy-button').addEventListener('click', async () => {
    const url = $('result-link').textContent;
    try {
        await navigator.clipboard.writeText(url);
        $('copy-button').textContent = '복사됨';
        setTimeout(() => { $('copy-button').textContent = '복사'; }, 1500);
    } catch {
        window.prompt('아래 주소를 복사해서 사용하세요.', url);
    }
});

// ===== 2. 채널별 클릭 비교 =====

$('compare-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const originalUrl = $('compare-url').value.trim();
    if (!originalUrl) {
        showError($('compare-error'), '비교할 원본 URL을 입력하세요.');
        return;
    }
    await loadCompare(originalUrl);
});

async function loadCompare(originalUrl) {
    hideError($('compare-error'));
    try {
        const { totalRedirectCount, items } = await getShortenUrls(originalUrl);
        renderCompare(totalRedirectCount, items);
    } catch (error) {
        showError($('compare-error'), error.message);
    }
}

function groupByChannel(items) {
    const groups = new Map();
    items.forEach(item => {
        const group = groups.get(item.channel) ?? { channel: item.channel, linkCount: 0, redirectCount: 0 };
        group.linkCount += 1;
        group.redirectCount += item.redirectCount;
        groups.set(item.channel, group);
    });
    return [...groups.values()].sort((a, b) => b.redirectCount - a.redirectCount);
}

function renderCompare(total, items) {
    const tbody = $('compare-table').querySelector('tbody');
    tbody.replaceChildren();

    if (items.length === 0) {
        $('compare-summary').textContent = '이 URL로 만든 단축 링크가 없어요.';
        $('compare-summary').hidden = false;
        $('compare-table').hidden = true;
        return;
    }

    const groups = groupByChannel(items);
    $('compare-summary').textContent =
        `채널 ${groups.length}개 · 링크 ${items.length}개 · 총 클릭 ${total.toLocaleString()}회`;
    $('compare-summary').hidden = false;

    const maxCount = Math.max(...groups.map(group => group.redirectCount), 1);

    groups.forEach(group => {
        const tr = document.createElement('tr');
        tr.appendChild(cell(group.channel));
        tr.appendChild(cell(group.linkCount.toLocaleString(), 'num'));
        tr.appendChild(cell(group.redirectCount.toLocaleString(), 'num'));

        const barTd = document.createElement('td');
        const track = document.createElement('div');
        track.className = 'bar-track';
        const fill = document.createElement('div');
        fill.className = 'bar-fill';
        fill.style.width = `${(group.redirectCount / maxCount) * 100}%`;
        track.appendChild(fill);
        barTd.appendChild(track);
        tr.appendChild(barTd);

        tbody.appendChild(tr);
    });

    $('compare-table').hidden = false;
}
// ===== 3. 최근 만든 링크 =====

$('refresh-button').addEventListener('click', loadRecent);

async function loadRecent() {
    try {
        const { items } = await getShortenUrls();
        renderRecent(items);
    } catch (error) {
        $('recent-empty').textContent = error.message;
        $('recent-empty').hidden = false;
    }
}

function renderRecent(items) {
    const tbody = $('recent-table').querySelector('tbody');
    tbody.replaceChildren();
    $('recent-empty').textContent = '아직 만든 링크가 없어요.';
    $('recent-empty').hidden = items.length > 0;
    $('recent-table').hidden = items.length === 0;

    items.forEach(item => {
        const tr = document.createElement('tr');
        tr.appendChild(cell(item.channel));
        tr.appendChild(linkCell(item.shortenUrl));
        const original = cell(item.originalUrl, 'truncate');
        original.title = item.originalUrl;
        tr.appendChild(original);
        tr.appendChild(cell(item.redirectCount.toLocaleString(), 'num'));
        tr.appendChild(cell(formatDate(item.createdAt)));
        tbody.appendChild(tr);
    });
}

loadRecent();