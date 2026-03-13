import requests
import time
import sys
import os

BASE_URL = "http://localhost:8080"
TIMESTAMP = int(time.time())
CUSTOMER_EMAIL = f"customer_p5_{TIMESTAMP}@example.com"
CUSTOMER_PASS = "password123"
SELLER_EMAIL = "seller@example.com"
SELLER_PASS = "password123"

print("=========================================")
print("Starting Phase 5 End-to-End Verification")
print("=========================================")

# 1. Register and Authenticate Customer
print(f"\n[SETUP] Registering customer: {CUSTOMER_EMAIL}")
resp = requests.post(f"{BASE_URL}/api/v1/auth/register/customer", json={
    "email": CUSTOMER_EMAIL,
    "password": CUSTOMER_PASS,
    "fullName": "P5 Test Customer",
    "phone": "+84900000001"
})
if resp.status_code not in [200, 201]:
    print("Failed to register customer:", resp.text)
    sys.exit(1)
print("Customer registered.")

def login(email, password):
    resp = requests.post(f"{BASE_URL}/api/v1/auth/login", json={
        "email": email,
        "password": password
    })
    if resp.status_code != 200:
        print(f"Failed to login as {email}: {resp.text}")
        sys.exit(1)
    token = resp.json().get("accessToken") or resp.json().get("token")
    if not token:
        print("No token found in login response:", resp.text)
        sys.exit(1)
    return token

print(f"\n[AUTH] Logging in as Customer: {CUSTOMER_EMAIL}")
customer_token = login(CUSTOMER_EMAIL, CUSTOMER_PASS)
customer_headers = {"Authorization": f"Bearer {customer_token}"}

print(f"[AUTH] Logging in as Seller: {SELLER_EMAIL}")
seller_token = login(SELLER_EMAIL, SELLER_PASS)
seller_headers = {"Authorization": f"Bearer {seller_token}"}

# 2. Seed stock for seller's variants to ensure availability
print("\n[SETUP] Seeding stock for seller products...")
resp = requests.get(f"{BASE_URL}/api/v1/seller/products?size=10", headers=seller_headers)
if resp.status_code == 200:
    seller_prods = resp.json().get('content', [])
    seeded = 0
    for p in seller_prods:
        seller_prods_resp = requests.get(f"{BASE_URL}/api/v1/seller/products/{p['id']}", headers=seller_headers)
        if seller_prods_resp.status_code == 200:
            prod_detail = seller_prods_resp.json()
            for v in prod_detail.get('variants', []):
                if v.get('isActive', True):
                    requests.put(
                        f"{BASE_URL}/api/v1/seller/products/variants/{v['id']}/inventory",
                        headers=seller_headers,
                        json={"stockQuantity": 50}
                    )
                    requests.put(
                        f"{BASE_URL}/api/v1/seller/products/variants/{v['id']}/pricing",
                        headers=seller_headers,
                        json={"basePrice": 1000, "discountPrice": 799}
                    )
                    seeded += 1
                    if seeded >= 5:
                        break
        if seeded >= 5:
            break
    print(f"Seeded stock for {seeded} variant(s).")

print("\n[STEP 1] Fetching public catalog...")
resp = requests.get(f"{BASE_URL}/api/v1/public/catalog/search?size=10")
if resp.status_code != 200:
    print("Failed to fetch catalog:", resp.text)
    sys.exit(1)

catalog_page = resp.json()
products = catalog_page.get('content', [])

if not products or len(products) == 0:
    print("No products in catalog. Cannot run test.")
    sys.exit(1)

# The search response (ProductResponseDto) only has inStock flag + slug - no variant IDs.
# Filter by inStock=True, then fetch full detail via slug to get variant IDs.
in_stock_products = [p for p in products if p.get('inStock', False)]
if not in_stock_products:
    print("No products marked as inStock in catalog.")
    sys.exit(1)

def get_product_detail(slug):
    r = requests.get(f"{BASE_URL}/api/v1/public/catalog/products/{slug}")
    if r.status_code != 200:
        return None
    return r.json()

detail1 = get_product_detail(in_stock_products[0]['slug'])
if not detail1 or not detail1.get('variants'):
    print("Could not load product 1 detail or no variants found.")
    sys.exit(1)
variant_id_1 = detail1['variants'][0]['id']
shop_id_1 = detail1.get('shopId') or detail1['variants'][0].get('shopId')
print(f"Product 1: {detail1['title']} | Variant: {variant_id_1}")

# Try to get a product from a different shop for multi-shop split test
variant_id_2 = None
for p in in_stock_products[1:]:
    d = get_product_detail(p['slug'])
    if d and d.get('variants'):
        variant_id_2 = d['variants'][0]['id']
        print(f"Product 2: {d['title']} | Variant: {variant_id_2}")
        break

# 3. Add to Cart
print("\n[STEP 2] Adding items to cart...")
resp = requests.post(f"{BASE_URL}/api/v1/customer/cart/items", headers=customer_headers, json={"variantId": variant_id_1, "quantity": 1})
if resp.status_code != 200:
    print("Failed to add item 1 to cart:", resp.text)
    sys.exit(1)
print(f"Cart: {resp.json().get('totalItems')} items, Total: {resp.json().get('totalPrice')}")

if variant_id_2:
    resp = requests.post(f"{BASE_URL}/api/v1/customer/cart/items", headers=customer_headers, json={"variantId": variant_id_2, "quantity": 1})
    if resp.status_code != 200:
        print("Failed to add item 2 to cart:", resp.text)
        # Non-fatal, continue with 1 item
    else:
        print(f"Cart after item 2: {resp.json().get('totalItems')} items, Total: {resp.json().get('totalPrice')}")

# 4. Confirm Cart
resp = requests.get(f"{BASE_URL}/api/v1/customer/cart", headers=customer_headers)
cart = resp.json()
print(f"\n[CART] Verified cart has {cart['totalItems']} items, total: {cart['totalPrice']}")

# 5. Checkout
print("\n[STEP 3] Checking out entire cart...")
resp = requests.post(f"{BASE_URL}/api/v1/customer/orders/checkout", headers=customer_headers, json={
    "shippingAddress": "123 Verification Lane, Test City",
    "customerName": "P5 Test Customer",
    "customerPhone": "+84900000001",
    "customerEmail": CUSTOMER_EMAIL,
    "customerNote": "End-to-end test order"
})
if resp.status_code != 200:
    print("Checkout failed:", resp.text)
    sys.exit(1)

orders = resp.json()
print(f"\n[CHECKOUT] Created {len(orders)} order(s) from cart:")
for o in orders:
    print(f"  - Order #{o['orderNumber']} | Shop: {o['shopName']} | Amount: {o['totalAmount']} | Status: {o['status']} | Payment: {o['paymentStatus']}")

assert len(orders) >= 1, "Expected at least 1 order created"

order_to_pay = orders[0]
order_id = order_to_pay['id']

# 6. Verify: Cart is cleared after checkout
resp = requests.get(f"{BASE_URL}/api/v1/customer/cart", headers=customer_headers)
cart_after = resp.json()
print(f"\n[VERIFY] Cart after checkout: {cart_after['totalItems']} items (should be 0)")
assert cart_after['totalItems'] == 0, "Cart should be empty after checkout"

# 7. Customer Dashboard: View My Orders
resp = requests.get(f"{BASE_URL}/api/v1/customer/orders", headers=customer_headers)
my_orders = resp.json()
created_ids = {str(o['id']) for o in orders}
found = [o for o in my_orders if str(o['id']) in created_ids]
assert len(found) == len(orders), f"Expected {len(orders)} orders in customer dashboard, got {len(found)}"
print(f"[CUSTOMER DASHBOARD] Found all {len(found)} order(s) in customer dashboard ✓")

# 8. Submit Payment Confirmation (Cloudinary upload)
print(f"\n[STEP 4] Submitting payment confirmation for Order #{order_to_pay['orderNumber']}...")

# Generate a minimal valid 1x1 white PNG using Python's zlib + struct
import struct, zlib

def make_png_1x1():
    def chunk(ctype, data):
        c = ctype + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)
    
    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = chunk(b'IHDR', struct.pack('>IIBBBBB', 1, 1, 8, 2, 0, 0, 0))
    raw_pixel = b'\x00\xff\xff\xff'   # filter byte + R,G,B white
    idat = chunk(b'IDAT', zlib.compress(raw_pixel))
    iend = chunk(b'IEND', b'')
    return sig + ihdr + idat + iend

dummy_path = "test_receipt.png"
with open(dummy_path, 'wb') as f:
    f.write(make_png_1x1())

with open(dummy_path, 'rb') as img_file:
    resp = requests.post(
        f"{BASE_URL}/api/v1/customer/orders/{order_id}/payment-confirmation",
        headers=customer_headers,
        files={"receiptImage": ("test_receipt.png", img_file, "image/png")},
        data={
            "payerName": "P5 Test Customer",
            "transferAmount": str(order_to_pay['totalAmount']),
            "transferTime": "2026-03-13T12:00:00"
        }
    )

os.remove(dummy_path)

if resp.status_code != 200:
    print("Failed to submit payment confirmation:", resp.text)
    sys.exit(1)

updated_order = resp.json()
print(f"Payment submitted! Payment Status: {updated_order['paymentStatus']} (expected: WAITING_CONFIRMATION)")
print(f"Receipt URL: {updated_order['receiptImageUrl']}")
assert updated_order['paymentStatus'] == 'WAITING_CONFIRMATION', "Expected payment to be WAITING_CONFIRMATION"
assert updated_order['receiptImageUrl'] and len(updated_order['receiptImageUrl']) > 0, "Expected receipt URL to be set"
print("[OK] Cloudinary upload and URL stored confirmed.")

# 9. Seller: List Orders
print("\n[STEP 5] Seller fetching shop orders...")
resp = requests.get(f"{BASE_URL}/api/v1/seller/orders", headers=seller_headers)
if resp.status_code != 200:
    print("Failed to list seller orders:", resp.text)
    sys.exit(1)

seller_orders = resp.json()
target = next((o for o in seller_orders if str(o['id']) == str(order_id)), None)
if not target:
    print(f"WARNING: Seller could not find the order {order_id}. The products probably belong to a different shop's seller.")
    print("This is expected if the product belongs to 'seller_p4@example.com'.")
    print("Switching to that seller's account...")
    SELLER_EMAIL = "seller_p4@example.com"
    seller_token = login(SELLER_EMAIL, SELLER_PASS)
    seller_headers = {"Authorization": f"Bearer {seller_token}"}
    resp = requests.get(f"{BASE_URL}/api/v1/seller/orders", headers=seller_headers)
    seller_orders = resp.json() if resp.status_code == 200 else []
    target = next((o for o in seller_orders if str(o['id']) == str(order_id)), None)

if not target:
    print(f"ERROR: Order {order_id} not found in any tested seller account. Dumping all seller order IDs:")
    for o in seller_orders:
        print(f"  - {o['id']}")
    print("The order belongs to a different shop/seller. Test cannot fully verify seller approval.")
    sys.exit(1)

print(f"✓ Seller found Order #{target['orderNumber']} | Payment Status: {target['paymentStatus']}")

# 10. Seller Approves Payment
print("\n[STEP 6] Seller approving payment...")
resp = requests.post(f"{BASE_URL}/api/v1/seller/orders/{order_id}/payment/approve", headers=seller_headers)
if resp.status_code != 200:
    print("Failed to approve payment:", resp.text)
    sys.exit(1)
approved = resp.json()
print(f"✓ Payment approved! New Payment Status: {approved['paymentStatus']} (expected: APPROVED)")
assert approved['paymentStatus'] == 'APPROVED', "Expected payment to be APPROVED"

# 11. Seller Progresses Fulfillment
def update_fulfillment(status):
    print(f"\n[STEP 7] Seller updating order status to {status}...")
    resp = requests.put(
        f"{BASE_URL}/api/v1/seller/orders/{order_id}/status",
        headers=seller_headers,
        params={"newStatus": status}
    )
    if resp.status_code != 200:
        print(f"Failed to set status to {status}:", resp.text)
        sys.exit(1)
    result = resp.json()
    print(f"✓ Order status is now: {result['status']}")
    return result

update_fulfillment("ASSEMBLING")
update_fulfillment("SHIPPING")
delivered_order = update_fulfillment("DELIVERED")
assert delivered_order['status'] == 'DELIVERED', "Expected final status to be DELIVERED"

# 12. Final Customer Dashboard check
resp = requests.get(f"{BASE_URL}/api/v1/customer/orders/{order_id}", headers=customer_headers)
final_order = resp.json()
print(f"\n[CUSTOMER DASHBOARD] Final Order Detail:")
print(f"  Status:         {final_order['status']} (expected: DELIVERED)")
print(f"  Payment Status: {final_order['paymentStatus']} (expected: APPROVED)")
print(f"  Total:          {final_order['totalAmount']}")
print(f"  Receipt URL:    {final_order['receiptImageUrl']}")
assert final_order['status'] == 'DELIVERED', "Final order status should be DELIVERED"
assert final_order['paymentStatus'] == 'APPROVED', "Final payment status should be APPROVED"

print("\n=========================================")
print("PHASE 5 END-TO-END VERIFICATION PASSED! ")
print("=========================================")
