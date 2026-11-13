#!/usr/bin/env bash
# =====================================================================
# 장바구니 · 쿠폰 · 배송비 미리보기 — 7·8단계
# 실행: 프로젝트 루트에서  bash http/cart.sh
# 준비: 서버를 "새로" 켠 상태에서 실행하세요. (위시리스트·장바구니가 비어 있어야 함)
#       외부 상품 가져오기 구간은 인터넷 연결이 필요합니다.
# =====================================================================
cd "$(dirname "$0")" && source ./lib.sh
require_server

request GET "/api/wishlist/items"
if [ "$(count_ids)" != "0" ]; then
  printf '\033[33m위시리스트가 비어 있지 않습니다. 서버를 재시작한 뒤 실행하세요.\033[0m\n'
  exit 1
fi


section "A. 준비: 위시리스트에 상품 등록"

request POST "/api/wishlist/items" '{"name": "데미안 양장 세트", "price": 55000}'
BOOK_ID="$(json_number id)"
expect "직접 등록 (55,000원) → 201" 201

request POST "/api/wishlist/items" '{"name": "어린 왕자", "price": 5000}'
SMALL_ID="$(json_number id)"
expect "직접 등록 (5,000원) → 201" 201

request POST "/api/wishlist/items/import" '{"externalId": 1}'
EXTERNAL_ID="$(json_number id)"
EXTERNAL_STOCK="$(json_number stock)"
expect "외부 상품 1번 가져오기 → 201" 201 '"sourceType":"EXTERNAL"'


section "B. 빈 장바구니"

request GET "/api/cart"
expect "장바구니 조회 → 200, 비어 있음" 200 '"items":[]' '"subtotal":0'

request GET "/api/cart/preview"
expect "미리보기 (쿠폰 생략 = NONE) → 모든 금액 0" 200 '"subtotal":0' '"shipping":0' '"finalAmount":0' '"coupon":"NONE"'

request GET "/api/cart/preview?couponCode=PERCENT10"
expect "빈 장바구니에 PERCENT10 → 400" 400 '"message"'

request GET "/api/cart/best-coupon"
expect "빈 장바구니 최저 쿠폰 → NONE" 200 '"coupon":"NONE"'


section "C. 담기 (같은 상품은 합산, 최대 5개)"

request POST "/api/cart/items" "{\"wishlistItemId\": $BOOK_ID, \"quantity\": 1}"
LINE_ID="$(json_number id)"
expect "처음 담기 → 201 (새 줄)" 201 '"quantity":1' '"unitPrice":55000'
expect_equal "Location 헤더" "$LOCATION" "/api/cart/items/$LINE_ID"

request POST "/api/cart/items" "{\"wishlistItemId\": $BOOK_ID, \"quantity\": 1}"
expect "같은 상품 또 담기 → 200 (합산, 새 줄 아님)" 200 '"quantity":2'
expect_equal "같은 장바구니 행 id" "$(json_number id)" "$LINE_ID"

request GET "/api/cart"
expect "장바구니: 1종, 합계 110,000원" 200 '"kindCount":1' '"totalQuantity":2' '"subtotal":110000'

request GET "/api/cart/items/$LINE_ID"
expect "장바구니 행 조회 → 200" 200 '"lineTotal":110000'

request POST "/api/cart/items" "{\"wishlistItemId\": $BOOK_ID, \"quantity\": 4}"
expect "합산하면 6개 → 409" 409 '"message"'

request POST "/api/cart/items" "{\"wishlistItemId\": $SMALL_ID, \"quantity\": 0}"
expect "수량 0 → 400" 400

request POST "/api/cart/items" "{\"wishlistItemId\": $SMALL_ID, \"quantity\": 6}"
expect "수량 6 → 400" 400

request POST "/api/cart/items" "{\"wishlistItemId\": $SMALL_ID}"
expect "수량 누락 → 400" 400

request POST "/api/cart/items" '{"quantity": 1}'
expect "wishlistItemId 누락 → 400" 400

request POST "/api/cart/items" '{"wishlistItemId": 999, "quantity": 1}'
expect "위시리스트에 없는 상품 → 404" 404

request GET "/api/cart/items/999"
expect "없는 장바구니 행 → 404" 404

request GET "/api/cart"
expect "실패한 담기 후에도 그대로 (2개, 110,000원)" 200 '"totalQuantity":2' '"subtotal":110000'


section "D. 쿠폰·배송비 미리보기 (상품 합계 110,000원)"

request GET "/api/cart/preview?couponCode=NONE"
expect "NONE → 할인 0, 배송비 0, 최종 110,000" 200 '"discount":0' '"shipping":0' '"finalAmount":110000'

request GET "/api/cart/preview?couponCode=PERCENT10"
expect "PERCENT10 → 할인 11,000, 배송비 5,000, 최종 104,000" 200 '"discount":11000' '"discountedSubtotal":99000' '"shipping":5000' '"finalAmount":104000'

request GET "/api/cart/preview?couponCode=FIXED15000"
expect "FIXED15000 → 할인 15,000, 배송비 5,000, 최종 100,000" 200 '"discount":15000' '"shipping":5000' '"finalAmount":100000'

request GET "/api/cart/preview?couponCode=%20percent10%20"
expect "소문자·앞뒤 공백도 허용 → PERCENT10" 200 '"coupon":"PERCENT10"'

request GET "/api/cart/preview?couponCode=HALF50"
expect "모르는 쿠폰 → 400" 400 '"message"'

request GET "/api/cart/best-coupon"
expect "최저 금액 쿠폰 → 200" 200
expect_equal "최저는 FIXED15000" "$(json_string coupon)" "FIXED15000"
expect_equal "최저 최종 금액 100,000" "$(json_number finalAmount)" 100000

request GET "/api/cart"
expect "미리보기 후에도 장바구니 그대로" 200 '"totalQuantity":2' '"subtotal":110000'


section "E. 담을 때 복사한 값 유지 (원본 수정·삭제)"

request PUT "/api/wishlist/items/$BOOK_ID" '{"name": "데미안 양장 세트", "price": 60000}'
expect "위시리스트 원본 가격 수정 → 200" 200

request GET "/api/cart/items/$LINE_ID"
expect "장바구니는 처음 복사한 55,000원 유지" 200 '"unitPrice":55000'

request DELETE "/api/wishlist/items/$BOOK_ID"
expect "위시리스트 원본 삭제 → 204" 204

request GET "/api/cart/items/$LINE_ID"
expect "원본이 삭제돼도 장바구니 행은 남음" 200 '"unitPrice":55000'

request PATCH "/api/cart/items/$LINE_ID" '{"quantity": 3}'
expect "원본 삭제 후에도 행 id로 수량 변경 → 200" 200 '"quantity":3'

request PATCH "/api/cart/items/$LINE_ID" '{"quantity": 6}'
expect "수량 6으로 변경 → 400" 400

request PATCH "/api/cart/items/$LINE_ID" '{}'
expect "수량 누락 → 400" 400

request PATCH "/api/cart/items/999" '{"quantity": 1}'
expect "없는 행 수량 변경 → 404" 404

request GET "/api/cart/items/$LINE_ID"
expect "실패한 변경 후에도 3개 그대로" 200 '"quantity":3'

request POST "/api/cart/items" "{\"wishlistItemId\": $BOOK_ID, \"quantity\": 1}"
expect "삭제된 원본으로 새로 담기 → 404" 404


section "F. 행 삭제 후 다시 담으면 현재 값으로 새로 복사"

request POST "/api/cart/items" "{\"wishlistItemId\": $SMALL_ID, \"quantity\": 1}"
SMALL_LINE_ID="$(json_number id)"
expect "어린 왕자 담기 → 201 (5,000원)" 201 '"unitPrice":5000'

request PUT "/api/wishlist/items/$SMALL_ID" '{"name": "어린 왕자", "price": 7000}'
request POST "/api/cart/items" "{\"wishlistItemId\": $SMALL_ID, \"quantity\": 1}"
expect "원본 가격 변경 후 합산 → 처음 복사한 5,000원 유지" 200 '"quantity":2' '"unitPrice":5000'

request DELETE "/api/cart/items/$SMALL_LINE_ID"
expect "장바구니 행 삭제 → 204" 204

request DELETE "/api/cart/items/$SMALL_LINE_ID"
expect "같은 행 다시 삭제 → 404" 404

request POST "/api/cart/items" "{\"wishlistItemId\": $SMALL_ID, \"quantity\": 1}"
expect "다시 담기 → 201, 현재 값 7,000원으로 새로 복사" 201 '"unitPrice":7000'
expect_not_equal "새 행 id" "$(json_number id)" "$SMALL_LINE_ID"


section "G. 외부 상품 (가져올 때의 재고 이하만)"

request POST "/api/cart/items" "{\"wishlistItemId\": $EXTERNAL_ID, \"quantity\": 1}"
if [ "$EXTERNAL_STOCK" == "0" ]; then
  expect "품절 외부 상품 담기 → 409" 409
else
  expect "외부 상품 담기 → 201, 재고 복사" 201 "\"stockLimit\":$EXTERNAL_STOCK"
  if [ "$EXTERNAL_STOCK" -lt 5 ]; then
    request POST "/api/cart/items" "{\"wishlistItemId\": $EXTERNAL_ID, \"quantity\": $EXTERNAL_STOCK}"
    expect "재고($EXTERNAL_STOCK개)를 넘게 합산 → 409" 409
  else
    printf '  - 외부 상품 재고가 %s개라 재고 초과는 여기서 확인할 수 없어요 (CartTest에서 확인)\n' "$EXTERNAL_STOCK"
  fi
fi


section "H. 상품 종류 최대 20종"

request GET "/api/cart"
KINDS="$(json_number kindCount)"
i=1
while [ "$KINDS" -lt 20 ]; do
  request POST "/api/wishlist/items" "{\"name\": \"종류 테스트 $i\", \"price\": 1000}"
  request POST "/api/cart/items" "{\"wishlistItemId\": $(json_number id), \"quantity\": 1}"
  KINDS=$((KINDS + 1))
  i=$((i + 1))
done
request GET "/api/cart"
expect "20종까지 담김" 200 '"kindCount":20'

request POST "/api/wishlist/items" '{"name": "21번째 종류", "price": 1000}'
request POST "/api/cart/items" "{\"wishlistItemId\": $(json_number id), \"quantity\": 1}"
expect "21번째 종류 → 409" 409

request POST "/api/cart/items" "{\"wishlistItemId\": $SMALL_ID, \"quantity\": 1}"
expect "20종이어도 이미 담긴 상품 합산은 가능 → 200" 200 '"quantity":2'


summary
