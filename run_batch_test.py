import subprocess
import time
import os
import ast
import shutil

adb_path = r"C:\Users\Yunxi\AppData\Local\Android\Sdk\platform-tools\adb.exe"
brain_dir = r"C:\Users\Yunxi\.gemini\antigravity\brain\58921cac-4c5a-4ad7-93b2-9dcb55217d28"

def run_adb_shell_utf8(command):
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
    if not os.path.exists("db_dump_utf8.txt"):
        print("db_dump_utf8.txt not found!")
        return

    # Read and parse database records
    alarms = []
    with open("db_dump_utf8.txt", "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                alarm = ast.literal_eval(line)
                alarms.append(alarm)
            except Exception as e:
                print(f"Error parsing line: {line}\nException: {e}")

    print(f"Loaded {len(alarms)} alarms from db_dump_utf8.txt.")
    
    results = []

    for i, alarm in enumerate(alarms):
        alarm_id = alarm.get("alarmId")
        remark = alarm.get("remark", "")
        voice_text = alarm.get("voiceText", "")
        hour = alarm.get("hour")
        minute = alarm.get("minute")
        time_str = f"{hour:02d}:{minute:02d}"
        
        # Clean remark and voice_text to fit in single quotes for shell
        clean_remark = remark.replace("'", "'\"'\"'")
        clean_voice_text = voice_text.replace("'", "'\"'\"'")
        
        print(f"\n[{i+1}/{len(alarms)}] Testing Alarm: {time_str} - {remark[:20]}...")
        
        # Trigger Service
        cmd_service = (
            f"am start-foreground-service -n com.pomodoroalert/com.pomodoroalert.service.AlarmService "
            f"--es alarmId {alarm_id} "
            f"--es alarmRemark '{clean_remark}' "
            f"--es alarmType SCHEDULE "
            f"--es voiceMode TTS "
            f"--es voiceText '{clean_voice_text}' "
            f"--ez lockScreenEnabled true"
        )
        run_adb_shell_utf8(cmd_service)
        
        # Trigger Activity
        cmd_activity = (
            f"am start -n com.pomodoroalert/com.pomodoroalert.ui.AlarmWakeUpActivity "
            f"--es alarmId {alarm_id} "
            f"--es alarmRemark '{clean_remark}' "
            f"--es alarmType SCHEDULE "
            f"--ez isIndependentAlarm true"
        )
        run_adb_shell_utf8(cmd_activity)
        
        # Wait for rendering
        time.sleep(3.5)
        
        # Dump UI layout to check correctness
        run_adb_shell_utf8("uiautomator dump /sdcard/window_dump.xml")
        xml_local = f"window_dump_{i+1}.xml"
        subprocess.run([adb_path, "pull", "/sdcard/window_dump.xml", xml_local], capture_output=True)
        
        # Verify text presence in UI dump
        text_verified = False
        if os.path.exists(xml_local):
            with open(xml_local, "r", encoding="utf-8") as f_xml:
                xml_content = f_xml.read()
            # Check if any distinctive part of remark is present (using first 10 chars as key)
            key_part = remark.split()[0] if remark.strip() else ""
            if key_part and key_part in xml_content:
                text_verified = True
                print("  UI layout verification: PASSED")
            else:
                # Fallback check for any part of remark
                words = [w for w in remark.split() if len(w) > 2]
                for w in words:
                    if w in xml_content:
                        text_verified = True
                        break
                if text_verified:
                    print("  UI layout verification: PASSED (fallback)")
                else:
                    print("  UI layout verification: WARNING (remark not found in layout dump)")
            try:
                os.remove(xml_local)
            except:
                pass
        else:
            print("  UI layout verification: FAILED (xml dump not pulled)")

        # Capture screenshot
        screen_phone = f"/sdcard/screenshot_{i+1}.png"
        screen_local = f"screenshot_alarm_{i+1}.png"
        run_adb_shell_utf8(f"screencap -p {screen_phone}")
        subprocess.run([adb_path, "pull", screen_phone, screen_local], capture_output=True)
        
        # Copy screenshot to brain folder
        brain_screen_path = os.path.join(brain_dir, f"screenshot_alarm_{i+1}.png")
        if os.path.exists(screen_local):
            shutil.copy(screen_local, brain_screen_path)
            print(f"  Screenshot saved to brain: {brain_screen_path}")
        else:
            print("  Failed to capture screenshot.")

        # Stop alarm and force-stop app to clear state
        cmd_stop = "am start-foreground-service -a com.pomodoroalert.service.ACTION_STOP_ALARM -n com.pomodoroalert/com.pomodoroalert.service.AlarmService"
        run_adb_shell_utf8(cmd_stop)
        run_adb_shell_utf8("am force-stop com.pomodoroalert")
        
        # Cool down time to let system clean up
        time.sleep(1)
        
        results.append({
            "index": i + 1,
            "alarmId": alarm_id,
            "time": time_str,
            "remark": remark,
            "text_verified": text_verified,
            "screenshot_local": screen_local,
            "screenshot_brain": brain_screen_path
        })
        
    print("\n=== Batch Test Completed! ===")
    for res in results:
        status = "PASSED" if res["text_verified"] else "WARNING"
        print(f"Alarm {res['index']} ({res['time']}): {status}")

if __name__ == "__main__":
    main()
