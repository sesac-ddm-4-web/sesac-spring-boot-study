#!/usr/bin/env bash
# =====================================================================
# 위시리스트 — 1차 과제 기능 + 2단계(형식 오류) + 5단계(가져오기·수정·삭제)
# 실행: 프로젝트 루트에서  bash http/wishlist.sh
# 준비: 서버를 "새로" 켠 상태에서 실행하세요. (메모리 저장이라 id가 1부터 시작해야 함)
#       D 구간(가져오기)은 인터넷 연결이 필요합니다.
# 순서: 추천(C)은 외부 상품을 가져오기 전에 확인해서, 외부 가격에 결과가 흔들리지 않게 합니다.
# =====================================================================
cd "$(dirname "$0")" && source ./lib.sh
require_server

request GET "/api/wishlist/items"
if [ "$(count_ids)" != "0" ]; then
  printf '\033[33m위시리스트가 비어 있지 않습니다. 서버를 재시작한 뒤 실행하세요.\033[0m\n'
  exit 1
fi


section "A. 직접 등록 (201 + Location)"

request POST "/api/wishlist/items" '{"name": "  데미안  ", "price": 12000}'
MANUAL_ID="$(json_number id)"
expect "직접 등록 → 201" 201 '"name":"데미안"' '"sourceType":"MANUAL"' '"externalId":null' '"stock":null'
expect_equal "Location 헤더" "$LOCATION" "/api/wishlist/items/$MANUAL_ID"

request POST "/api/wishlist/items" '{"name": "무료 전자책 샘플", "price": 0}'
expect "0원 아이템 → 201" 201 '"price":0'

request POST "/api/wishlist/items" '{"name": "어린 왕자", "price": 5000}'
request POST "/api/wishlist/items" '{"name": "노인과 바다", "price": 5000}'
request POST "/api/wishlist/items" '{"name": "동물농장", "price": 8000}'
expect "추천 확인용 데이터 3개 추가 → 201" 201


section "B. 등록 실패 (400, 목록 변화 없음)"

request POST "/api/wishlist/items" '{"name": "   ", "price": 1000}'
expect "공백뿐인 이름 → 400" 400 '"message"'

request POST "/api/wishlist/items" '{"name": "test", "price": -1}'
expect "음수 가격 → 400" 400

request POST "/api/wishlist/items" '{"name": "test"}'
expect "가격 누락 → 400" 400

request POST "/api/wishlist/items" '{"name": "test", "price": }'
expect "깨진 JSON → 400 + message (2단계 형식 오류 통일)" 400 '"message"'

request GET "/api/wishlist/items"
expect_equal "실패한 등록 후에도 5개 그대로" "$(count_ids)" 5


section "C. 예산 추천 (1차 과제) — 외부 상품을 가져오기 전에 확인"

request GET "/api/wishlist/recommendations?budget=5000"
expect "예산 5,000원 → 200" 200
expect_equal "경계값 포함·가격순·같은 가격은 등록순·최대 3개" "$(names)" "무료 전자책 샘플,어린 왕자,노인과 바다"

request GET "/api/wishlist/recommendations?budget=0"
expect "예산 0원 → 1개" 200 '"count":1'

request GET "/api/wishlist/recommendations?budget=-1"
expect "음수 예산 → 400" 400

request GET "/api/wishlist/recommendations"
expect "예산 누락 → 400 + message (2단계 형식 오류 통일)" 400 '"message"'

request GET "/api/wishlist/items"
expect_equal "추천 후 원본 순서 그대로" "$(names | cut -d, -f1-2)" "데미안,무료 전자책 샘플"


section "D. 외부 상품 가져오기 (5단계, 인터넷 필요)"

request POST "/api/wishlist/items/import" '{"externalId": 1}'
IMPORTED_ID="$(json_number id)"
expect "외부 상품 1번 가져오기 → 201" 201 '"sourceType":"EXTERNAL"' '"externalId":1'
expect_equal "Location 헤더" "$LOCATION" "/api/wishlist/items/$IMPORTED_ID"
expect_equal "내 위시리스트 id는 6 (외부 id 1과 따로)" "$IMPORTED_ID" 6

request POST "/api/wishlist/items/import" '{"externalId": 1}'
expect "같은 외부 상품 다시 → 409" 409 '"message"'

request POST "/api/wishlist/items/import" '{"externalId": 2, "name": "조작한 이름", "price": 1}'
expect "요청에 name·price를 넣어도 → 201" 201
expect_not_equal "요청의 name 무시" "$(json_string name)" "조작한 이름"
expect_not_equal "요청의 price 무시" "$(json_number price)" 1

request POST "/api/wishlist/items/import" '{"externalId": 99999}'
expect "외부에 없는 상품 → 404" 404

request POST "/api/wishlist/items/import" '{}'
expect "externalId 누락 → 400" 400

request POST "/api/wishlist/items/import" '{"externalId": 0}'
expect "externalId 0 → 400" 400


section "E. 조회"

request GET "/api/wishlist/items"
expect "전체 목록 → 200" 200
expect_equal "7개 (직접 5 + 가져온 2)" "$(count_ids)" 7
expect_equal "등록 순서 (첫 번째 데미안)" "$(names | cut -d, -f1)" "데미안"

request GET "/api/wishlist/items/$IMPORTED_ID"
expect "가져온 아이템 단건 조회 → 200" 200 '"externalId":1'

request GET "/api/wishlist/items/999"
expect "없는 id → 404" 404

request GET "/api/wishlist/items/abc"
expect "id=abc → 400" 400


section "F. 수정 (5단계)"

request PUT "/api/wishlist/items/$MANUAL_ID" '{"name": "데미안 (개정판)", "price": 13000}'
expect "직접 등록 아이템 수정 → 200" 200 '"name":"데미안 (개정판)"' '"price":13000'

request PUT "/api/wishlist/items/$IMPORTED_ID" '{"name": "바꾼 이름", "price": 1}'
expect "가져온 아이템 수정 → 409" 409

request PUT "/api/wishlist/items/$MANUAL_ID" '{"name": " ", "price": 13000}'
expect "수정 값 규칙 위반 → 400" 400

request GET "/api/wishlist/items/$MANUAL_ID"
expect "실패한 수정 후 값 유지" 200 '"name":"데미안 (개정판)"'

request PUT "/api/wishlist/items/999" '{"name": "x", "price": 1}'
expect "없는 id 수정 → 404" 404


section "G. 삭제 (5단계)"

request DELETE "/api/wishlist/items/$MANUAL_ID"
expect "삭제 → 204" 204
expect_equal "본문 없음" "$BODY" ""

request GET "/api/wishlist/items/$MANUAL_ID"
expect "삭제한 아이템 조회 → 404" 404

request DELETE "/api/wishlist/items/$MANUAL_ID"
expect "같은 아이템 다시 삭제 → 404" 404


summary
