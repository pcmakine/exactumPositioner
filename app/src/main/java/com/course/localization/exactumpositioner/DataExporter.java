package com.course.localization.exactumpositioner;

import android.content.Context;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Pete on 28.11.2015.
 */
public class DataExporter {
    private static final String TAG = DataExporter.class.getSimpleName();
    private final String QUERY_MAX_PER_LOCATION = "SELECT MAX(aver), X, Y, Z FROM (" +
            "SELECT x, y, z, MAC, AVG(RSSI) AS aver FROM " +
            "WIFI_FINGER_PRINT " +
            "GROUP BY X, Y, Z, MAC) " +
            "GROUP BY X, Y, Z";

    //returns an empty string if successful, otherwise an error message
    public static String writePrintsToFile(List<WifiFingerPrint> prints, File folder, String fileName){
        if( !isExternalStorageWritable() ){
            return "external storage not writable";
        }
        File file = new File(folder, fileName);
      /*  if( file.exists() ){
            file.delete();
            Log.d(TAG, "Old fingerprint file deleted...");
        }*/
        try{
            OutputStream os = new FileOutputStream(file, false);
            os.write(printsToString(prints).getBytes());
            os.close();
        }catch(Exception e){
            Log.d(TAG, "Exception on data export");
            Log.d(TAG, e.getMessage());
            return "Exception on data export";
        }
        return "";
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static String printsToString(List<WifiFingerPrint> prints){
        //List<WifiFingerPrint> prints = DbService.findAllPrintsOrderedByCoords();
        StringBuilder sb = new StringBuilder();
        if(prints != null && !prints.isEmpty()){
            List<String> prevLine = new ArrayList<>();      //this list will have the elements for the string to be returned
            Float currentZ = null;
            Float currentX = null;
            Float currentY = null;
            for(WifiFingerPrint print: prints){
                if(!isSameCoords(currentZ, print.getZ(), currentX, print.getX(), currentY, print.getY())){
                    if( !prevLine.isEmpty()){
                        addLineToStringBuilder(prevLine, sb);
                        sb.append('\n');
                    }
                    prevLine.add(String.valueOf(print.getZ()));
                    prevLine.add(String.valueOf(print.getX()));
                    prevLine.add(String.valueOf(print.getY()));
                    currentZ = print.getZ();
                    currentX = print.getX();
                    currentY = print.getY();
                }
                String mac = print.getMac();
                if( mac != null ){
                    mac = mac.trim().replaceAll(":", "");
                }
                prevLine.add(String.valueOf(Long.parseLong(mac, 16)));      //the mac address as decimal
                prevLine.add(String.valueOf(print.getRssi()));
            }
            addLineToStringBuilder(prevLine, sb);
        }
        return sb.toString();
    }

    private static boolean isSameCoords(Float z1, Float z2, Float x1, Float x2, Float y1, Float y2){
        if(z2 == null || x2 == null || y2 == null){
            throw new IllegalArgumentException("z2, x2 and y2 can't be null");
        }
        return (z1 != null && x1 != null && y1 != null) && z1.equals(z2) && x1.equals(x2) && y1.equals(y2);

    }

    private static void addLineToStringBuilder(List<String> line, StringBuilder sb){
        String str = StringUtils.join(line, ';');
        sb.append(str);
        line.clear();
    }
}
