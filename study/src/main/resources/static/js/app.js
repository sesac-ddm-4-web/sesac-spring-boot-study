'use strict';

// 화면은 해시(#/...)로 구분한다. 권한 검사는 서버가 하고, 화면은 역할에 맞는 버튼만 보여 준다.
// 사용자가 입력한 값은 항상 텍스트 노드로 넣는다. (innerHTML을 쓰지 않아 HTML로 해석되지 않는다.)

const DAYS = {
    MONDAY: '월요일', TUESDAY: '화요일', WEDNESDAY: '수요일', THURSDAY: '목요일',
    FRIDAY: '금요일', SATURDAY: '토요일', SUNDAY: '일요일',
};
const STATUSES = { RECRUITING: '모집 중', CLOSED: '모집 마감' };
const ROLES = { OWNER: '스터디장', MEMBER: '참여 중' };
const REASONS = { OWNER_DELETED: '내가 삭제함', LEFT: '탈퇴함', STUDY_DELETED: '스터디가 삭제됨' };

const app = document.getElementById('app');
const nav = document.getElementById('nav');

let me = null;
let renderSequence = 0;
let prefillEmail = '';

// ---------------------------------------------------------------- 공통

class ApiError extends Error {
    constructor(status, message, errors = []) {
        super(message);
        this.status = status;
        this.errors = errors;
    }

    /** 필드별 오류가 있으면 함께 보여 준다. */
    get detail() {
        return this.errors.length ? this.errors.map((e) => e.message).join('\n') : this.message;
    }
}

async function api(method, url, body) {
    let response;
    try {
        response = await fetch(url, {
            method,
            headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
            body: body === undefined ? undefined : JSON.stringify(body),
            cache: 'no-store',
        });
    } catch {
        throw new ApiError(0, '서버에 연결할 수 없습니다.');
    }
    if (response.status === 204) {
        return null;
    }
    const data = await response.json().catch(() => null);
    if (response.ok) {
        return data;
    }
    const error = new ApiError(response.status, data?.message ?? '요청을 처리하지 못했습니다.', data?.errors);
    // 로그인 실패를 제외한 401은 세션이 끝난 것이다.
    if (response.status === 401 && url !== '/api/auth/login') {
        const wasLoggedIn = me !== null;
        me = null;
        if (wasLoggedIn) {
            toast('로그인이 만료됐습니다. 다시 로그인해 주세요.', true);
            go('#/login');
        }
    }
    throw error;
}

/** el('p', { class: 'meta' }, '내용') */
function el(tag, props = {}, ...children) {
    const node = document.createElement(tag);
    for (const [key, value] of Object.entries(props)) {
        if (value === undefined || value === null || value === false) {
            continue;
        }
        if (key.startsWith('on')) {
            node.addEventListener(key.slice(2), value);
        } else if (key === 'class') {
            node.className = value;
        } else if (key === 'value') {
            node.value = value;
        } else {
            node.setAttribute(key, value === true ? '' : value);
        }
    }
    node.append(...children.flat().filter((child) => child !== null && child !== undefined && child !== false));
    return node;
}

let toastTimer;

function toast(message, failed = false) {
    const box = document.getElementById('toast');
    box.textContent = message;
    box.classList.toggle('fail', failed);
    box.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { box.hidden = true; }, 4000);
}

function confirmAction(message, okLabel) {
    const dialog = document.getElementById('confirm');
    document.getElementById('confirm-message').textContent = message;
    document.getElementById('confirm-ok').textContent = okLabel;
    dialog.returnValue = 'cancel';
    dialog.showModal();
    return new Promise((resolve) => {
        dialog.addEventListener('close', () => resolve(dialog.returnValue === 'ok'), { once: true });
    });
}

function go(hash) {
    if (location.hash === hash) {
        render();
    } else {
        location.hash = hash;
    }
}

/** 제출 중에는 버튼을 막고, 실패하면 폼 위에 서버의 안내 문구를 보여 준다. */
function form(submitLabel, fields, onSubmit) {
    const errorBox = el('p', { class: 'error', role: 'alert', hidden: true });
    const button = el('button', { type: 'submit' }, submitLabel);
    const node = el('form', { class: 'stack', novalidate: true }, errorBox, fields, el('div', { class: 'actions' }, button));
    node.addEventListener('submit', async (event) => {
        event.preventDefault();
        button.disabled = true;
        errorBox.hidden = true;
        try {
            await onSubmit(Object.fromEntries(new FormData(node)));
        } catch (error) {
            errorBox.textContent = error.detail ?? error.message;
            errorBox.hidden = false;
            errorBox.scrollIntoView({ block: 'nearest' });
        } finally {
            button.disabled = false;
        }
    });
    return node;
}

/** 버튼 한 번의 작업. 실패하면 그 사이 상태가 바뀌었을 수 있어 화면을 다시 그린다. */
function actionButton(label, className, task) {
    const button = el('button', { type: 'button', class: className }, label);
    button.addEventListener('click', async () => {
        button.disabled = true;
        try {
            await task();
        } catch (error) {
            if (error.status !== 401) {
                toast(error.detail ?? error.message, true);
                render();
            }
        } finally {
            button.disabled = false;
        }
    });
    return button;
}

function field(label, control, hint) {
    return el('label', {}, el('span', {}, label, hint ? el('span', { class: 'hint' }, ` ${hint}`) : null), control);
}

function statusBadge(status) {
    return el('span', { class: status === 'RECRUITING' ? 'badge open' : 'badge' }, STATUSES[status]);
}

function timeText(study) {
    return `매주 ${DAYS[study.dayOfWeek]} ${study.startTime}~${study.endTime}`;
}

function dateTimeText(value) {
    return value.replace('T', ' ');
}

/** 한국 시간의 현재 시각. 일정 입력값과 같은 형식(yyyy-MM-ddTHH:mm)이다. */
function nowInSeoul() {
    return new Intl.DateTimeFormat('sv-SE', {
        timeZone: 'Asia/Seoul', year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
    }).format(new Date()).replace(' ', 'T');
}

function studyList(studies, emptyMessage) {
    if (!studies.length) {
        return el('p', { class: 'card empty' }, emptyMessage);
    }
    return el('ul', { class: 'list' }, studies.map((study) =>
        el('li', {}, el('a', { class: 'card', href: `#/studies/${study.id}` },
            el('p', { class: 'card-title' }, statusBadge(study.recruitStatus), study.name),
            el('div', { class: 'meta' }, timeText(study)),
            el('div', { class: 'meta' }, `스터디장 ${study.ownerName} · 인원 ${study.memberCount}/${study.capacity}`)))));
}

// ---------------------------------------------------------------- 로그인·회원가입

function loginView(mode = 'login') {
    const switchTo = (next) => app.replaceChildren(loginView(next));
    const tabs = el('div', { class: 'tabs' },
        el('button', { type: 'button', class: mode === 'login' ? '' : 'secondary', onclick: () => switchTo('login') }, '로그인'),
        el('button', { type: 'button', class: mode === 'signup' ? '' : 'secondary', onclick: () => switchTo('signup') }, '회원가입'));

    const email = el('input', { name: 'email', type: 'email', maxlength: 100, autocomplete: 'email', value: prefillEmail });
    const password = el('input', {
        name: 'password', type: 'password', maxlength: 64,
        autocomplete: mode === 'login' ? 'current-password' : 'new-password',
    });

    const body = mode === 'login'
        ? form('로그인', [field('이메일', email), field('비밀번호', password)], async (values) => {
            await api('POST', '/api/auth/login', values);
            prefillEmail = '';
            me = await api('GET', '/api/members/me');
            go('#/studies');
        })
        : form('가입하기', [
            field('이메일', email),
            field('비밀번호', password, '8~64자'),
            field('이름', el('input', { name: 'name', maxlength: 30, autocomplete: 'name' }), '30자 이하'),
            field('자기소개', el('textarea', { name: 'introduction', maxlength: 500 }), '선택, 500자 이하'),
        ], async (values) => {
            const member = await api('POST', '/api/members', values);
            // 가입 후 자동 로그인하지 않는다.
            prefillEmail = member.email;
            toast('가입했습니다. 로그인해 주세요.');
            switchTo('login');
        });

    return el('section', {}, el('h1', {}, mode === 'login' ? '로그인' : '회원가입'), tabs, el('div', { class: 'card' }, body));
}

// ---------------------------------------------------------------- 목록·개설

async function studiesView() {
    const studies = await api('GET', '/api/studies');
    return el('section', {},
        el('div', { class: 'page-head' },
            el('h1', {}, '스터디 목록'),
            el('a', { class: 'button', href: '#/studies/new' }, '스터디 개설')),
        studyList(studies, '아직 스터디가 없습니다. 첫 스터디를 만들어 보세요.'));
}

function newStudyView() {
    const select = (name, options) => el('select', { name },
        Object.entries(options).map(([value, label]) => el('option', { value }, label)));

    const body = form('개설하기', [
        field('스터디명', el('input', { name: 'name', maxlength: 50 }), '50자 이하'),
        field('상세 내용', el('textarea', { name: 'description', maxlength: 2000, placeholder: '활동 방식을 포함해 적어 주세요.' }), '2000자 이하'),
        el('div', { class: 'row' },
            field('정원', el('input', { name: 'capacity', type: 'number', min: 2, max: 100, value: 4 }), '스터디장 포함'),
            field('모집 상태', select('recruitStatus', STATUSES))),
        el('div', { class: 'row' },
            field('요일', select('dayOfWeek', DAYS)),
            field('시작', el('input', { name: 'startTime', type: 'time', value: '19:00' })),
            field('종료', el('input', { name: 'endTime', type: 'time', value: '21:00' }))),
    ], async (values) => {
        const study = await api('POST', '/api/studies', {
            ...values,
            capacity: values.capacity === '' ? null : Number(values.capacity),
            startTime: values.startTime || null,
            endTime: values.endTime || null,
        });
        toast('스터디를 개설했습니다.');
        go(`#/studies/${study.id}`);
    });

    return el('section', {}, el('h1', {}, '스터디 개설'), el('div', { class: 'card' }, body));
}

// ---------------------------------------------------------------- 상세·일정

async function studyView(id) {
    const study = await api('GET', `/api/studies/${id}`);
    const joined = study.myRole !== 'NONE';

    const facts = el('dl', { class: 'facts' },
        el('dt', {}, '스터디장'), el('dd', {}, study.ownerName),
        el('dt', {}, '시간'), el('dd', {}, timeText(study)),
        el('dt', {}, '인원'), el('dd', {}, `${study.memberCount} / ${study.capacity}명`));

    const section = el('section', {},
        el('h1', {}, study.name),
        el('div', { class: 'card' },
            el('p', { class: 'actions' }, statusBadge(study.recruitStatus),
                joined ? el('span', { class: 'badge role' }, ROLES[study.myRole]) : null),
            facts,
            el('p', { class: 'body-text' }, study.description)),
        studyActions(study));

    section.append(el('h2', {}, '모임 일정'));
    if (!joined) {
        section.append(el('p', { class: 'card empty' }, '참여하면 모임 일정을 볼 수 있습니다.'));
        return section;
    }
    const schedules = await api('GET', `/api/studies/${id}/schedules`);
    section.append(scheduleList(schedules));
    if (study.myRole === 'OWNER') {
        section.append(el('h2', {}, '일정 등록'), el('div', { class: 'card' }, scheduleForm(study.id)));
    }
    return section;
}

function studyActions(study) {
    const url = `/api/studies/${study.id}`;

    if (study.myRole === 'OWNER') {
        const next = study.recruitStatus === 'RECRUITING' ? 'CLOSED' : 'RECRUITING';
        return el('div', { class: 'actions' },
            actionButton(next === 'CLOSED' ? '모집 마감하기' : '다시 모집하기', 'secondary', async () => {
                await api('PATCH', `${url}/recruit-status`, { recruitStatus: next });
                toast('모집 상태를 변경했습니다.');
                render();
            }),
            actionButton('스터디 삭제', 'danger', async () => {
                if (!await confirmAction(`'${study.name}' 스터디를 삭제할까요? 일정과 참여 정보가 모두 삭제됩니다.`, '삭제')) {
                    return;
                }
                await api('DELETE', url);
                toast('스터디를 삭제했습니다.');
                go('#/studies');
            }));
    }

    if (study.myRole === 'MEMBER') {
        return el('div', { class: 'actions' }, actionButton('탈퇴하기', 'danger', async () => {
            if (!await confirmAction(`'${study.name}' 스터디에서 탈퇴할까요?`, '탈퇴')) {
                return;
            }
            await api('DELETE', `${url}/members/me`);
            toast('탈퇴했습니다.');
            render();
        }));
    }

    const join = actionButton('참여하기', '', async () => {
        await api('POST', `${url}/members`);
        toast('스터디에 참여했습니다.');
        render();
    });
    let reason = null;
    if (study.recruitStatus === 'CLOSED') {
        reason = '모집이 마감된 스터디입니다.';
    } else if (study.memberCount >= study.capacity) {
        reason = '정원이 가득 찼습니다.';
    }
    join.disabled = reason !== null;
    return el('div', { class: 'actions' }, join, reason ? el('span', { class: 'meta' }, reason) : null);
}

function scheduleList(schedules) {
    if (!schedules.length) {
        return el('p', { class: 'card empty' }, '등록된 일정이 없습니다.');
    }
    const now = nowInSeoul();
    return el('ul', { class: 'list' }, schedules.map((schedule) => {
        const past = schedule.meetingAt <= now;
        return el('li', { class: past ? 'card past' : 'card' },
            el('p', { class: 'card-title' }, dateTimeText(schedule.meetingAt),
                past ? el('span', { class: 'badge' }, '지난 일정') : null),
            el('div', { class: 'meta' }, `장소: ${schedule.place}`),
            el('p', { class: 'body-text' }, schedule.content));
    }));
}

function scheduleForm(studyId) {
    return form('일정 등록', [
        field('일시', el('input', { name: 'meetingAt', type: 'datetime-local', min: nowInSeoul() }), '한국 시간'),
        field('장소', el('input', { name: 'place', maxlength: 100 }), '100자 이하'),
        field('내용', el('textarea', { name: 'content', maxlength: 1000 }), '1000자 이하'),
    ], async (values) => {
        await api('POST', `/api/studies/${studyId}/schedules`, { ...values, meetingAt: values.meetingAt || null });
        toast('일정을 등록했습니다.');
        render();
    });
}

// ---------------------------------------------------------------- 내 정보

async function myView() {
    me = await api('GET', '/api/members/me');

    const edit = form('저장', [
        field('이름', el('input', { name: 'name', maxlength: 30, value: me.name }), '30자 이하'),
        field('자기소개', el('textarea', { name: 'introduction', maxlength: 500, value: me.introduction }), '500자 이하'),
    ], async (values) => {
        await api('PATCH', '/api/members/me', values);
        toast('내 정보를 저장했습니다.');
        render();
    });

    const histories = me.histories.length
        ? el('ul', { class: 'list' }, me.histories.map((history) =>
            el('li', { class: 'card' },
                el('p', { class: 'card-title' }, history.studyName),
                el('div', { class: 'meta' }, `${REASONS[history.reason]} · ${dateTimeText(history.endedAt)}`))))
        : el('p', { class: 'card empty' }, '과거 이력이 없습니다.');

    return el('section', {},
        el('h1', {}, '내 정보'),
        el('div', { class: 'card' }, el('p', { class: 'meta' }, `이메일: ${me.email}`), edit),
        el('h2', {}, '개설한 스터디'), studyList(me.ownedStudies, '개설한 스터디가 없습니다.'),
        el('h2', {}, '참여 중인 스터디'), studyList(me.joinedStudies, '참여 중인 스터디가 없습니다.'),
        el('h2', {}, '과거 이력'), histories);
}

// ---------------------------------------------------------------- 라우팅

function resolveRoute(hash) {
    const detail = hash.match(/^#\/studies\/(\d+)$/);
    if (hash === '#/studies/new') {
        return { name: 'new', view: newStudyView };
    }
    if (detail) {
        return { name: 'studies', view: () => studyView(detail[1]) };
    }
    if (hash === '#/me') {
        return { name: 'me', view: myView };
    }
    return { name: 'studies', view: studiesView };
}

async function render() {
    const sequence = ++renderSequence;
    nav.hidden = me === null;

    if (me === null) {
        if (location.hash !== '#/login') {
            location.hash = '#/login';
            return;
        }
        app.replaceChildren(loginView());
        return;
    }
    if (location.hash === '#/login' || location.hash === '') {
        location.hash = '#/studies';
        return;
    }

    const route = resolveRoute(location.hash);
    for (const link of nav.querySelectorAll('a')) {
        link.classList.toggle('active', link.dataset.route === route.name);
    }
    try {
        const view = await route.view();
        // 불러오는 동안 다른 화면으로 이동했으면 버린다.
        if (sequence === renderSequence) {
            app.replaceChildren(view);
        }
    } catch (error) {
        if (sequence === renderSequence && error.status !== 401) {
            app.replaceChildren(el('section', {},
                el('p', { class: 'error', role: 'alert' }, error.message),
                el('p', {}, el('a', { href: '#/studies' }, '스터디 목록으로'))));
        }
    }
}

document.getElementById('logout').addEventListener('click', async () => {
    try {
        await api('POST', '/api/auth/logout');
    } catch (error) {
        if (error.status !== 401) {
            toast(error.message, true);
            return;
        }
    }
    me = null;
    toast('로그아웃했습니다.');
    go('#/login');
});

window.addEventListener('hashchange', () => {
    window.scrollTo(0, 0);
    render();
});

(async function start() {
    try {
        me = await api('GET', '/api/members/me');
    } catch (error) {
        if (error.status !== 401) {
            toast(error.message, true);
        }
    }
    render();
})();
