function pinFields(id) {
    const fields = Array.from(document.querySelectorAll("#" + id + " input"));
    function fill(start, text) {
        const digits = text.replace(/[^0-9]/g, "");
        if (!digits) return;
        const count = Math.min(digits.length, fields.length - start);
        for (let offset = 0; offset < count; offset++) {
            fields[start + offset].value = digits[offset];
        }
        if (fields[start + count]) fields[start + count].focus();
    }
    fields.forEach((field, index) => {
        field.addEventListener("focus", () => field.select());
        field.addEventListener("input", () => {
            const text = field.value;
            field.value = "";
            fill(index, text);
        });
        field.addEventListener("paste", event => {
            event.preventDefault();
            const text = event.clipboardData.getData("text");
            fill(text.replace(/[^0-9]/g, "").length === 4 ? 0 : index, text);
        });
        field.addEventListener("keydown", event => {
            if (event.key === "Enter") {
                event.preventDefault();
                field.form.requestSubmit(field.form.querySelector('button[type="submit"]'));
                return;
            }
            if (event.key === "Backspace" && !field.value && index > 0) {
                event.preventDefault();
                fields[index - 1].value = "";
                fields[index - 1].focus();
            }
        });
    });
    return {
        get value() { return fields.map(field => field.value).join(""); },
        clear() { fields.forEach(field => { field.value = ""; }); },
        focus() { (fields.find(field => !field.value) || fields[0]).focus(); }
    };
}

const sendPin = pinFields("send-pin");
const receivePin = pinFields("receive-pin");

const sendForm = document.getElementById("send-form");
const originalUrlInput = document.getElementById("original-url");
const pdfFileInput = document.getElementById("pdf-file");
const pdfModeButton = document.getElementById("pdf-mode");
const linkModeButton = document.getElementById("link-mode");
const pdfFields = document.getElementById("pdf-fields");
const linkFields = document.getElementById("link-fields");
const issuedNote = document.getElementById("issued-note");
const sendButton = document.getElementById("send-button");
const sendMessage = document.getElementById("send-message");
const issuedCode = document.getElementById("issued-code");
const issuedValue = document.getElementById("issued-value");
const copyButton = document.getElementById("copy-button");
const copyMessage = document.getElementById("copy-message");
const previewLink = document.getElementById("preview-link");
const cancelActions = document.getElementById("cancel-actions");
const cancelButton = document.getElementById("cancel-button");
const cancelMessage = document.getElementById("cancel-message");

const openForm = document.getElementById("open-form");
const accessCodeInput = document.getElementById("access-code");
const openButton = document.getElementById("open-button");
const openMessage = document.getElementById("open-message");
const openFallback = document.getElementById("open-fallback");

const codePattern = /^[0-9]{8}$/;
const maxPdfSize = 10 * 1024 * 1024;
let sendMode = "pdf";
let issuedPdf = null;
let cancelling = false;
const draftKey = "print-link-issued-pdf";
const fileRoutePattern = /^\/(?:pdf|pdf-bundles)\/pdf-[0-9]+\.pdf$/;
const tokenPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function showMessage(element, message, state = "") {
    element.textContent = message;
    element.className = "form-message" + (state ? " " + state : "");
    element.hidden = !message;
}

function setBusy(form, button, busy, label) {
    form.setAttribute("aria-busy", String(busy));
    button.disabled = busy;
    button.textContent = label;
}

function validOriginalUrl(value) {
    if (!/^https?:\/\//i.test(value)) {
        return false;
    }
    try {
        const url = new URL(value);
        return (url.protocol === "http:" || url.protocol === "https:") && Boolean(url.hostname);
    } catch {
        return false;
    }
}

function storeIssuedPdf(pdf) {
    issuedPdf = pdf;
    try {
        if (pdf) {
            sessionStorage.setItem(draftKey, JSON.stringify(pdf));
        } else {
            sessionStorage.removeItem(draftKey);
        }
    } catch {
        // 현재 화면의 취소 기능은 저장소 접근 제한 시에도 사용할 수 있다.
    }
}

function resetIssuedCode() {
    showMessage(sendMessage, "");
    showMessage(copyMessage, "");
    issuedCode.hidden = true;
    issuedValue.value = "";
    previewLink.removeAttribute("href");
    cancelActions.hidden = true;
    showMessage(cancelMessage, "");
    storeIssuedPdf(null);
}

function updateSendFields() {
    const isPdf = sendMode === "pdf";
    const busy = sendButton.disabled || cancelling;
    cancelButton.disabled = busy;
    copyButton.disabled = cancelling;

    pdfModeButton.setAttribute("aria-pressed", String(isPdf));
    linkModeButton.setAttribute("aria-pressed", String(!isPdf));
    pdfModeButton.disabled = busy;
    linkModeButton.disabled = busy;
    pdfFields.hidden = !isPdf;
    linkFields.hidden = isPdf;
    pdfFileInput.disabled = busy || !isPdf;
    originalUrlInput.disabled = busy || isPdf;
}

function chooseSendMode(mode) {
    if (sendButton.disabled || cancelling || sendMode === mode) {
        return;
    }
    sendMode = mode;
    originalUrlInput.removeAttribute("aria-invalid");
    pdfFileInput.removeAttribute("aria-invalid");
    showMessage(sendMessage, "");
    updateSendFields();
}

pdfModeButton.addEventListener("click", () => chooseSendMode("pdf"));
linkModeButton.addEventListener("click", () => chooseSendMode("link"));

sendForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (sendButton.disabled || cancelling) {
        return;
    }

    const isPdf = sendMode === "pdf";
    showMessage(sendMessage, "");
    if (issuedPdf && !window.confirm("새 코드를 발급하면 현재 PDF의 취소 버튼이 사라집니다. 계속할까요?")) {
        return;
    }

    let endpoint;
    let request;

    if (isPdf) {
        const files = Array.from(pdfFileInput.files);
        let error = "";
        if (files.length === 0 || files.length > 5) {
            error = "PDF를 1개에서 5개까지 선택해 주세요.";
        } else if (files.some(file => file.size === 0 || file.size > maxPdfSize)) {
            error = "비어 있지 않은 10MB 이하의 PDF를 선택해 주세요.";
        } else if (files.reduce((sum, file) => sum + file.size, 0) > 2 * maxPdfSize) {
            error = "선택한 PDF의 합계는 20MB까지예요.";
        } else if (files.some(file => !file.name.toLowerCase().endsWith(".pdf")
                && file.type !== "application/pdf")) {
            error = "PDF 파일만 선택해 주세요.";
        }
        if (error) {
            pdfFileInput.setAttribute("aria-invalid", "true");
            showMessage(sendMessage, error, "error");
            pdfFileInput.focus();
            return;
        }
        pdfFileInput.removeAttribute("aria-invalid");
        const pin = sendPin.value;
        if (!/^[0-9]{4}$/.test(pin)) {
            showMessage(sendMessage, "열람 PIN을 숫자 4자리로 입력하세요.", "error");
            sendPin.focus();
            return;
        }
        const fileData = new FormData();
        fileData.append("pin", pin);
        for (const file of files) {
            fileData.append("file", file);
        }
        endpoint = "/pdf";
        request = { method: "POST", body: fileData };
    } else {
        const originalUrl = originalUrlInput.value.trim();

        if (!validOriginalUrl(originalUrl)) {
            originalUrlInput.setAttribute("aria-invalid", "true");
            showMessage(sendMessage, "http:// 또는 https://로 시작하는 웹 링크를 입력해 주세요.", "error");
            originalUrlInput.focus();
            return;
        }

        const encodedLength = originalUrl.normalize("NFC").replace(/[^\x00-\x7F]+/gu, encodeURIComponent).length;
        if (originalUrl.length > 2048 || encodedLength > 2048) {
            originalUrlInput.setAttribute("aria-invalid", "true");
            showMessage(sendMessage, "링크가 너무 길어요. 한글 등은 인코딩한 뒤 최대 2,048자까지 보낼 수 있어요.", "error");
            originalUrlInput.focus();
            return;
        }

        originalUrlInput.removeAttribute("aria-invalid");
        endpoint = "/shortenUrl";
        request = {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ originalUrl })
        };
    }

    setBusy(sendForm, sendButton, true, isPdf ? "PDF 업로드 중…" : "코드 발급 중…");
    updateSendFields();

    try {
        const response = await fetch(endpoint, request);

        if (response.status === 413) {
            showMessage(sendMessage, "PDF 한 개는 10MB, 전체는 20MB까지예요.", "error");
            return;
        }

        if (response.status === 400) {
            showMessage(sendMessage, isPdf ? "PDF 파일을 다시 확인해 주세요." : "링크 주소를 다시 확인해 주세요.", "error");
            return;
        }

        if (!response.ok) {
            throw new Error("코드 발급 실패");
        }

        const data = await response.json();

        if (typeof data.shortenUrlKey !== "string" || !codePattern.test(data.shortenUrlKey)) {
            throw new Error("잘못된 응답");
        }

        if (isPdf && (!fileRoutePattern.test(data.originalUrl) || !tokenPattern.test(data.cancelToken))) {
            throw new Error("PDF 취소 정보가 누락된 응답");
        }

        resetIssuedCode();
        if (isPdf) {
            storeIssuedPdf({
                code: data.shortenUrlKey,
                path: data.originalUrl,
                token: data.cancelToken
            });
            cancelActions.hidden = false;
        }
        issuedValue.value = data.shortenUrlKey;
        previewLink.href = "/" + data.shortenUrlKey;
        issuedNote.textContent = isPdf
            ? "공용 PC에서 이 코드를 입력하세요. PDF는 업로드 후 10분 동안 열 수 있어요."
            : "공용 PC에서 프린트 링크를 열고 이 코드를 입력하세요.";
        issuedCode.hidden = false;
        showMessage(sendMessage, "코드가 발급되었어요.", "success");
    } catch {
        showMessage(sendMessage, "코드를 발급할 수 없어요. 연결을 확인하고 다시 시도해 주세요.", "error");
    } finally {
        setBusy(sendForm, sendButton, false, "8자리 코드 발급");
        updateSendFields();
    }
});

originalUrlInput.addEventListener("input", () => {
    originalUrlInput.removeAttribute("aria-invalid");
    showMessage(sendMessage, "");
});

pdfFileInput.addEventListener("change", () => {
    if (pdfFileInput.files.length) {
        sendPin.clear();
        sendPin.focus();
    }
    pdfFileInput.removeAttribute("aria-invalid");
    showMessage(sendMessage, "");
});

copyButton.addEventListener("click", async () => {
    if (!issuedValue.value) {
        return;
    }

    if (navigator.clipboard && window.isSecureContext) {
        try {
            await navigator.clipboard.writeText(issuedValue.value);
            showMessage(copyMessage, "코드를 복사했어요.", "success");
            return;
        } catch {
            // HTTP 접속이나 클립보드 권한 제한이 있을 때 수동 복사를 제공한다.
        }
    }

    issuedValue.focus();
    issuedValue.select();
    issuedValue.setSelectionRange(0, issuedValue.value.length);

    try {
        if (document.execCommand("copy")) {
            showMessage(copyMessage, "코드를 복사했어요.", "success");
            return;
        }
    } catch {
        // 브라우저에서 복사를 지원하지 않으면 선택한 코드의 수동 복사를 안내한다.
    }

    showMessage(copyMessage, "선택된 코드를 복사해 주세요.");
});

cancelButton.addEventListener("click", async () => {
    if (!issuedPdf || cancelling || sendButton.disabled) {
        return;
    }
    if (!window.confirm("전송을 취소하면 PDF가 삭제되고 발급된 코드는 사용할 수 없어요. 삭제할까요?")) {
        return;
    }

    const pdf = issuedPdf;
    cancelling = true;
    sendButton.disabled = true;
    cancelButton.textContent = "삭제 중…";
    updateSendFields();
    showMessage(cancelMessage, "");

    try {
        const response = await fetch(pdf.path, {
            method: "DELETE",
            headers: { "X-Cancel-Token": pdf.token },
            cache: "no-store"
        });

        if (response.status === 404) {
            const availability = await fetch("/shortenUrl/" + pdf.code, { cache: "no-store" });
            if (availability.status !== 404) {
                throw new Error("취소할 수 없는 PDF");
            }
        } else if (response.status !== 204) {
            throw new Error("삭제 실패");
        }

        resetIssuedCode();
        pdfFileInput.value = "";
        showMessage(sendMessage, "전송이 취소되었어요. 이 코드는 더 이상 사용할 수 없어요.", "success");
    } catch {
        showMessage(cancelMessage, "자료를 삭제하지 못했어요. 잠시 후 다시 시도해 주세요.", "error");
    } finally {
        cancelling = false;
        sendButton.disabled = false;
        cancelButton.textContent = "전송 취소 · PDF 삭제";
        updateSendFields();
    }
});

async function restoreIssuedPdf() {
    try {
        const pdf = JSON.parse(sessionStorage.getItem(draftKey));
        if (!pdf || !codePattern.test(pdf.code)
                || !fileRoutePattern.test(pdf.path) || !tokenPattern.test(pdf.token)) {
            return;
        }
        const response = await fetch("/shortenUrl/" + pdf.code, { cache: "no-store" });
        if (response.status === 404) {
            storeIssuedPdf(null);
            return;
        }
        if (!response.ok || issuedPdf || sendButton.disabled) {
            return;
        }

        const data = await response.json();
        if (data.originalUrl !== pdf.path || data.shortenUrlKey !== pdf.code || issuedPdf || sendButton.disabled) {
            return;
        }
        issuedValue.value = pdf.code;
        previewLink.href = "/" + pdf.code;
        issuedNote.textContent = "공용 PC에서 이 코드를 입력하세요. PDF는 업로드 후 10분 동안 열 수 있어요.";
        issuedCode.hidden = false;
        cancelActions.hidden = false;
        issuedPdf = pdf;
    } catch {
        // 브라우저 저장소 또는 서버에 일시적으로 연결할 수 없는 경우 새 발급은 계속 가능하다.
    }
}

const receivedFiles = document.getElementById("received-files");

function hideOpenFallback() {
    receivedFiles.replaceChildren();
    receivedFiles.hidden = true;
    openFallback.hidden = true;
    openFallback.removeAttribute("href");
}

previewLink.addEventListener("click", event => {
    if (!issuedPdf) return;
    event.preventDefault();
    accessCodeInput.value = issuedPdf.code;
    accessCodeInput.dispatchEvent(new Event("input"));
});

accessCodeInput.addEventListener("input", () => {
    receivePin.clear();
    if (codePattern.test(accessCodeInput.value)) receivePin.focus();
    accessCodeInput.removeAttribute("aria-invalid");
    showMessage(openMessage, "");
    hideOpenFallback();
});

openForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (openButton.disabled) {
        return;
    }

    const code = accessCodeInput.value.trim();
    showMessage(openMessage, "");
    hideOpenFallback();

    if (!codePattern.test(code)) {
        accessCodeInput.setAttribute("aria-invalid", "true");
        showMessage(openMessage, "숫자 8자리 코드를 입력해 주세요.", "error");
        accessCodeInput.focus();
        return;
    }

    accessCodeInput.removeAttribute("aria-invalid");
    setBusy(openForm, openButton, true, "코드 확인 중…");
    accessCodeInput.readOnly = true;

    let destinationTab = null;
    let opened = false;

    try {
        const response = await fetch("/shortenUrl/" + code, {
            cache: "no-store"
        });

        if (response.status === 404) {
            accessCodeInput.setAttribute("aria-invalid", "true");
            showMessage(openMessage, "존재하지 않거나 만료된 코드예요. 휴대폰에서 코드를 다시 발급받아 주세요.", "error");
            accessCodeInput.focus();
            return;
        }

        if (!response.ok) {
            throw new Error("코드 조회 실패");
        }

        const data = await response.json();
        if (data.shortenUrlKey !== code) {
            throw new Error("잘못된 응답");
        }

        if (fileRoutePattern.test(data.originalUrl)) {
            const pinInput = receivePin;
            if (!/^[0-9]{4}$/.test(pinInput.value)) {
                showMessage(openMessage, "PDF 열람 PIN 네 자리를 입력하세요.", "error");
                pinInput.focus();
                return;
            }
            const name = data.originalUrl.split("/").pop();
            const access = await fetch("/pdf-access/" + encodeURIComponent(name), {
                method: "POST", body: new URLSearchParams({ pin: pinInput.value }), cache: "no-store"
            });
            if (access.status === 403) {
                showMessage(openMessage, "PIN을 확인하세요. 5회 실패하면 새 코드를 발급받아야 해요.", "error");
                pinInput.focus();
                return;
            }
            if (!access.ok) throw new Error("자료를 열 수 없습니다.");
            pinInput.clear();
        }
        const target = new URL("/" + code, window.location.href).href;
        if (/^\/pdf-bundles\/pdf-[0-9]+\.pdf$/.test(data.originalUrl)) {
            const listing = await fetch(target, { cache: "no-store" });
            if (!listing.ok) throw new Error("파일 목록 조회 실패");
            const documentWithFiles = new DOMParser().parseFromString(await listing.text(), "text/html");
            const items = document.createDocumentFragment();
            for (const source of documentWithFiles.querySelectorAll("main li a")) {
                const href = source.getAttribute("href");
                if (!href || !href.startsWith(data.originalUrl + "/files/") ||
                        !/^pdf-[0-9]+\.pdf$/.test(href.slice((data.originalUrl + "/files/").length))) continue;
                const link = document.createElement("a");
                link.href = href;
                link.textContent = source.textContent;
                link.target = "_blank";
                link.rel = "noopener noreferrer";
                const item = document.createElement("li");
                item.append(link);
                items.append(item);
            }
            if (!items.childElementCount) throw new Error("빈 파일 목록");
            receivedFiles.replaceChildren(items);
            receivedFiles.hidden = false;
            accessCodeInput.value = "";
            showMessage(openMessage, "열 파일을 선택하세요.", "success");
            return;
        }
        try {
            destinationTab = window.open("about:blank", "_blank");
            if (destinationTab) destinationTab.opener = null;
        } catch {
            // 브라우저가 팝업을 막으면 아래의 직접 열기 링크를 제공한다.
        }
        if (destinationTab && !destinationTab.closed) {
            try {
                destinationTab.location.replace(target);
                opened = true;
            } catch {
                // 새 탭이 닫혔거나 이동을 허용하지 않으면 직접 열기 링크를 이용한다.
            }
        }

        accessCodeInput.value = "";
        if (opened) {
            showMessage(openMessage, "새 탭에서 자료를 열었어요.", "success");
        } else {
            openFallback.href = target;
            openFallback.hidden = false;
            showMessage(openMessage, "아래 버튼을 눌러 새 탭에서 자료를 열어 주세요.");
        }
    } catch {
        showMessage(openMessage, "자료를 열 수 없어요. 연결을 확인하고 다시 시도해 주세요.", "error");
    } finally {
        if (!opened && destinationTab && !destinationTab.closed) {
            destinationTab.close();
        }
        accessCodeInput.readOnly = false;
        setBusy(openForm, openButton, false, "자료 열기");
    }
});

updateSendFields();
restoreIssuedPdf();
