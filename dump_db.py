import subprocess
import sqlite3
import os

adb_path = r"C:\Users\Yunxi\AppData\Local\Android\Sdk\platform-tools\adb.exe"
files = ["pomodoro_db", "pomodoro_db-wal", "pomodoro_db-shm"]
for f in files:
    if os.path.exists(f):
        os.remove(f)
    with open(f, "wb") as out_file:
        subprocess.run([adb_path, "exec-out", f"run-as com.pomodoroalert cat /data/data/com.pomodoroalert/databases/{f}"], stdout=out_file)

if os.path.exists("pomodoro_db") and os.path.getsize("pomodoro_db") > 0:
    conn = sqlite3.connect("pomodoro_db")
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM alarms")
        colnames = [desc[0] for desc in cursor.description]
        rows = cursor.fetchall()
        with open("db_dump_utf8.txt", "w", encoding="utf-8") as f:
            for row in rows:
                d = dict(zip(colnames, row))
                f.write(str(d) + "\n")
        print("Success! Written to db_dump_utf8.txt")
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()
    
    for f in files:
        if os.path.exists(f):
            os.remove(f)
else:
    print("Failed to pull database")
