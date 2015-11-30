adb shell
run-as com.course.localization.exactumpositioner
chmod 777 databases
chmod 777 databases/test.db
exit
cp /data/data/com.course.localization.exactumpositioner/databases/test.db /sdcard
exit
adb pull /sdcard/test.db