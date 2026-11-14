'use strict';
// CartController.java의 실제 엔드포인트와 일치하도록 설정
const ROUTES = {
  collections: '/collections',
  external: '/external/products',
  cart: '/cart',
  cartAdd: '/cart/items',
  cartPreview: (coupon) => `/cart/preview?coupon=${encodeURIComponent(coupon)}`,
  cartItem: (id) => `/cart/items/${id}`
};
const $ = (id) => document.getElementById(id);
const money = (n) => `${Number(n ?? 0).toLocaleString('ko-KR')}원`;
const state = { wishlist: [], cart: null, shop: [], skip: 0, limit: 12, total: 0, search: '', tab: 'home' };
let toastTimer;
function toast(text) { const el = $('toast'); el.textContent = text; el.classList.add('visible'); clearTimeout(toastTimer); toastTimer = setTimeout(() => el.classList.remove('visible'), 3500); }
function errorMessage(status, fallback) { return ({400:'입력값을 확인해 주세요.',404:'요청한 상품을 찾을 수 없어요.',409:'중복 등록 또는 수량·재고 제한에 걸렸어요.',502:'외부 상품 서버에 문제가 있어요. 잠시 후 다시 시도해 주세요.'})[status] || fallback || `요청 실패 (HTTP ${status})`; }
async function request(path, options = {}) {
  let response;
  try { response = await fetch(path, { ...options, headers: { ...(options.body ? {'Content-Type':'application/json'} : {}), ...(options.headers || {}) } }); }
  catch { throw new Error('서버에 연결할 수 없어요. Spring Boot 실행 상태를 확인해 주세요.'); }
  if (!response.ok) {
    let msg = ''; try { const data = await response.json(); msg = data.message || data.detail || ''; } catch {}
    throw new Error(errorMessage(response.status, msg));
  }
  if (response.status === 204) return null;
  const raw = await response.text(); return raw ? JSON.parse(raw) : null;
}
function empty(text, icon='🥊') { const el = document.createElement('div'); el.className='empty'; const a=document.createElement('span');a.textContent=icon;const b=document.createElement('strong');b.textContent=text;el.append(a,b);return el; }
function setTab(tab) {
  state.tab = tab;
  document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.toggle('active',btn.dataset.tab===tab));
  document.querySelectorAll('.view').forEach(view => { const on=view.id===`view-${tab}`;view.classList.toggle('active',on);view.hidden=!on; });
  const titles={home:'우리 체육관',shop:'아이템 상점',wishlist:'위시리스트',add:'직접 등록',recommend:'예산 추천',cart:'장바구니'};
  $('current-location').textContent=titles[tab];window.scrollTo({top:0,behavior:'smooth'});
  if(tab==='shop') loadShop(); if(tab==='wishlist') loadWishlist(); if(tab==='cart') loadCart();
}
document.querySelectorAll('[data-tab],[data-go]').forEach(el=>el.addEventListener('click',()=>setTab(el.dataset.tab||el.dataset.go)));
function productCard(item, origin) {
  const external=origin==='shop'; const card=document.createElement('article');card.className='product';
  const media=document.createElement('div');media.className='product-media';
  const tag=document.createElement('span');tag.className='tag';tag.textContent=external?'SHOP ITEM':item.sourceType==='EXTERNAL'?'IMPORTED':'MY ITEM';media.append(tag);
  if(external && item.thumbnail){const img=document.createElement('img');img.src=item.thumbnail;img.alt=item.title||'상품 이미지';img.loading='lazy';media.append(img);}else{const icon=document.createElement('span');icon.className='fallback';icon.textContent='🥊';media.append(icon);}
  const body=document.createElement('div');body.className='product-body';const name=document.createElement('h3');name.textContent=external?item.title:item.name;
  const meta=document.createElement('div');meta.className='product-meta';meta.textContent=external?`외부 ID ${item.id} · 재고 ${item.stock??'미확인'}`:`내 상품 ID ${item.id}${item.stock===0?' · 품절':''}`;
  const price=document.createElement('div');price.className='product-price';price.textContent=money(external?Math.round(Number(item.price)*1000):item.price);
  const actions=document.createElement('div');actions.className='product-actions';
  if(external){const importBtn=document.createElement('button');importBtn.className='primary';importBtn.textContent='♡ 위시리스트에 담기';importBtn.addEventListener('click',async()=>{importBtn.disabled=true;try{await request(`${ROUTES.collections}/import`,{method:'POST',body:JSON.stringify({externalId:item.id})});toast('위시리스트에 가져왔어요!');await loadWishlist();}catch(e){toast(e.message);}finally{importBtn.disabled=false;}});actions.append(importBtn);}
  else {const cartBtn=document.createElement('button');cartBtn.className='primary';cartBtn.textContent=item.stock===0?'품절':'🛒 장바구니 담기';cartBtn.disabled=item.stock===0;cartBtn.addEventListener('click',async()=>{cartBtn.disabled=true;try{await request(ROUTES.cartAdd,{method:'POST',body:JSON.stringify({collectionItemId:item.id,quantity:1})});toast('장바구니에 1개 담았어요!');await loadCart(false);}catch(e){toast(e.message);}finally{cartBtn.disabled=item.stock===0;}});actions.append(cartBtn);}
  body.append(name,meta,price,actions);card.append(media,body);return card;
}
async function loadWishlist(){const target=$('wishlist-grid');try{const data=await request(ROUTES.collections);if(!Array.isArray(data))throw new Error('위시리스트 응답 형식이 올바르지 않아요.');state.wishlist=data;$('wishlist-count').textContent=data.length;$('stat-wishlist').textContent=data.length;target.replaceChildren(...(data.length?data.map(i=>productCard(i,'wishlist')):[empty('아직 담아둔 아이템이 없어요.','💗')]));}catch(e){target.replaceChildren(empty(e.message,'⚠️'));}}
async function loadShop(){const target=$('shop-grid');target.replaceChildren(empty('상품을 불러오는 중이에요...','🛍️'));try{let url;if(state.search){url=`${ROUTES.external}/search?q=${encodeURIComponent(state.search)}`;}else{url=`${ROUTES.external}?limit=${state.limit}&skip=${state.skip}`;}const data=await request(url);state.shop=data.products||[];state.total=Number(data.total||0);$('shop-info').textContent=`${state.search?'검색 결과':'전체 상품'} ${state.total}개`;$('shop-page').textContent=state.search?'검색 결과':`${Math.floor(state.skip/state.limit)+1} 페이지`;$('shop-prev').disabled=!!state.search||state.skip===0;$('shop-next').disabled=!!state.search||state.skip+state.limit>=state.total;target.replaceChildren(...(state.shop.length?state.shop.map(i=>productCard(i,'shop')):[empty('검색된 상품이 없어요.','🔎')]));}catch(e){target.replaceChildren(empty(e.message,'⚠️'));}}
$('search-form').addEventListener('submit',e=>{e.preventDefault();state.search=$('search-input').value.trim();state.skip=0;loadShop();});$('shop-reset').addEventListener('click',()=>{state.search='';state.skip=0;$('search-input').value='';loadShop();});$('shop-prev').addEventListener('click',()=>{state.skip=Math.max(0,state.skip-state.limit);loadShop();});$('shop-next').addEventListener('click',()=>{state.skip+=state.limit;loadShop();});$('wishlist-refresh').addEventListener('click',loadWishlist);
$('add-form').addEventListener('submit',async e=>{e.preventDefault();const name=$('item-name').value.trim(),price=Number($('item-price').value),msg=$('add-message');msg.textContent='';msg.classList.remove('error');if(!name||!Number.isInteger(price)||price<0||price>2147483647){msg.textContent='이름과 올바른 가격을 입력해 주세요.';msg.classList.add('error');return;}try{await request(ROUTES.collections,{method:'POST',body:JSON.stringify({name,price})});$('add-form').reset();toast('위시리스트에 등록했어요!');await loadWishlist();setTab('wishlist');}catch(err){msg.textContent=err.message;msg.classList.add('error');}});
$('budget-form').addEventListener('submit',async e=>{e.preventDefault();const budget=Number($('budget').value),target=$('recommend-grid');if(!Number.isInteger(budget)||budget<0||budget>2147483647){target.replaceChildren(empty('올바른 예산을 입력해 주세요.'));return;}try{const data=await request(`${ROUTES.collections}/recommend?budget=${budget}`);$('recommend-count').textContent=`${data.length}개 추천`;target.replaceChildren(...(data.length?data.map(i=>productCard(i,'wishlist')):[empty('예산에 맞는 상품이 없어요.','🎯')]));}catch(err){target.replaceChildren(empty(err.message,'⚠️'));}});
// 장바구니의 응답 DTO와 매핑은 실제 CartController에 맞게 ROUTES 및 normalizeCart를 조정할 수 있습니다.
function normalizeCart(data){if(Array.isArray(data))return {items:data,itemCount:data.length,totalQuantity:data.reduce((s,i)=>s+Number(i.quantity||0),0),subtotal:data.reduce((s,i)=>s+Number(i.price||0)*Number(i.quantity||0),0)};const items=data?.items||data?.cartItems||[];return {...data,items,itemCount:data?.itemCount??items.length,totalQuantity:data?.totalQuantity??items.reduce((s,i)=>s+Number(i.quantity||0),0)};}
async function loadCart(render=true){const target=$('cart-list');try{const data=normalizeCart(await request(ROUTES.cart));state.cart=data;$('nav-cart-count').textContent=data.itemCount;$('stat-cart').textContent=data.itemCount;if(render){target.replaceChildren(...(data.items.length?data.items.map(cartRow):[empty('장바구니가 비어 있어요.','🛒')]));}await loadPreview();}catch(e){if(render)target.replaceChildren(empty(e.message,'⚠️'));$('cart-message').textContent=e.message;$('cart-message').classList.add('error');}}
function cartRow(item){const row=document.createElement('div');row.className='cart-row';const thumb=document.createElement('div');thumb.className='cart-thumb';thumb.textContent='🥊';const details=document.createElement('div');details.className='cart-details';const name=document.createElement('strong');name.textContent=item.name;const sub=document.createElement('small');sub.textContent=`${money(item.price)} × ${item.quantity} = ${money(Number(item.price)*Number(item.quantity))}`;details.append(name,sub);const controls=document.createElement('div');controls.className='cart-controls';for(const [text,qty] of [['−',Number(item.quantity)-1],['+',Number(item.quantity)+1]]){const btn=document.createElement('button');btn.textContent=text;btn.disabled=qty<1||qty>5||(item.stock!=null&&qty>item.stock);btn.addEventListener('click',async()=>{try{await request(ROUTES.cartItem(item.id),{method:'PUT',body:JSON.stringify({quantity:qty})});await loadCart();}catch(e){toast(e.message);}});controls.append(btn);if(text==='−'){const count=document.createElement('strong');count.textContent=item.quantity;controls.append(count);}}const remove=document.createElement('button');remove.textContent='✕';remove.className='remove';remove.title='삭제';remove.addEventListener('click',async()=>{try{await request(ROUTES.cartItem(item.id),{method:'DELETE'});await loadCart();toast('장바구니에서 삭제했어요.');}catch(e){toast(e.message);}});controls.append(remove);row.append(thumb,details,controls);return row;}
async function loadPreview(){const msg=$('cart-message');msg.textContent='';msg.classList.remove('error');try{const data=await request(ROUTES.cartPreview($('coupon').value));$('sum-subtotal').textContent=money(data.subtotal);$('sum-discount').textContent=`− ${money(data.discount)}`;$('sum-discounted').textContent=money(data.discountedSubtotal);$('sum-shipping').textContent=money(data.shippingFee);$('sum-total').textContent=money(data.finalTotal);}catch(e){msg.textContent=e.message;msg.classList.add('error');}}
$('coupon').addEventListener('change',loadPreview);$('cart-refresh').addEventListener('click',()=>loadCart());$('reload-all').addEventListener('click',async()=>{await Promise.all([loadWishlist(),loadCart(false)]);if(state.tab==='shop')await loadShop();toast('새로고침했어요!');});
loadWishlist();loadCart(false);
