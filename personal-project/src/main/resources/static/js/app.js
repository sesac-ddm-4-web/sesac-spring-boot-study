
const $ = id => document.getElementById(id);
const won = value => Number(value).toLocaleString("ko-KR") + "원";

async function api(url, options = {}) {
    const response = await fetch(url, options);

    if (!response.ok) {
        let error = {};
        try {
            error = await response.json();
        } catch (_) {}

        throw new Error(error.error || `요청 실패 (${response.status})`);
    }

    return response.status === 204 ? null : response.json();
}

function jsonOptions(method, data) {
    return {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    };
}

function message(id, text, isError = false) {
    const el = $(id);
    el.textContent = text;
    el.className = isError ? "message error" : "message";
}

function empty(container, text) {
    container.replaceChildren();
    const el = document.createElement("div");
    el.className = "empty";
    el.textContent = text;
    container.append(el);
}

function makeButton(text, callback) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.textContent = text;
    btn.addEventListener("click", callback);
    return btn;
}

function createRow(name, detail, price, actionText, action) {
    const row = document.createElement("div");
    row.className = "item";

    const info = document.createElement("div");
    const title = document.createElement("div");
    title.className = "item-name";
    title.textContent = name;

    const sub = document.createElement("div");
    sub.className = "muted";
    sub.textContent = detail;

    info.append(title, sub);

    const actions = document.createElement("div");
    actions.className = "actions";

    const priceEl = document.createElement("strong");
    priceEl.className = "item-price";
    priceEl.textContent = won(price);

    actions.append(priceEl);

    if (actionText) {
        actions.append(makeButton(actionText, action));
    }

    row.append(info, actions);
    return row;
}

// 1. 기존 장비 등록
$("registerForm").addEventListener("submit", async event => {
    event.preventDefault();

    const name = $("itemName").value.trim();
    const priceText = $("itemPrice").value.trim();
    const price = Number(priceText);

    if (!name || priceText === "" ||
        !Number.isSafeInteger(price) || price < 0) {
        message("registerMessage", "이름과 올바른 가격을 입력해 주세요.", true);
        return;
    }

    try {
        const params = new URLSearchParams({
            name,
            price: String(price)
        });

        await api("/api/items", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: params.toString()
        });

        message("registerMessage", "장비가 등록되었습니다.");
        event.target.reset();
        await loadItems();

    } catch (error) {
        message("registerMessage", error.message, true);
    }
});

// 2. 기존 위시리스트 조회

async function loadItems() {
    const target = $("itemList");

    try {
        const items = await api("/api/items");
        target.replaceChildren();

        if (!items.length) {
            empty(target, "등록된 장비가 없습니다.");
            return;
        }

        for (const item of items) {
            const source = item.sourceType === "DUMMYJSON"
                ? `외부 상품 #${item.externalId}`
                : "수동 등록";

            const row = createRow(
                item.name,
                `#${item.id} · ${source}`,
                item.price,
                "장바구니 담기",
                () => addCart(item.id)
            );

            // 위시리스트 삭제 버튼
            const deleteBtn = makeButton("삭제", () => {
                deleteItem(item.id, item.name);
            });

            deleteBtn.className = "btn-delete";

            row.querySelector(".actions").append(deleteBtn);

            target.append(row);
        }

    } catch (error) {
        empty(target, error.message);
    }
}

async function deleteItem(id, name) {

    const confirmed = confirm(
        `${name} 상품을 위시리스트에서 삭제하시겠습니까?`
    );

    if (!confirmed) return;

    try {
        await api(`/api/items/${id}`, {
            method: "DELETE"
        });

        alert("상품이 삭제되었습니다.");

        // 삭제 후 위시리스트 갱신
        await loadItems();

    } catch (error) {
        alert(error.message);
    }
}


// 3. 기존 예산별 추천
$("recommendForm").addEventListener("submit", async event => {
    event.preventDefault();

    const target = $("recommendList");
    const budget = Number($("budget").value);

    if (!Number.isSafeInteger(budget) || budget < 0) {
        empty(target, "올바른 예산을 입력해 주세요.");
        return;
    }

    try {
        const items = await api(
            `/api/items/recommend?budget=${encodeURIComponent(budget)}`
        );

        target.replaceChildren();

        if (!items.length) {
            empty(target, "예산에 맞는 장비가 없습니다.");
            return;
        }

        items.forEach((item, index) => {
            target.append(createRow(
                `${index + 1}. ${item.name}`,
                `장비 #${item.id}`,
                item.price,
                null,
                null
            ));
        });

    } catch (error) {
        empty(target, error.message);
    }
});

// 4. 신규: 외부 상품 검색
async function loadExternal(query = null) {
    const target = $("externalList");

    const url = query === null
        ? "/api/external-products?limit=10&skip=0"
        : `/api/external-products/search?q=${encodeURIComponent(query)}&limit=10&skip=0`;

    try {
        const result = await api(url);
        target.replaceChildren();

        if (!result.products?.length) {
            empty(target, "검색 결과가 없습니다.");
            return;
        }

        for (const item of result.products) {
            const row = createRow(
                item.title,
                `외부 ID #${item.id} · 재고 ${item.stock}개`,
                Math.round(Number(item.price) * 1000),
                "위시리스트 가져오기",
                () => importItem(item.id)
            );

            if (item.thumbnail) {
                const img = document.createElement("img");
                img.src = item.thumbnail;
                img.alt = item.title;
                img.className = "thumb";
                row.prepend(img);
            }

            target.append(row);
        }
    } catch (error) {
        empty(target, error.message);
    }
}

$("searchForm").addEventListener("submit", event => {
    event.preventDefault();

    const query = $("query").value.trim();

    if (!query) {
        empty($("externalList"), "검색어를 입력해 주세요.");
        return;
    }

    loadExternal(query);
});

$("browseProducts").addEventListener("click", () => loadExternal());

// 5. 신규: 외부 상품 가져오기
async function importItem(externalId) {
    try {
        await api(`/api/items/import/${externalId}`, {
            method: "POST"
        });

        await loadItems();
        alert("위시리스트에 저장되었습니다.");

    } catch (error) {
        alert(error.message);
    }
}

// 6. 신규: 장바구니 담기
async function addCart(itemId) {
    const input = prompt("담을 수량 (1~5)", "1");

    if (input === null) return;

    const quantity = Number(input);

    if (!Number.isInteger(quantity) || quantity < 1 || quantity > 5) {
        alert("수량은 1~5개여야 합니다.");
        return;
    }

    try {
        await api("/api/cart", jsonOptions("POST", {
            itemId,
            quantity
        }));

        await loadCart();

    } catch (error) {
        alert(error.message);
    }
}

// 7. 신규: 장바구니 조회·수량 변경·삭제
async function loadCart() {
    const target = $("cartList");

    try {
        const items = await api("/api/cart");
        target.replaceChildren();

        if (!items.length) {
            empty(target, "장바구니가 비어 있습니다.");
            $("previewResult").textContent = "";
            return;
        }

        for (const item of items) {
            const row = document.createElement("div");
            row.className = "item";

            const info = document.createElement("div");
            info.textContent = `${item.name} · ${won(item.unitPrice)} × ${item.quantity}`;

            const actions = document.createElement("div");
            actions.className = "actions";

            const quantityInput = document.createElement("input");
            quantityInput.type = "number";
            quantityInput.min = "1";
            quantityInput.max = "5";
            quantityInput.value = item.quantity;
            quantityInput.className = "qty";

            const updateBtn = makeButton("수량 변경", async () => {
                try {
                    const quantity = Number(quantityInput.value);

                    await api(
                        `/api/cart/${item.id}`,
                        jsonOptions("PATCH", { quantity })
                    );

                    await loadCart();
                } catch (error) {
                    alert(error.message);
                }
            });

            const deleteBtn = makeButton("삭제", async () => {
                try {
                    await api(`/api/cart/${item.id}`, {
                        method: "DELETE"
                    });

                    await loadCart();
                } catch (error) {
                    alert(error.message);
                }
            });

            actions.append(quantityInput, updateBtn, deleteBtn);
            row.append(info, actions);
            target.append(row);
        }

    } catch (error) {
        empty(target, error.message);
    }
}

// 8. 신규: 쿠폰·배송비 미리보기
$("previewBtn").addEventListener("click", async () => {
    try {
        const coupon = $("coupon").value;
        const result = await api(
            `/api/cart/preview?couponCode=${encodeURIComponent(coupon)}`
        );

        message("previewResult",
            `상품 합계: ${won(result.subtotal)}
할인 금액: ${won(result.discount)}
할인 후 금액: ${won(result.discountedSubtotal)}
배송비: ${won(result.shipping)}
최종 금액: ${won(result.finalAmount)}`
        );

    } catch (error) {
        message("previewResult", error.message, true);
    }
});

// 최초 실행
loadItems();
loadCart();
loadExternal();
