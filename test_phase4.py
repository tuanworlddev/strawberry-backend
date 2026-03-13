import urllib.request
import json
import time

def make_request(url, method='GET', data=None, headers=None):
    if headers is None:
        headers = {}
    
    req = urllib.request.Request(url, method=method, headers=headers)
    if data:
        req.data = json.dumps(data).encode('utf-8')
        req.add_header('Content-Type', 'application/json')
    
    try:
        with urllib.request.urlopen(req) as response:
            return json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        print(f"HTTP Error {e.code}: {e.read().decode('utf-8')}")
        return None
    except Exception as e:
        print(f"Error: {e}")
        return None

# Auth
login_url = "http://127.0.0.1:8080/api/v1/auth/login"
login_data = {"email": "seller@example.com", "password": "password123"}
auth_res = make_request(login_url, 'POST', login_data)
if not auth_res:
    print("Login failed")
    exit(1)
token = auth_res['token']
headers = {'Authorization': f'Bearer {token}'}

# Get shop ID
shops = make_request("http://127.0.0.1:8080/api/v1/seller/shops", 'GET', headers=headers)
shop_id = shops[0]['id']
print(f"Testing for Shop: {shop_id}")

# 1. Storefront - Initial Search
print("\n1. Storefront - Initial Search...")
search_res = make_request("http://127.0.0.1:8080/api/v1/public/catalog/search", 'GET')
if search_res:
    print(f"Public Search Count: {search_res['totalElements']}")
    if search_res['content']:
        sample_prod = search_res['content'][0]
        print(f"Sample Product: {sample_prod['title']} (Slug: {sample_prod['slug']})")
    else:
        print("No products visible publicly yet.")
else:
    print("Search failed")

# 2. Seller - Update Metadata & Visibility
print("\n2. Updating Product Metadata & Visibility...")
# Get personal product list
my_prods = make_request("http://127.0.0.1:8080/api/v1/seller/products", 'GET', headers=headers)
if not my_prods['content']:
    print("No products found for seller")
    exit(1)

prod = my_prods['content'][0]
prod_id = prod['id']
print(f"Updating Product: {prod_id}")

meta_url = f"http://127.0.0.1:8080/api/v1/seller/products/{prod_id}/metadata"
meta_data = {
    "localTitle": "Antigravity Premium Product " + str(int(time.time())),
    "localDescription": "Enhanced description for storefront",
    "visibility": "VISIBLE",
    "seoSlug": "premium-product-" + str(prod_id)[:8]
}
make_request(meta_url, 'PUT', meta_data, headers)
print("Metadata updated.")

# 3. Storefront - Verify Metadata Update
print("\n3. Storefront - Verify Product Detail by Slug...")
slug = meta_data['seoSlug']
detail = make_request(f"http://127.0.0.1:8080/api/v1/public/catalog/products/{slug}", 'GET')
if detail:
    print(f"Found product by slug: {detail['title']}")
    print(f"Description matches: {detail['description'] == meta_data['localDescription']}")
else:
    print(f"Could NOT find product by slug: {slug}")

# 4. Seller - Update Pricing
print("\n4. Updating Variant Pricing...")
# Need to get a variant ID from detail
variant_id = detail['variants'][0]['id']
pricing_url = f"http://127.0.0.1:8080/api/v1/seller/products/variants/{variant_id}/pricing"
pricing_data = {
    "basePrice": 2500.0,
    "discountPrice": 1999.0
}
make_request(pricing_url, 'PUT', pricing_data, headers)
print("Pricing updated.")

# 5. Seller - Update Inventory
print("\n5. Updating Variant Inventory...")
inventory_url = f"http://127.0.0.1:8080/api/v1/seller/products/variants/{variant_id}/inventory"
inventory_data = {
    "stockQuantity": 50
}
make_request(inventory_url, 'PUT', inventory_data, headers)
print("Inventory updated.")

# 6. Storefront - Re-verify Detail
print("\n6. Storefront - Final Verification (Price & Stock)...")
detail = make_request(f"http://127.0.0.1:8080/api/v1/public/catalog/products/{slug}", 'GET')
if detail:
    v = detail['variants'][0]
    print(f"Price: {v['discountPrice']} (Expected: 1999.0)")
    print(f"Stock: {v['stockQuantity']} (Expected: 50)")

# 7. Storefront - Search Filters
print("\n7. Storefront - Filters...")
filters = make_request("http://127.0.0.1:8080/api/v1/public/catalog/filters", 'GET')
print(f"Available Categories: {len(filters['categories']) if filters else 0}")
print(f"Available Brands: {len(filters['brands']) if filters else 0}")
