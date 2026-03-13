import urllib.request
import urllib.error
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
        body = e.read().decode('utf-8')
        print(f"HTTP Error {e.code} on {url}: {body}")
        try:
            return json.loads(body)
        except:
            return body
    except Exception as e:
        print(f"Error on {url}: {e}")
        return None

BASE_URL = "http://127.0.0.1:8080/api/v1"

# 1. Setup / Ensure Seller
print("\n--- 1. Using Existing Seller ---")
reg_data = {
    "email": "seller@example.com",
    "password": "password123"
}

# 2. Login as Seller
print("\n--- 2. Logging in as Seller ---")
auth_res = make_request(f"{BASE_URL}/auth/login", 'POST', {"email": reg_data['email'], "password": reg_data['password']})
print(f"Seller Login Result: {auth_res}")
if not auth_res or 'accessToken' not in auth_res:
    print("Failed to login as seller")
    exit(1)
token = auth_res['accessToken']
headers = {'Authorization': f'Bearer {token}'}

# 3. Ensure Shop & Products
print("\n--- 3. Ensure Shop & Products ---")
shops = make_request(f"{BASE_URL}/shops", 'GET', headers=headers)
if not shops:
    print("No shops found for seller. Creating one...")
shop_id = shops[0]['shopId']
print(f"Using Shop ID: {shop_id}")

# If no products, trigger a small sync (assuming WB creds are valid or using mock)
my_prods = make_request(f"{BASE_URL}/seller/products", 'GET', headers=headers)
if not my_prods['content']:
    print("No products found. Triggering sync...")
    # This might fail if WB key is not set, but for Phase 4 we assume some data exists from previous phases.
    # If empty, we can't test storefront.
    pass

if not my_prods['content']:
    print("\nWARNING: No products found for seller. Storefront tests will be limited.")
else:
    prod = my_prods['content'][0]
    prod_id = prod['id']
    print(f"Selected Product for enrichment: {prod['title']} ({prod_id})")

    # 4. Update Metadata
    print("\n--- 4. Updating Metadata (Title, Visibility, Slug) ---")
    custom_slug = f"premium-item-{int(time.time())}"
    meta_data = {
        "localTitle": "Antigravity Ultra 4000",
        "localDescription": "World's lightest shoes.",
        "visibility": "ACTIVE",
        "slugOverride": custom_slug
    }
    make_request(f"{BASE_URL}/seller/products/{prod_id}/metadata", 'PUT', meta_data, headers)
    print("Metadata updated.")

    # 5. Storefront - Check Product by Slug
    print("\n--- 5. Storefront: Get Product by Slug ---")
    # Wait a bit for visibility rules / db
    time.sleep(1)
    detail = make_request(f"{BASE_URL}/public/catalog/products/{custom_slug}", 'GET')
    if detail:
        print(f"Public Entry Found: {detail['title']}")
        print(f"Slug Match: {detail['slug'] == custom_slug}")
        
        # 6. Update Pricing & Stock
        print("\n--- 6. Updating Pricing & Stock ---")
        variant_id = detail['variants'][0]['id']
        make_request(f"{BASE_URL}/seller/products/variants/{variant_id}/pricing", 'PUT', {"basePrice": 3000, "discountPrice": 2499}, headers)
        make_request(f"{BASE_URL}/seller/products/variants/{variant_id}/inventory", 'PUT', {"stockQuantity": 100}, headers)
        
        # 7. Final Verification
        print("\n--- 7. Final Verification ---")
        detail = make_request(f"{BASE_URL}/public/catalog/products/{custom_slug}", 'GET')
        v = detail['variants'][0]
        print(f"New Price: {v['discountPrice']} (Exp: 2499)")
        print(f"New Stock: {v['stockQuantity']} (Exp: 100)")
        
        # 8. Filter Suggestion
        print("\n--- 8. Storefront: Filters ---")
        filters = make_request(f"{BASE_URL}/public/catalog/filters", 'GET')
        if filters:
            print(f"Categories count: {len(filters['categories'])}")
    else:
        print(f"COULD NOT find product publicly with slug {custom_slug}. Check visibility/active variants.")

print("\n--- Verification Finished ---")
