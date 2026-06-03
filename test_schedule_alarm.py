import subprocess
import time
import os

adb_path = r"C:\Users\Yunxi\AppData\Local\Android\Sdk\platform-tools\adb.exe"

def run_adb(args):
    cmd = [adb_path] + args
    print(f"Running: {' '.join(cmd)}")
    res = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore')
    return res.stdout, res.stderr, res.returncode

def main():
    # 0. Force stop app and clear logcat
    print("Force stopping app and clearing logcat...")
    run_adb(["shell", "am", "force-stop", "com.pomodoroalert"])
    run_adb(["shell", "logcat", "-c"])

    # 1. Start AlarmService (simulating ExactAlarmReceiver behavior)
    print("\n=== Step 1: Triggering AlarmService ===")
    cmd_service = (
        'am start-foreground-service '
        '-n com.pomodoroalert/com.pomodoroalert.service.AlarmService '
        '--es alarmId 64593e57-1626-4dce-890d-b1bea37cdd44 '
        '--es alarmRemark "起床洗漱、打八部金刚功" '
        '--es alarmType SCHEDULE '
        '--es voiceMode TTS '
        '--es voiceText "主人，该起床洗漱了" '
        '--ez lockScreenEnabled true'
    )
    
    proc1 = subprocess.Popen(
        [adb_path, "shell"],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding='utf-8'
    )
    stdout, stderr = proc1.communicate(input=cmd_service + "\nexit\n")
    print("Service Stdout:", stdout.strip())
    print("Service Stderr:", stderr.strip())

    # 2. Wait 8 seconds to allow the app to launch the activity internally and render
    print("\n=== Step 2: Waiting 8 seconds for AlarmWakeUpActivity launch and UI rendering ===")
    time.sleep(8)

    # 3. Check current active Activity
    print("\n=== Step 3: Checking current active Activity ===")
    stdout, stderr, code = run_adb(["shell", "dumpsys", "window", "displays"])
    activity_lines = [line.strip() for line in stdout.splitlines() if "mCurrentFocus" in line or "mFocusedApp" in line or "mResumedActivity" in line]
    for line in activity_lines:
        print("Active:", line)

    # 4. Fetch logs related to background launch blocking or AlarmService
    print("\n=== Step 4: Fetching AlarmService and BAL_BLOCK logs ===")
    log_stdout, _, _ = run_adb(["logcat", "-d"])
    lines = log_stdout.splitlines()
    for line in lines:
        if any(tag in line for tag in ["AlarmService", "BAL_BLOCK", "BackgroundActivityStart", "ActivityTaskManager", "ActivityStart"]):
            print(line)

    # 5. Take screenshot
    print("\n=== Step 5: Taking screenshot of the screen ===")
    screenshot_name = "test_result_screen.png"
    run_adb(["shell", "screencap", "-p", "/sdcard/" + screenshot_name])
    run_adb(["pull", "/sdcard/" + screenshot_name, screenshot_name])
    if os.path.exists(screenshot_name):
        print(f"Screenshot successfully captured and saved as {screenshot_name}")
    else:
        print("Failed to save screenshot locally.")

    # 6. Clean up: close service and force stop
    print("\n=== Step 6: Cleaning up ===")
    stop_args = [
        "shell", "am", "start-foreground-service",
        "-a", "com.pomodoroalert.service.ACTION_STOP_ALARM",
        "-n", "com.pomodoroalert/com.pomodoroalert.service.AlarmService"
    ]
    run_adb(stop_args)
    run_adb(["shell", "am", "force-stop", "com.pomodoroalert"])

if __name__ == "__main__":
    main()
