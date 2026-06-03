import subprocess
import time

adb_path = r"C:\Users\Yunxi\AppData\Local\Android\Sdk\platform-tools\adb.exe"

def run_adb(args):
    cmd = [adb_path] + args
    res = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore')
    return res.stdout, res.stderr

def main():
    # Clear logcat first
    print("Clearing logcat...")
    run_adb(["logcat", "-c"])
    
    # Start AlarmService with TTS using UTF-8 input redirection to prevent Windows command line garbling
    print("Starting AlarmService with TTS...")
    cmd = (
        'am start-foreground-service '
        '-n com.pomodoroalert/com.pomodoroalert.service.AlarmService '
        '--es alarmId 64593e57-1626-4dce-890d-b1bea37cdd44 '
        '--es alarmRemark "测试作息提醒TTS" '
        '--es alarmType SCHEDULE '
        '--es voiceMode TTS '
        '--es voiceText "主人，测试作息提醒语音播报，听到说明正常。" '
        '--ez lockScreenEnabled true'
    )
    
    proc = subprocess.Popen(
        [adb_path, "shell"],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding='utf-8'
    )
    stdout, stderr = proc.communicate(input=cmd + "\nexit\n")
    print("Start status:", stdout.strip(), stderr.strip())
    
    # Wait for 10 seconds
    print("Waiting 10 seconds for TTS initialization and playback...")
    time.sleep(10)
    
    # Fetch logcat
    print("Fetching logcat...")
    stdout, _ = run_adb(["logcat", "-d"])
    
    print("\n=== Logcat Output ===")
    lines = stdout.splitlines()
    for line in lines:
        if any(tag in line for tag in ["AlarmService", "TextToSpeech", "TTS", "tts", "speak", "speech"]):
            print(line)
            
    # Stop AlarmService
    print("\nStopping AlarmService...")
    stop_args = [
        "shell", "am", "start-foreground-service",
        "-a", "com.pomodoroalert.service.ACTION_STOP_ALARM",
        "-n", "com.pomodoroalert/com.pomodoroalert.service.AlarmService"
    ]
    run_adb(stop_args)

if __name__ == '__main__':
    main()
