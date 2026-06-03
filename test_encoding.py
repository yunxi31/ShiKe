import subprocess
import time
import os
import xml.etree.ElementTree as ET

adb_path = r"C:\Users\Yunxi\AppData\Local\Android\Sdk\platform-tools\adb.exe"

def run_adb_shell_utf8(command):
    # Pass command via stdin to avoid Windows command-line argument encoding issues
    proc = subprocess.Popen(
        [adb_path, "shell"],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding='utf-8'
    )
    stdout, stderr = proc.communicate(input=command + "\nexit\n")
    return stdout, stderr, proc.returncode

def main():
    # Trigger first alarm
    remark = "40分钟    起床洗漱、打八部金刚功    唤醒身体阳气，疏通气血，为一天积蓄精力。"
    voice_text = "主人，该40分钟    起床洗漱、打八部金刚功    唤醒身体阳气，疏通气血，为一天积蓄精力。了，今天又是崭新的一天"
    
    cmd_service = (
        f"am start-foreground-service -n com.pomodoroalert/com.pomodoroalert.service.AlarmService "
        f"--es alarmId 64593e57-1626-4dce-890d-b1bea37cdd44 "
        f"--es alarmRemark '{remark}' "
        f"--es alarmType SCHEDULE "
        f"--es voiceMode TTS "
        f"--es voiceText '{voice_text}' "
        f"--ez lockScreenEnabled true"
    )
    
    cmd_activity = (
        f"am start -n com.pomodoroalert/com.pomodoroalert.ui.AlarmWakeUpActivity "
        f"--es alarmId 64593e57-1626-4dce-890d-b1bea37cdd44 "
        f"--es alarmRemark '{remark}' "
        f"--es alarmType SCHEDULE "
        f"--ez isIndependentAlarm true"
    )
    
    print("Starting service...")
    run_adb_shell_utf8(cmd_service)
    
    print("Starting activity...")
    run_adb_shell_utf8(cmd_activity)
    
    print("Waiting for UI...")
    time.sleep(3)
    
    print("Dumping UI hierarchy...")
    run_adb_shell_utf8("uiautomator dump /sdcard/window_dump.xml")
    
    # Pull layout XML
    subprocess.run([adb_path, "pull", "/sdcard/window_dump.xml", "window_dump_test.xml"])
    
    # Stop alarm
    cmd_stop = "am start-foreground-service -a com.pomodoroalert.service.ACTION_STOP_ALARM -n com.pomodoroalert/com.pomodoroalert.service.AlarmService"
    run_adb_shell_utf8(cmd_stop)
    run_adb_shell_utf8("am force-stop com.pomodoroalert")
    
    # Parse xml to see if the remark is in it
    if os.path.exists("window_dump_test.xml"):
        with open("window_dump_test.xml", "r", encoding="utf-8") as f:
            xml_content = f.read()
        print("XML length:", len(xml_content))
        # Look for the remark
        if "起床洗漱" in xml_content:
            print("SUCCESS! UI contains the correct Chinese remark!")
        else:
            print("FAILURE! Chinese remark not found in UI dump.")
    else:
        print("XML dump file not found.")

if __name__ == "__main__":
    main()
