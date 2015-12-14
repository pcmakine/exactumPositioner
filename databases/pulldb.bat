adb shell "run-as com.course.localization.exactumpositioner chmod 777 databases && chmod 777 databases/test.db"
adb shell "cp /data/data/com.course.localization.exactumpositioner/databases/test.db /sdcard"
adb pull /sdcard/test.db