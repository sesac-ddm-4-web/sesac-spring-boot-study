#!/usr/bin/env bash
# =====================================================================
# 외부 상품 (DummyJSON) 목록·검색·상세 — 3단계
# 실행: 프로젝트 루트에서  bash http/external-products.sh
# 준비: ./gradlew bootRun 으로 서버 실행, 인터넷 연결 필요 (내 서버 → dummyjson.com)
# =====================================================================
cd "$(dirname "$0")" && source ./lib.sh
require_server


section "1. 목록·검색·상세 (성공)"

request GET "/api/external-products"
expect "목록 기본값 → 200" 200
expect_equal "기본 limit 10 → 상품 10개" "$(count_ids)" 10

request GET "/api/external-products?limit=3&skip=3"
expect "목록 limit=3&skip=3 → 200" 200
expect_equal "상품 3개" "$(count_ids)" 3
expect_equal "skip 3 → 첫 상품 id 4" "$(json_number id)" 4

request GET "/api/external-products?limit=20"
expect "limit 경계값 20 → 200" 200
expect_equal "상품 20개" "$(count_ids)" 20

request GET "/api/external-products/search?q=phone&limit=5"
expect "검색 q=phone → 200" 200
expect_not_equal "검색 결과 있음" "$(count_ids)" 0

request GET "/api/external-products/search?q=zzzzzz"
expect "검색 결과 없음 → 200 + 빈 목록" 200 '"products":[]'

request GET "/api/external-products/1"
expect "상세 id=1 → 200" 200 '"priceWon":' '"stock":'
expect_equal "price 9.99 → priceWon 9990 (1단위 = 1,000원)" "$(json_number priceWon)" 9990
if [[ "$BODY" == *discountPercentage* || "$BODY" == *minimumOrderQuantity* ]]; then
  report "사용하지 않는 필드는 응답에 없음" "discountPercentage 또는 minimumOrderQuantity가 있음"
else
  report "사용하지 않는 필드는 응답에 없음"
fi


section "2. 실패"

request GET "/api/external-products/99999"
expect "외부에 없는 상품 → 404" 404 '"message"'

request GET "/api/external-products/0"
expect "상품 id 0 → 400" 400

request GET "/api/external-products?limit=21"
expect "limit 21 → 400" 400

request GET "/api/external-products?limit=0"
expect "limit 0 → 400" 400

request GET "/api/external-products?skip=-1"
expect "skip -1 → 400" 400

request GET "/api/external-products?limit=abc"
expect "limit=abc → 400 + message (2단계 형식 오류 통일)" 400 '"message"'

request GET "/api/external-products/search"
expect "검색어 누락 → 400" 400

request GET "/api/external-products/search?q=%20%20"
expect "검색어 공백뿐 → 400" 400


summary
