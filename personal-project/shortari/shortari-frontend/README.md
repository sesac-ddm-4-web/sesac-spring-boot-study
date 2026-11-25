# Shortari 프론트엔드

React + Vite 기반 레트로 게임 스타일 UI입니다. Spring Boot 백엔드와 연결합니다.

## 실행

```bash
npm install
npm run dev
```

브라우저에서 http://localhost:5173 로 접속하세요. 백엔드는 http://localhost:8080 에서 실행되어야 합니다.

## 백엔드 API 전제

- `POST /api/urls` — `{originalUrl, displayName, mode}` → ShortUrlResponse
- `GET /api/urls` — ShortUrlResponse 배열
- `PUT /api/urls/{id}/name` — `{displayName}`
- `DELETE /api/urls/{id}`
- `GET /r/{shortCode}` — 원본 사이트로 302 리다이렉트

`ShortUrlResponse`는 `id`, `shortUrl`, `shortCode`, `displayName`, `createdAt` 필드를 포함해야 합니다.

개발 서버는 `/api`, `/r` 경로를 localhost:8080으로 프록시하므로 CORS 설정 없이 개발할 수 있습니다. 실제 단축 URL은 백엔드의 baseUrl 설정을 따릅니다.

## 주의

- 이 프로젝트에는 로그인 기능이 없습니다. 링크 목록과 수정·삭제 API는 백엔드 접근 권한에 따라 모두에게 노출될 수 있습니다.
- 링크 이름 수정은 현재 백엔드의 `/api/urls/{id}/name` API를 호출합니다. `SHORT` 모드의 이름 수정 허용 여부는 백엔드 정책과 일치시켜 주세요.
- 배포 시 `VITE_API_BASE_URL`을 설정할 수 있습니다. Vite 환경변수는 비밀 저장소가 아니므로 비밀번호를 넣지 마세요.


## 이름으로 바로가기
기존 `/api/urls` 목록 조회 응답의 `displayName`, `shortUrl`을 사용합니다.
이름을 정확히 입력하고 Enter 또는 '이동하기'를 누르면 해당 `shortUrl`로 이동합니다.
백엔드 수정은 필요하지 않습니다. 이름이 없거나 목록 조회에 실패하면 이동하지 않습니다.
이 기능은 Shortari 페이지 안에서만 작동하며 Chrome 주소창을 변경하지 않습니다.
