'use strict';

const $ = (selector) => document.querySelector(selector);
const format = new Intl.NumberFormat('ko-KR');
const won = (value) => `${format.format(value)}원`;
const state = {
  tab: 'collection', collection: [], external: [], cart: [],
  collectionView: { url: '/item', description: '내가 모아둔 상품을 장바구니에 담아보세요.' },
  externalQuery: '', skip: 0, total: 0, limit: 8, externalLoaded: false,
  loading: { collection: false, external: false }, errors: { collection: '', external: '' },
  sequence: { collection: 0, external: 0, cart: 0, summary: 0 },
  cartLoading: false, cartLoaded: false, mutating: false, imported: new Set()
};
let toastTimer;

function element(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

function showError(selector, message = '') {
  $(selector).textContent = message;
  $(selector).hidden = !message;
}

function notify(message, error = false) {
  clearTimeout(toastTimer);
  $('#feedback').textContent = message;
  $('#feedback').classList.toggle('error', error);
  $('#feedback').hidden = false;
  toastTimer = setTimeout(() => { $('#feedback').hidden = true; }, error ? 10000 : 4500);
}

async function request(url, { method = 'GET', body } = {}) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  try {
    const response = await fetch(url, {
      method, signal: controller.signal,
      ...(body !== undefined ? { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) } : {})
    });
    const data = await response.json().catch(() => null);
    if (!response.ok) {
      const message = Array.isArray(data?.errors) ? data.errors.join('\n') : data?.detail || data?.message;
      throw new Error(message || `요청을 처리하지 못했어요. (HTTP ${response.status})`);
    }
    return data;
  } catch (error) {
    if (error.name === 'AbortError') throw new Error('응답이 늦어지고 있어요. 잠시 후 다시 시도해 주세요.');
    if (error instanceof TypeError) throw new Error('서버에 연결할 수 없어요. 연결 상태를 확인하고 다시 시도해 주세요.');
    throw error;
  } finally {
    clearTimeout(timeout);
  }
}

function integer(input, min = 0, max = 2147483647) {
  const value = Number(input.value);
  if (!input.value.trim() || !Number.isSafeInteger(value) || value < min || value > max) {
    throw new Error(`${format.format(min)}~${format.format(max)} 사이의 정수를 입력해 주세요.`);
  }
  return value;
}

function mutationButton(label, className, disabled = false) {
  const button = element('button', className, label);
  button.type = 'button';
  button.dataset.mutation = '';
  button.dataset.disabled = String(disabled);
  return button;
}

function updateControls() {
  document.querySelectorAll('[data-mutation]').forEach((button) => {
    button.disabled = !button.hasAttribute('data-keep-enabled')
      && (state.mutating || state.cartLoading || button.dataset.disabled === 'true');
  });
  $('#coupon').disabled = state.mutating || state.cartLoading || !state.cartLoaded;
  $('#refresh-cart').disabled = state.mutating || state.cartLoading;
}

async function mutate(task, onError = (error) => notify(error.message, true)) {
  if (state.mutating || state.cartLoading) return;
  state.mutating = true;
  updateControls();
  try { await task(); }
  catch (error) { onError(error); }
  finally { state.mutating = false; updateControls(); }
}

function renderList() {
  const external = state.tab === 'external';
  const items = external ? state.external : state.collection;
  const container = $('#items');
  container.replaceChildren();
  container.setAttribute('aria-busy', String(state.loading[state.tab]));
  $('#pagination').hidden = !external || state.loading.external || !!state.errors.external || state.total === 0;
  $('#view-description').textContent = external
    ? (state.externalQuery ? `“${state.externalQuery}” 검색 결과 · 컬렉션에 새 취향을 더해보세요.` : '아직 만나지 못한 상품에서 새로운 취향을 발견해 보세요.')
    : state.collectionView.description;
  $('#show-all').classList.toggle('active', state.collectionView.url === '/item');
  showError('#list-error', state.errors[state.tab]);
  $('#result-count').textContent = state.loading[state.tab] || state.errors[state.tab] ? ''
    : external ? `전체 ${format.format(state.total)}개` : `${items.length}개 상품`;
  if (state.loading[state.tab]) {
    container.append(element('p', 'loading', '취향을 불러오는 중이에요…'));
    return;
  }
  if (state.errors[state.tab]) {
    const empty = element('div', 'empty');
    empty.append(element('span', '', '↻'), element('h3', '', '상품을 불러오지 못했어요'),
      element('p', '', '연결 상태를 확인한 뒤 다시 시도해 주세요.'));
    const retry = element('button', 'outline-button', '다시 불러오기');
    retry.type = 'button';
    retry.addEventListener('click', refreshList);
    empty.append(retry);
    container.append(empty);
    return;
  }
  if (!items.length) {
    const empty = element('div', 'empty');
    empty.append(element('span', '', '◇'), element('h3', '', '아직 이곳은 비어 있어요'),
      element('p', '', external ? '다른 검색어로 새로운 상품을 찾아보세요.' : '새 상품을 등록하거나 다른 조건으로 찾아보세요.'));
    container.append(empty);
    return;
  }
  items.forEach((item) => container.append(productCard(item, external)));
  $('#page-info').textContent = `${Math.floor(state.skip / state.limit) + 1} / ${Math.max(1, Math.ceil(state.total / state.limit))}`;
  $('#previous-page').disabled = state.skip === 0;
  $('#next-page').disabled = state.skip + state.limit >= state.total;
  updateControls();
}

function productCard(item, external) {
  const card = element('article', 'item-card');
  const top = element('div', 'card-top');
  top.append(element('span', 'item-number', `${external ? 'FIND' : 'NO.'} ${String(item.id).padStart(3, '0')}`));
  const symbol = element('span', 'item-symbol', external ? '✳' : '✧');
  symbol.setAttribute('aria-hidden', 'true');
  top.append(symbol);
  const name = item.name ?? item.title;
  const price = element('p', 'item-price', format.format(external ? Math.round(item.price * 1000) : item.price));
  price.append(element('small', '', '원'));
  const stock = element('p', `stock${item.stock === 0 ? ' sold-out' : ''}`,
    external ? `등록 예상가 · 외부 가격 ${item.price} × 1,000\n재고 ${format.format(item.stock)}개`
      : item.stock === null ? '직접 등록한 상품 · 재고 제한 없음' : `외부에서 모아온 상품 · 재고 ${format.format(item.stock)}개`);
  stock.style.whiteSpace = 'pre-line';
  card.append(top, element('h3', '', name), price, stock);
  const actions = element('div', 'card-actions');
  if (external) {
    const imported = state.imported.has(item.id);
    const button = mutationButton(imported ? '컬렉션에 등록됨 ✓' : '컬렉션에 등록 ＋', 'card-add', imported);
    button.setAttribute('aria-label', `${name} 컬렉션에 등록`);
    button.addEventListener('click', () => mutate(async () => {
      await request(`/item/external/${item.id}`, { method: 'POST' });
      state.imported.add(item.id);
      await loadCollection({ url: '/item', description: '내가 모아둔 상품을 장바구니에 담아보세요.' });
      renderList();
      notify('컬렉션에 등록했어요. ‘모아둔 상품’에서 장바구니에 담아보세요.');
    }));
    actions.append(button);
  } else {
    const select = element('select', 'quantity-select');
    select.setAttribute('aria-label', `${name} 담을 수량`);
    for (let amount = 1; amount <= 5; amount++) {
      const option = element('option', '', `${amount}개`);
      option.value = amount;
      select.append(option);
    }
    const button = $('#cart-add-button').content.firstElementChild.cloneNode(true);
    button.setAttribute('aria-label', `${name} 장바구니에 담기`);
    button.addEventListener('click', () => mutate(async () => {
      await request('/shopping-cart', { method: 'POST', body: { collectionItemId: item.id, amount: Number(select.value) } });
      await loadCart();
      notify('좋아하는 상품을 장바구니에 담았어요.');
    }));
    actions.append(select, button);
  }
  card.append(actions);
  return card;
}

async function loadCollection(view = state.collectionView) {
  const sequence = ++state.sequence.collection;
  state.collectionView = view;
  state.loading.collection = true;
  state.errors.collection = '';
  if (state.tab === 'collection') renderList();
  try {
    const data = await request(view.url);
    if (sequence !== state.sequence.collection) return;
    state.collection = Array.isArray(data) ? data : [data];
    if (view.url === '/item') $('#collection-count').textContent = state.collection.length;
  } catch (error) {
    if (sequence !== state.sequence.collection) return;
    state.errors.collection = error.message;
  } finally {
    if (sequence === state.sequence.collection) {
      state.loading.collection = false;
      if (state.tab === 'collection') renderList();
    }
  }
}

async function loadExternal(skip = state.skip, query = state.externalQuery) {
  const sequence = ++state.sequence.external;
  state.skip = skip;
  state.externalQuery = query;
  state.loading.external = true;
  state.errors.external = '';
  if (state.tab === 'external') renderList();
  const params = new URLSearchParams({ limit: state.limit, skip });
  if (query) params.set('q', query);
  try {
    const data = await request(`/external-item${query ? '/search' : ''}?${params}`);
    if (sequence !== state.sequence.external) return;
    state.external = data.products;
    state.total = data.total;
    state.externalLoaded = true;
  } catch (error) {
    if (sequence !== state.sequence.external) return;
    state.errors.external = error.message;
  } finally {
    if (sequence === state.sequence.external) {
      state.loading.external = false;
      if (state.tab === 'external') renderList();
    }
  }
}

function selectTab(tab) {
  state.tab = tab;
  ['collection', 'external'].forEach((name) => {
    const active = name === tab;
    $(`#tab-${name}`).classList.toggle('active', active);
    $(`#tab-${name}`).setAttribute('aria-selected', String(active));
    $(`#tab-${name}`).tabIndex = active ? 0 : -1;
    $(`#${name}-pane`).hidden = !active;
  });
  renderList();
  if (tab === 'external' && !state.externalLoaded && !state.loading.external) loadExternal(0);
}

function refreshList() {
  return state.tab === 'external' ? loadExternal() : loadCollection();
}

function renderCart() {
  const container = $('#cart-items');
  container.replaceChildren();
  const count = state.cart.length;
  $('#cart-count').textContent = `${count} / 20`;
  $('#nav-cart-count').textContent = count;
  if (!count) {
    const empty = element('div', 'cart-empty');
    empty.append(element('span', '', '♧'), element('p', '', '장바구니에 작은 취향을 담아보세요.\n마음에 드는 상품의 ‘담기’를 눌러주세요.'));
    container.append(empty);
  }
  state.cart.forEach((line) => {
    const row = element('article', 'cart-item');
    const heading = element('div', 'cart-item-heading');
    const remove = mutationButton('×', 'remove-item');
    remove.setAttribute('aria-label', `${line.name} 장바구니에서 삭제`);
    remove.addEventListener('click', () => mutate(async () => {
      await request(`/shopping-cart/${line.collectionItemId}`, { method: 'DELETE' });
      await loadCart();
      notify('장바구니에서 상품을 삭제했어요.');
    }));
    heading.append(element('h3', '', line.name), remove);
    const meta = element('p', 'cart-item-meta', `단가 ${won(line.unitPrice)}${line.stock === null ? '' : ` · 재고 ${line.stock}개`}`);
    const bottom = element('div', 'cart-item-bottom');
    const stepper = element('div', 'stepper');
    const max = line.stock === null ? 5 : Math.min(5, line.stock);
    for (const delta of [-1, 1]) {
      if (delta === 1) stepper.append(element('span', '', line.amount));
      const button = mutationButton(delta === -1 ? '−' : '+', '', delta === -1 ? line.amount <= 1 : line.amount >= max);
      button.setAttribute('aria-label', `${line.name} 수량 ${delta === -1 ? '줄이기' : '늘리기'}`);
      button.addEventListener('click', () => mutate(async () => {
        await request('/shopping-cart', { method: 'PATCH', body: { collectionItemId: line.collectionItemId, amount: line.amount + delta } });
        await loadCart();
      }));
      stepper.append(button);
    }
    bottom.append(stepper, element('p', 'line-price', won(line.unitPrice * line.amount)));
    row.append(heading, meta, bottom);
    container.append(row);
  });
  updateControls();
}

function clearSummary() {
  ['subtotal', 'discount', 'shipping', 'final-price'].forEach((id) => { $(`#${id}`).textContent = '—'; });
}

async function loadCart() {
  const sequence = ++state.sequence.cart;
  ++state.sequence.summary;
  state.cartLoading = true;
  $('#cart-items').setAttribute('aria-busy', 'true');
  clearSummary();
  showError('#cart-error');
  updateControls();
  try {
    const data = await request('/shopping-cart');
    if (sequence !== state.sequence.cart) return;
    state.cart = data;
    state.cartLoaded = true;
    renderCart();
    renderList();
    await calculateSummary();
  } catch (error) {
    if (sequence !== state.sequence.cart) return;
    state.cartLoaded = false;
    $('#cart-count').textContent = '—';
    $('#nav-cart-count').textContent = '—';
    $('#cart-items').replaceChildren(element('p', 'cart-empty', '장바구니를 새로고침해 주세요.'));
    showError('#cart-error', error.message);
    renderList();
  } finally {
    if (sequence === state.sequence.cart) {
      state.cartLoading = false;
      $('#cart-items').setAttribute('aria-busy', 'false');
      updateControls();
    }
  }
}

async function calculateSummary() {
  const sequence = ++state.sequence.summary;
  const couponCode = $('#coupon').value || null;
  clearSummary();
  showError('#summary-error');
  $('#summary').setAttribute('aria-busy', 'true');
  $('#coupon-description').textContent = couponCode === 'PERCENT10'
    ? '상품 금액의 10% 할인 · 최대 20,000원 · 0원 초과 시 사용 가능'
    : couponCode === 'FIXED15000' ? '상품 금액 100,000원 이상일 때 15,000원 할인'
      : '쿠폰 없이 상품 금액을 기준으로 배송비를 계산해요.';
  try {
    const data = await request('/shopping-cart/subtotal', { method: 'POST', body: { couponCode } });
    if (sequence !== state.sequence.summary) return;
    $('#subtotal').textContent = won(data.subtotal);
    $('#discount').textContent = data.discount ? `−${won(data.discount)}` : '0원';
    $('#shipping').textContent = data.shipping ? won(data.shipping) : '무료';
    $('#final-price').textContent = won(data.finalPrice);
  } catch (error) {
    if (sequence !== state.sequence.summary) return;
    showError('#summary-error', `${error.message}\n쿠폰을 변경하거나 ‘쿠폰 사용 안 함’을 선택해 주세요.`);
  } finally {
    if (sequence === state.sequence.summary) $('#summary').setAttribute('aria-busy', 'false');
  }
}

$('#open-add').addEventListener('click', () => {
  showError('#add-error');
  $('#add-dialog').showModal();
});
$('#close-add').addEventListener('click', () => $('#add-dialog').close());
$('#add-submit').dataset.mutation = '';
$('#add-form').addEventListener('submit', (event) => {
  event.preventDefault();
  mutate(async () => {
    showError('#add-error');
    const name = $('#name').value.trim();
    if (name.length < 1 || name.length > 40) throw new Error('상품 이름은 1~40자로 입력해 주세요.');
    const price = integer($('#price'));
    await request('/item', { method: 'POST', body: { name, price } });
    $('#add-form').reset();
    $('#add-dialog').close();
    selectTab('collection');
    await loadCollection({ url: '/item', description: '내가 모아둔 상품을 장바구니에 담아보세요.' });
    notify('새로운 취향을 컬렉션에 추가했어요.');
  }, (error) => showError('#add-error', error.message));
});
$('#budget-form').addEventListener('submit', (event) => {
  event.preventDefault();
  try {
    const budget = integer($('#budget'));
    loadCollection({ url: `/item/recommendations?budget=${budget}`, description: `${won(budget)} 이하 · 낮은 가격순 · 최대 3개` });
  } catch (error) { notify(error.message, true); }
});
$('#lookup-form').addEventListener('submit', (event) => {
  event.preventDefault();
  try {
    const id = integer($('#item-id'), 1, Number.MAX_SAFE_INTEGER);
    loadCollection({ url: `/item/${id}`, description: `상품 ID ${id}의 조회 결과예요.` });
  } catch (error) { notify(error.message, true); }
});
$('#show-all').addEventListener('click', () => loadCollection({ url: '/item', description: '내가 모아둔 상품을 장바구니에 담아보세요.' }));
$('#refresh').addEventListener('click', refreshList);
$('#refresh-cart').addEventListener('click', loadCart);
$('#coupon').addEventListener('change', calculateSummary);
$('#external-form').addEventListener('submit', (event) => {
  event.preventDefault();
  loadExternal(0, $('#external-query').value.trim());
});
$('#previous-page').addEventListener('click', () => loadExternal(Math.max(0, state.skip - state.limit)));
$('#next-page').addEventListener('click', () => loadExternal(state.skip + state.limit));
['collection', 'external'].forEach((tab) => {
  $(`#tab-${tab}`).addEventListener('click', () => selectTab(tab));
  $(`#tab-${tab}`).addEventListener('keydown', (event) => {
    if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) return;
    event.preventDefault();
    const target = event.key === 'Home' ? 'collection' : event.key === 'End' ? 'external' : tab === 'collection' ? 'external' : 'collection';
    selectTab(target);
    $(`#tab-${target}`).focus();
  });
});
loadCollection();
loadCart();
