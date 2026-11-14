"use strict";

const $ = (selector) => document.querySelector(selector);
const won = new Intl.NumberFormat("ko-KR");
let activeQuery = "";
let activeBudget = null;
let busy = false;

function notify(message, error = false) {
  $("#notice").textContent = message;
  $("#notice").classList.toggle("error", error);
}

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(path, { ...options, headers: { "Accept": "application/json", ...(options.body ? { "Content-Type": "application/json" } : {}) } });
  } catch {
    throw new Error("서버에 연결할 수 없어요. 애플리케이션이 실행 중인지 확인해 주세요.");
  }
  const raw = await response.text();
  let data;
  try { data = JSON.parse(raw); } catch { data = null; }
  if (!response.ok) {
    const message = data?.message || (raw && !raw.trim().startsWith("<") && !data ? raw.slice(0, 200) : "");
    throw new Error(message || `요청을 처리하지 못했어요. 입력값과 서버 로그를 확인해 주세요. (HTTP ${response.status})`);
  }
  if (data === null) throw new Error("서버 응답이 JSON 형식이 아니에요.");
  return data;
}

function integer(value, label) {
  if (value.trim() === "") throw new Error(`${label}을 입력해 주세요.`);
  const number = Number(value);
  if (!Number.isInteger(number) || number < 0 || number > 2147483647) {
    throw new Error(`${label}은 0 이상 2,147,483,647 이하의 정수로 입력해 주세요.`);
  }
  return number;
}

function element(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

function priceText(price) { return price === 0 ? "무료 · 0원" : `${won.format(price)}원`; }

function renderItems(items) {
  const container = $("#items");
  container.replaceChildren();
  $("#count").textContent = items.length;
  if (!items.length) {
    container.append(element("p", "empty", activeQuery ? "검색 결과가 없어요. 다른 이름으로 찾아보세요." : "아직 담은 아이템이 없어요. 첫 번째 위시를 등록해 보세요."));
    return;
  }
  for (const item of items) {
    const row = element("article", "item-row");
    const icon = element("span", "item-icon", "✦");
    icon.setAttribute("aria-hidden", "true");
    const info = element("div", "item-info");
    info.append(element("h3", "", item.name), element("small", "", `ITEM #${item.id}`));
    row.append(icon, info, element("span", `item-price${item.price === 0 ? " free" : ""}`, priceText(item.price)));
    container.append(row);
  }
}

function renderRecommendations(items) {
  const container = $("#recommendations");
  container.replaceChildren();
  if (!items.length) {
    container.append(element("p", "empty compact", "이 예산으로 살 수 있는 아이템이 없어요."));
    return;
  }
  const picks = element("div", "picks");
  items.forEach((item, index) => {
    const card = element("article", "pick");
    card.append(element("span", "rank", `PICK 0${index + 1}`), element("h4", "", item.name), element("strong", item.price === 0 ? "free" : "", priceText(item.price)));
    picks.append(card);
  });
  container.append(picks);
}

async function loadItems() {
  const path = activeQuery ? `/items/search?${new URLSearchParams({ name: activeQuery })}` : "/items";
  renderItems(await request(path));
}

async function loadRecommendations() {
  renderRecommendations(await request(`/items/recommend?${new URLSearchParams({ budget: activeBudget })}`));
}

async function action(task) {
  if (busy) return;
  busy = true;
  document.querySelectorAll("button").forEach((button) => { button.disabled = true; });
  try { await task(); } catch (error) { notify(error.message, true); }
  finally {
    busy = false;
    document.querySelectorAll("button").forEach((button) => { button.disabled = false; });
  }
}

$("#add-form").addEventListener("submit", (event) => {
  event.preventDefault();
  action(async () => {
    const name = $("#name").value.trim();
    if (!name) throw new Error("아이템 이름을 입력해 주세요.");
    const price = integer($("#price").value, "가격");
    const saved = await request("/items", { method: "POST", body: JSON.stringify({ name, price }) });
    $("#add-form").reset();
    activeQuery = "";
    $("#query").value = "";
    notify(`“${saved.name}” 등록 완료 · 서버가 부여한 id: ${saved.id}`);
    try {
      await loadItems();
      if (activeBudget !== null) await loadRecommendations();
    } catch (error) {
      notify(`등록은 완료됐지만 화면 갱신에 실패했어요. 전체 보기를 눌러 주세요. ${error.message}`, true);
    }
  });
});

$("#search-form").addEventListener("submit", (event) => {
  event.preventDefault();
  action(async () => {
    const query = $("#query").value.trim();
    if (!query) throw new Error("검색어를 입력해 주세요. 전체 목록은 ‘전체 보기’를 눌러 주세요.");
    const items = await request(`/items/search?${new URLSearchParams({ name: query })}`);
    activeQuery = query;
    renderItems(items);
    notify(`“${query}” 검색 결과 ${items.length}개`);
  });
});

$("#refresh").addEventListener("click", () => action(async () => {
  const items = await request("/items");
  activeQuery = "";
  $("#query").value = "";
  renderItems(items);
  notify(`전체 아이템 ${items.length}개를 불러왔어요.`);
}));

$("#budget-form").addEventListener("submit", (event) => {
  event.preventDefault();
  action(async () => {
    const budget = integer($("#budget").value, "예산");
    const items = await request(`/items/recommend?${new URLSearchParams({ budget })}`);
    activeBudget = budget;
    renderRecommendations(items);
    notify(`개별 가격 ${won.format(budget)}원 이하 추천 · ${items.length}개`);
  });
});

action(async () => {
  await loadItems();
  notify("목록을 불러왔어요. 새로운 위시를 담아보세요.");
});
