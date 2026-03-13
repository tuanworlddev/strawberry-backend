import psycopg2
import sys

try:
    conn = psycopg2.connect(
        dbname="strawberry_db",
        user="strawberry_user",
        password="strawberry_password",
        host="localhost"
    )
    cur = conn.cursor()
    cur.execute("SELECT email, role FROM users")
    rows = cur.fetchall()
    print("Database Users:")
    for row in rows:
        print(f"Email: {row[0]}, Role: {row[1]}")
    cur.close()
    conn.close()
except Exception as e:
    print(f"Error: {e}")
