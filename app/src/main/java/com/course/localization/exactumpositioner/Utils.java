package com.course.localization.exactumpositioner;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Pete on 26.11.2015.
 */
public class Utils {
    public static final String TAG = Utils.class.getSimpleName();

    //Doesn't work for some reason??? Service does not get started

    public static void saveAll(List<WifiFingerPrint> prints, Context context, String noPrintsMessage){
        Log.d(TAG, "utils static method saving  prints...");
        if(prints != null){
            Intent intent = new Intent(context, DbService.class);
            intent.putExtra(CommonConstants.FINGERPRINT_KEY, (Serializable) prints);
            intent.setAction(DbService.ACTION_SAVE_ALL);
            context.startService(intent);
        }else{
            Toast.makeText(context, noPrintsMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
