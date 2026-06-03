import subprocess

adb_path = r"C:\Users\Yunxi\AppData\Local\Android\Sdk\platform-tools\adb.exe"

def run_adb(args):
    cmd = [adb_path] + args
    print(f"Running: {' '.join(cmd)}")
    res = subprocess.run(cmd, capture_output=True, text=True)
    print("STDOUT:")
    print(res.stdout)
    print("STDERR:")
    print(res.stderr)
    print(f"Exit code: {res.returncode}")
    return res.stdout, res.stderr

# Test running a simple getprop command and package start
run_adb(["shell", "getprop", "ro.product.model"])
