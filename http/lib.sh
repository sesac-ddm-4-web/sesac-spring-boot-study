#!/usr/bin/env bash
# =====================================================================
# 공통 함수: 요청 보내기 · 결과 검사 · 요약
# 다른 스크립트에서 source 해서 사용합니다. (직접 실행하지 않음)
# =====================================================================

HOST="${HOST:-http://localhost:8080}"
PASS=0
FAIL=0

STATUS=""     # 마지막 응답의 상태 코드 (서버에 연결 못 하면 000)
BODY=""       # 마지막 응답 본문
LOCATION=""   # 마지막 응답의 Location 헤더

# request METHOD PATH [JSON]
# 예) request GET "/api/wishlist/items"
#     request POST "/api/wishlist/items" '{"name": "데미안", "price": 12000}'
# 한글이 깨지지 않도록 JSON은 임시 파일에 UTF-8로 쓴 뒤 보냅니다.
request() {
  local method="$1" path="$2" json="${3:-}"
  local headers_file body_file data_file=""
  headers_file="$(mktemp)"
  body_file="$(mktemp)"

  local args=(-s -o "$body_file" -D "$headers_file" -w '%{http_code}' -X "$method" "$HOST$path")
  if [ -n "$json" ]; then
    data_file="$(mktemp)"
    printf '%s' "$json" > "$data_file"
    args+=(-H "Content-Type: application/json" --data-binary "@$data_file")
  fi

  STATUS="$(curl "${args[@]}")"
  BODY="$(cat "$body_file")"
  LOCATION="$(grep -i '^location:' "$headers_file" | tr -d '\r' | cut -d' ' -f2)"
  rm -f "$headers_file" "$body_file" "$data_file"
}

# expect "설명" 기대_상태코드 [본문에_있어야_할_문자열 ...]
expect() {
  local name="$1" expected="$2"
  shift 2
  local reasons=()
  if [ "$STATUS" != "$expected" ]; then
    reasons+=("상태 코드 $STATUS (기대 $expected)")
  fi
  local text
  for text in "$@"; do
    if [[ "$BODY" != *"$text"* ]]; then
      reasons+=("본문에 '$text' 없음")
    fi
  done
  report "$name" "${reasons[@]}"
}

# expect_equal "설명" 실제값 기대값
expect_equal() {
  if [ "$2" == "$3" ]; then
    report "$1"
  else
    report "$1" "값 '$2' (기대 '$3')"
  fi
}

# expect_not_equal "설명" 실제값 기대하지_않는_값
expect_not_equal() {
  if [ "$2" != "$3" ]; then
    report "$1"
  else
    report "$1" "값이 '$3'이면 안 됨"
  fi
}

# 결과 출력 (이유가 없으면 통과)
report() {
  local name="$1"
  shift
  if [ $# -eq 0 ]; then
    PASS=$((PASS + 1))
    printf '  \033[32m✓\033[0m %s\n' "$name"
  else
    FAIL=$((FAIL + 1))
    printf '  \033[31m✗ %s\033[0m\n' "$name"
    local reason
    for reason in "$@"; do
      printf '      - %s\n' "$reason"
    done
    printf '      응답: %.300s\n' "$BODY"
  fi
}

# ---- 응답 JSON에서 값 꺼내기 (jq 없이 간단히) ----

# 첫 번째 숫자 필드 값: json_number id → 3
json_number() {
  grep -o "\"$1\":-\?[0-9]*" <<< "$BODY" | head -1 | cut -d: -f2
}

# 첫 번째 문자열 필드 값: json_string sourceType → MANUAL
json_string() {
  grep -o "\"$1\":\"[^\"]*\"" <<< "$BODY" | head -1 | sed "s/\"$1\":\"//; s/\"\$//"
}

# 객체 개수 (id 필드 개수로 셈): count_ids → 7
count_ids() {
  grep -o '"id":' <<< "$BODY" | wc -l | tr -d ' '
}

# 모든 name 값을 쉼표로 이어 붙임: names → 데미안,어린 왕자
names() {
  grep -o '"name":"[^"]*"' <<< "$BODY" | sed 's/"name":"//; s/"$//' | paste -sd, -
}

section() {
  printf '\n\033[1m%s\033[0m\n' "$1"
}

# 서버가 켜져 있는지 먼저 확인
require_server() {
  request GET "/api/wishlist/items"
  if [ "$STATUS" == "000" ]; then
    printf '\033[31m서버에 연결할 수 없습니다: %s\033[0m\n' "$HOST"
    printf './gradlew bootRun 으로 서버를 먼저 실행하세요.\n'
    exit 1
  fi
}

summary() {
  printf '\n----------------------------------------\n'
  printf '결과: \033[32m통과 %d\033[0m / \033[31m실패 %d\033[0m\n' "$PASS" "$FAIL"
  [ "$FAIL" -eq 0 ]
}
