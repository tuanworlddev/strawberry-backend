import json
import urllib.request
import sys

def make_request(url, method, payload=None, headers=None):
    if headers is None: headers = {}
    headers['Content-Type'] = 'application/json'
    
    data = json.dumps(payload).encode('utf-8') if payload else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    
    try:
        with urllib.request.urlopen(req) as response:
            body = response.read().decode('utf-8')
            return json.loads(body) if body else {}
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8')
        print(f"HTTP {e.code}: {body}")
        sys.exit(1)
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

print('Logging in...')
login_res = make_request('http://127.0.0.1:8080/api/v1/auth/login', 'POST', {'email': 'seller@example.com', 'password': 'password'})
token = login_res.get('accessToken')
print(f"Token: {token}")

shop_id = '8346cd96-8403-4bba-862f-4fefaac87360'
headers = {'Authorization': f'Bearer {token}'}

print('Updating Integration...')
# Replace with your real WB API key for testing
make_request(f'http://127.0.0.1:8080/api/v1/shops/{shop_id}/integration', 'PUT', {'wbApiKey': 'YOUR_REAL_WB_API_KEY_HERE'}, headers)
print('Integration Updated!')

print('Triggering Full Sync...')
res = make_request(f'http://127.0.0.1:8080/api/v1/seller/shops/{shop_id}/sync/full', 'POST', {}, headers)
print(f"Sync Trigger Response: {res}")
