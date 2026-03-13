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
    except Exception as e:
        print(f"Error: {e}")
        return None

# Auth
login_url = "http://127.0.0.1:8080/api/v1/auth/login"
login_data = {"email": "seller@example.com", "password": "password123"}
auth_res = make_request(login_url, 'POST', login_data)
token = auth_res['token']
headers = {'Authorization': f'Bearer {token}'}

# Get shop ID
shops = make_request("http://127.0.0.1:8080/api/v1/seller/shops", 'GET', headers=headers)
shop_id = shops[0]['id']
print(f"Testing for Shop: {shop_id}")

# 1. Update Sync Settings
print("\n1. Updating Sync Settings (paused=false, interval=15)...")
settings_url = f"http://127.0.0.1:8080/api/v1/seller/shops/{shop_id}/sync/settings"
make_request(settings_url, 'PUT', {"syncIntervalMinutes": 15, "isSyncPaused": False}, headers)

# 2. Get Health Stats
print("2. Checking Sync Health Stats...")
health = make_request(f"http://127.0.0.1:8080/api/v1/seller/shops/{shop_id}/sync/stats", 'GET', headers=headers)
print(f"Health: {health}")

# 3. Get History
print("3. Checking Sync History...")
history = make_request(f"http://127.0.0.1:8080/api/v1/seller/shops/{shop_id}/sync/history", 'GET', headers=headers)
print(f"History (First Item): {history[0] if history else 'Empty'}")

# 4. Trigger Manual Sync (to see trigger_type and sync_type)
print("\n4. Triggering Manual FULL sync...")
trigger_url = f"http://127.0.0.1:8080/api/v1/seller/shops/{shop_id}/sync/full"
make_request(trigger_url, 'POST', {}, headers)
time.sleep(2) # wait for job creation

print("5. Verifying trigger_type in history...")
history = make_request(f"http://127.0.0.1:8080/api/v1/seller/shops/{shop_id}/sync/history", 'GET', headers=headers)
if history:
    print(f"Latest Job ID: {history[0]['jobId']}")
    print(f"Trigger Type: {history[0]['triggerType']}")
    print(f"Sync Type: {history[0]['syncType']}")
