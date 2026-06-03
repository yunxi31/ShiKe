import subprocess
import time
import os

adb_path = r"C:\Users\Yunxi\AppData\Local\Android\Sdk\platform-tools\adb.exe"

def run_adb(args):
    cmd = [adb_path] + args
    print(f"Running: {' '.join(cmd)}")
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res.stdout, res.stderr, res.returncode

def main():
    # 1. 启动闹钟前台服务 (播放铃声/TTS)
    print("=== Step 1: Triggering AlarmService ===")
    service_args = [
        "shell", "am", "start-foreground-service",
        "-n", "com.pomodoroalert/com.pomodoroalert.service.AlarmService",
        "--es", "alarmId", "64593e57-1626-4dce-890d-b1bea37cdd44",
        "--es", "alarmRemark", "起床洗漱、打八部金刚功",
        "--es", "alarmType", "SCHEDULE",
        "--es", "voiceMode", "TTS",
        "--es", "voiceText", "主人，该起床洗漱、打八部金刚功了，今天又是崭新的一天",
        "--ez", "lockScreenEnabled", "true"
    ]
    stdout, stderr, code = run_adb(service_args)
    print("Service Stdout:", stdout.strip())
    print("Service Stderr:", stderr.strip())

    # 2. 启动 AlarmWakeUpActivity (直接在前台展示 UI，规避 BAL 限制)
    print("\n=== Step 2: Launching AlarmWakeUpActivity directly ===")
    activity_args = [
        "shell", "am", "start",
        "-n", "com.pomodoroalert/com.pomodoroalert.ui.AlarmWakeUpActivity",
        "--es", "alarmId", "64593e57-1626-4dce-890d-b1bea37cdd44",
        "--es", "alarmRemark", "起床洗漱、打八部金刚功",
        "--es", "alarmType", "SCHEDULE",
        "--ez", "isIndependentAlarm", "true"
    ]
    stdout, stderr, code = run_adb(activity_args)
    print("Activity Stdout:", stdout.strip())
    print("Activity Stderr:", stderr.strip())
    
    # 3. 等待 4 秒让界面完全渲染并绘制出来
    print("\n=== Step 3: Waiting 4 seconds for UI rendering ===")
    time.sleep(4)
    
    # 4. 检查当前处于顶部的 Activity
    print("\n=== Step 4: Checking current active Activity ===")
    stdout, stderr, code = run_adb(["shell", "dumpsys", "window", "displays"])
    activity_lines = [line.strip() for line in stdout.splitlines() if "mCurrentFocus" in line or "mFocusedApp" in line or "mResumedActivity" in line]
    for line in activity_lines:
        print("Active:", line)
        
    # 5. 截屏并保存到本地以作视觉核对
    print("\n=== Step 5: Taking screenshot ===")
    run_adb(["shell", "screencap", "-p", "/sdcard/test_result_screen.png"])
    run_adb(["pull", "/sdcard/test_result_screen.png", "test_result_screen.png"])
    
    if os.path.exists("test_result_screen.png"):
        print("Screenshot successfully captured and saved as test_result_screen.png")
    else:
        print("Failed to save screenshot locally.")

    # 6. 关闭服务以停止播放铃声和语音
    print("\n=== Step 6: Stopping alarm service ===")
    stop_args = [
        "shell", "am", "start-foreground-service",
        "-a", "com.pomodoroalert.service.ACTION_STOP_ALARM",
        "-n", "com.pomodoroalert/com.pomodoroalert.service.AlarmService"
    ]
    stdout, stderr, code = run_adb(stop_args)
    print("Stop Service Stdout:", stdout.strip())

    # 7. 关闭 activity
    print("\n=== Step 7: Closing activity ===")
    run_adb(["shell", "am", "force-stop", "com.pomodoroalert"])

if __name__ == "__main__":
    main()
