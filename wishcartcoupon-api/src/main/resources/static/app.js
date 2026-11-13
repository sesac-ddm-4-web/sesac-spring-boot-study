'use strict';

/* ------------------------------------------------------------------
 * 공통 도우미
 * ------------------------------------------------------------------ */

const $ = (selector) => document.querySelector(selector);

// 요소를 만드는 도우미. 문자열 자식은 항상 "텍스트"로 들어가므로
// 서버나 외부 API가 보낸 값에 HTML이 섞여 있어도 실행되지 않는다(XSS 방지).
function el(tag, attrs = {}, ...children) {
    const node = document.createElement(tag);
    for (const [key, value] of Object.entries(attrs)) {
        if (key === 'class') node.className = value;
        else if (key.startsWith('on')) node.addEventListener(key.slice(2), value);
        else node.setAttribute(key, value);
    }
    node.append(...children.filter((child) => child !== null && child !== undefined));
    return node;
}

const won = (amount) => `${Number(amount).toLocaleString('ko-KR')}원`;

const COUPON_LABELS = {
    NONE: '쿠폰 없음',
    PERCENT10: '10% 할인',
    FIXED15000: '15,000원 할인',
};

let messageTimer;
function showMessage(text, type = 'info') {
    const box = $('#message');
    box.textContent = text;
    box.className = type;
    box.hidden = false;
    clearTimeout(messageTimer);
    messageTimer = setTimeout(() => { box.hidden = true; }, 5000);
}

// 서버 호출. 실패하면 서버가 보낸 오류 메시지를 담은 Error를 던진다.
async function api(path, options = {}) {
    let response;
    try {
        response = await fetch(path, {
            headers: { 'Content-Type': 'application/json' },
            ...options,
        });
    } catch {
        throw new Error('서버에 연결할 수 없습니다.');
    }

    const text = await response.text();            // 204처럼 본문이 없을 수 있다
    let body = null;
    if (text) {
        try { body = JSON.parse(text); } catch { body = null; }
    }
    if (!response.ok) {
        throw new Error(`[${response.status}] ${errorText(response, body)}`);
    }
    return body;
}

function errorText(response, body) {
    if (body && Array.isArray(body.errors)) return body.errors.join(' / ');  // 우리 서버의 오류 형식
    if (body && body.error) return body.error;                                // Spring 기본 오류 형식
    return response.statusText || '요청에 실패했습니다.';
}

// 화면 이벤트에서 호출하는 작업은 이걸로 감싸서 오류를 메시지로 보여 준다.
async function guard(task) {
    try {
        await task();
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

/* ------------------------------------------------------------------
 * ① 외부 상품 (1단계 API)
 * ------------------------------------------------------------------ */

const ext = { q: '', skip: 0, limit: 8 };

// 카테고리 칩. 누르면 그 이름으로 검색한다. 상품 목록을 바꾸면 여기만 고치면 된다.
const CATEGORIES = [
    { label: '전체', q: '', icon: '🏀', tile: '#fde4d0' },
    { label: '농구공', q: '농구공', icon: '🏀', tile: '#fde4d0' },
    { label: '농구화', q: '농구화', icon: '👟', tile: '#dde8fb' },
    { label: '의류', q: '의류', icon: '👕', tile: '#e6f3e1' },
    { label: '보호대', q: '보호대', icon: '🩹', tile: '#f6e3ef' },
    { label: '가방', q: '가방', icon: '🎒', tile: '#efe8d8' },
    { label: '훈련용품', q: '훈련용품', icon: '🎯', tile: '#e1f0f3' },
    { label: '골대', q: '골대', icon: '🥅', tile: '#ece4f7' },
];
const lookCategory = (name) => CATEGORIES.find((c) => c.label === name) || CATEGORIES[0];

function renderChips() {
    const nav = $('#chips');
    nav.replaceChildren(...CATEGORIES.map((c) =>
        el('button', {
            type: 'button',
            class: 'chip',
            'aria-pressed': String(c.q === ext.q),
            onclick: () => {
                ext.q = c.q;
                $('#search-q').value = c.q;
                guard(() => loadExternal(true));
            },
        }, c.label)));
}

async function loadExternal(reset) {
    const list = $('#external-list');
    if (reset) {
        ext.skip = 0;
        list.replaceChildren();
        renderChips();
        $('#external-title').textContent = ext.q ? `'${ext.q}' 검색 결과` : '전체 상품';
    }

    const params = new URLSearchParams({ limit: ext.limit, skip: ext.skip });
    let path = '/external-products';
    if (ext.q) {
        path = '/external-products/search';
        params.set('q', ext.q);
    }

    const products = await api(`${path}?${params}`);
    products.forEach((product) => list.append(externalCard(product)));

    ext.skip += ext.limit;                              // 다음 페이지: skip을 limit만큼 늘린다
    $('#more-btn').hidden = products.length < ext.limit; // limit보다 적게 왔으면 마지막 페이지
    $('#result-info').textContent = `${list.querySelectorAll('.product').length}개 표시 중`;
    if (reset && products.length === 0) {
        list.append(el('li', { class: 'empty' }, '조건에 맞는 상품이 없습니다.'));
        $('#result-info').textContent = '';
    }
}

function externalCard(product) {
    const soldOut = product.stock === 0;
    const low = product.stock > 0 && product.stock <= 5;
    const look = lookCategory(product.category);
    const hasImage = typeof product.thumbnail === 'string' && product.thumbnail.startsWith('https://');
    // 사진 주소가 있으면 사진, 없으면 카테고리별 아이콘 타일
    const thumb = hasImage
        ? el('div', { class: 'thumb' }, el('img', { src: product.thumbnail, alt: '', loading: 'lazy' }))
        : el('div', { class: 'thumb', style: `--tile:${look.tile}`, 'aria-hidden': 'true' }, look.icon);
    const stockText = soldOut ? '품절' : low ? `품절 임박 · ${product.stock}개` : `재고 ${product.stock}`;

    return el('li', { class: soldOut ? 'product soldout' : 'product' },
        thumb,
        el('div', { class: 'info' },
            el('span', { class: 'category' }, product.category),
            el('strong', {}, product.title),
            el('span', { class: 'price' }, won(product.priceWon), el('small', {}, `$${product.price}`)),
            el('span', { class: soldOut ? 'badge danger' : low ? 'badge low' : 'badge' }, stockText)),
        el('button', { type: 'button', onclick: () => guard(() => importWish(product.id)) }, '♡ 위시로 가져오기'));
}

async function importWish(externalId) {
    const wish = await api('/wish/import', {
        method: 'POST',
        body: JSON.stringify({ externalId }),
    });
    showMessage(`"${wish.name}"을(를) 위시리스트로 가져왔습니다.`, 'success');
    await loadWishes();
}

/* ------------------------------------------------------------------
 * ② 위시리스트
 * ------------------------------------------------------------------ */

async function loadWishes() {
    const wishes = await api('/wishes');
    const list = $('#wish-list');
    list.replaceChildren();
    if (wishes.length === 0) {
        list.append(el('li', { class: 'empty' }, '위시리스트가 비어 있습니다.'));
    }
    wishes.forEach((wish) => list.append(wishCard(wish)));
}

function qtySelect(current) {
    const select = el('select', { 'aria-label': '수량' });
    for (let n = 1; n <= 5; n += 1) {
        const option = el('option', { value: String(n) }, String(n));
        if (n === current) option.selected = true;
        select.append(option);
    }
    return select;
}

function wishCard(wish) {
    const quantity = qtySelect(1);
    return el('li', { class: 'card' },
        el('div', { class: 'info' },
            el('strong', {}, wish.name),
            el('span', {}, won(wish.price))),
        el('div', { class: 'actions' },
            quantity,
            el('button', { type: 'button', onclick: () => guard(() => addToCart(wish.id, Number(quantity.value))) }, '장바구니 담기'),
            el('button', { type: 'button', class: 'ghost', onclick: () => guard(() => deleteWish(wish.id)) }, '삭제')));
}

async function createWish() {
    const name = $('#wish-name').value;
    const price = Number($('#wish-price').value);
    await api('/wish', { method: 'POST', body: JSON.stringify({ name, price }) });
    $('#wish-form').reset();
    showMessage('위시를 등록했습니다.', 'success');
    await loadWishes();
}

async function deleteWish(id) {
    await api(`/wish/${id}`, { method: 'DELETE' });
    showMessage('위시를 삭제했습니다. (장바구니에 담은 항목은 그대로 유지됩니다)', 'success');
    await loadWishes();
}

/* ------------------------------------------------------------------
 * ③ 장바구니 (2단계 API) + 쿠폰 추천 (3단계 API)
 * ------------------------------------------------------------------ */

async function addToCart(wishId, quantity) {
    await api('/cart/items', { method: 'POST', body: JSON.stringify({ wishId, quantity }) });
    showMessage('장바구니에 담았습니다.', 'success');
    await refreshCart();
}

async function changeQuantity(itemId, quantity) {
    try {
        await api(`/cart/items/${itemId}`, { method: 'PATCH', body: JSON.stringify({ quantity }) });
    } finally {
        await refreshCart();   // 성공이든 실패든 서버가 가진 실제 값으로 화면을 다시 맞춘다
    }
}

async function removeCartItem(itemId) {
    await api(`/cart/items/${itemId}`, { method: 'DELETE' });
    await refreshCart();
}

async function refreshCart() {
    const items = await api('/cart/items');
    renderCartItems(items);
    await Promise.all([refreshPreview(), refreshRecommendation()]);
}

function renderCartItems(items) {
    const list = $('#cart-list');
    list.replaceChildren();
    if (items.length === 0) {
        list.append(el('li', { class: 'empty' }, '장바구니가 비어 있습니다.'));
        return;
    }
    items.forEach((item) => {
        const quantity = qtySelect(item.quantity);
        quantity.addEventListener('change', () => guard(() => changeQuantity(item.id, Number(quantity.value))));
        list.append(el('li', { class: 'card' },
            el('div', { class: 'info' },
                el('strong', {}, item.name),
                el('span', { class: 'meta' }, `${won(item.price)} × ${item.quantity} = ${won(item.lineAmount)}`),
                item.stock === null ? null : el('span', { class: 'meta' }, `재고 ${item.stock}`)),
            el('div', { class: 'actions' },
                quantity,
                el('button', { type: 'button', class: 'ghost', onclick: () => guard(() => removeCartItem(item.id)) }, '삭제'))));
    });
}

// 선택한 쿠폰으로 금액 미리보기 (저장하지 않는 조회)
async function refreshPreview() {
    const coupon = $('#coupon').value;
    try {
        renderSummary(await api(`/cart/preview?coupon=${encodeURIComponent(coupon)}`));
    } catch (error) {
        if (coupon === 'NONE') throw error;
        showMessage(error.message, 'error');   // 예: 소계가 부족해 쓸 수 없는 쿠폰
        $('#coupon').value = 'NONE';           // '쿠폰 없음'으로 되돌려 다시 계산
        renderSummary(await api('/cart/preview?coupon=NONE'));
    }
}

function summaryRow(label, value, isTotal = false) {
    const cls = isTotal ? 'total' : '';
    return [el('dt', { class: cls }, label), el('dd', { class: cls }, value)];
}

function renderSummary(preview) {
    $('#summary').replaceChildren(
        ...summaryRow('소계', won(preview.subtotal)),
        ...summaryRow('쿠폰 할인', preview.discount > 0 ? `-${won(preview.discount)}` : won(0)),
        ...summaryRow('할인 후 금액', won(preview.discountedSubtotal)),
        ...summaryRow('배송비', preview.shipping === 0 ? '무료' : won(preview.shipping)),
        ...summaryRow('최종 결제 금액', won(preview.finalAmount), true));
}

// 쓸 수 있는 쿠폰 중 최종 금액이 가장 낮은 쿠폰 추천
async function refreshRecommendation() {
    const recommendation = await api('/cart/coupon-recommendation');
    const best = recommendation.candidates.find((c) => c.coupon === recommendation.recommendedCoupon);
    const box = $('#recommendation');
    box.dataset.coupon = recommendation.recommendedCoupon;
    box.replaceChildren(
        el('p', {}, `추천 쿠폰: ${COUPON_LABELS[recommendation.recommendedCoupon]} (최종 ${won(best.finalAmount)})`),
        el('ul', {}, ...recommendation.candidates.map((candidate) =>
            el('li', { class: candidate.coupon === recommendation.recommendedCoupon ? 'best' : '' },
                `${COUPON_LABELS[candidate.coupon]}: ${won(candidate.finalAmount)}`))),
        el('button', { type: 'button', onclick: () => guard(applyRecommended) }, '추천 쿠폰 적용'));
}

async function applyRecommended() {
    $('#coupon').value = $('#recommendation').dataset.coupon;
    await refreshPreview();
}

/* ------------------------------------------------------------------
 * 이벤트 연결과 첫 화면
 * ------------------------------------------------------------------ */

$('#search-form').addEventListener('submit', (event) => {
    event.preventDefault();
    ext.q = $('#search-q').value.trim();
    guard(() => loadExternal(true));
});
$('#more-btn').addEventListener('click', () => guard(() => loadExternal(false)));
$('#wish-form').addEventListener('submit', (event) => {
    event.preventDefault();
    guard(createWish);
});
$('#coupon').addEventListener('change', () => guard(refreshPreview));

guard(() => loadExternal(true));
guard(loadWishes);
guard(refreshCart);