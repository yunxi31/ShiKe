import subprocess

def main():
    adb_path = r"C:\Users\Yunxi\AppData\Local\Android\Sdk\platform-tools\adb.exe"
    cmd = [adb_path, "exec-out", "run-as", "com.pomodoroalert", "tar", "c", "-C", "/data/data/com.pomodoroalert/databases", "."]
    print("Running command:", " ".join(cmd))
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if res.returncode != 0:
        print("Error:", res.stderr.decode('utf-8', errors='ignore'))
        return
    
    with open("pomodoro_db.tar", "wb") as f:
        f.write(res.stdout)
    print("tar written successfully, size:", len(res.stdout))

    import tarfile
    try:
        with tarfile.open("pomodoro_db.tar") as tar:
            tar.extractall("db_extracted")
        print("Extraction complete.")
    except Exception as e:
        print("Extraction failed:", e)

if __name__ == '__main__':
    main()
