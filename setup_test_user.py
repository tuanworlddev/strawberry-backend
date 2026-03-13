import urllib.request
import json

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

# Register
print("Registering seller...")
reg_data = {
    "email": "seller@example.com",
    "password": "password123",
    "fullName": "Test Seller",
    "phone": "123456789"
}
res = make_request("http://localhost:8080/api/v1/auth/register", 'POST', reg_data)
print(f"Registration: {res}")

# Login as Admin to approve
print("Logging in as admin...")
admin_login = {"email": "admin@strawberry.com", "password": "admin-password"}
adm_res = make_request("http://localhost:8080/api/v1/auth/login", 'POST', admin_login)
if adm_res:
    adm_token = adm_res['token']
    adm_headers = {'Authorization': f'Bearer {adm_token}'}
    
    # Get pending sellers
    pending = make_request("http://localhost:8080/api/v1/admin/sellers/pending", 'GET', headers=adm_headers)
    if pending:
        for s in pending:
            if s['user']['email'] == "seller@example.com":
                print(f"Approving seller {s['id']}...")
                make_request(f"http://localhost:8080/api/v1/admin/sellers/{s['id']}/approve", 'POST', headers=adm_headers)
                break
else:
    print("Admin login failed. Assuming admin already exists or check seed.")

# Try login again
print("Logging in as seller...")
seller_res = make_request("http://localhost:8080/api/v1/auth/login", 'POST', {"email": "seller@example.com", "password": "password123"})
if seller_res:
    print("Login successful!")
else:
    print("Login still failed.")
