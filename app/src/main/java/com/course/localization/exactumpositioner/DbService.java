package com.course.localization.exactumpositioner;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.course.localization.exactumpositioner.activity.ScanResults;
import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DbService extends IntentService {
    public static final String TAG = DbService.class.getSimpleName();

    public static final String ACTION_SAVE_ALL = "com.course.localization.exactumpositioner.action.SAVE";
    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_FAILURE = "failure";
    private static final String ACTION_BAZ = "com.course.localization.exactumpositioner.action.BAZ";

    public DbService() {
        super("DbService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "dbservice got the intent");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SAVE_ALL.equals(action)) {
                List prints = (ArrayList<WifiFingerPrint>) intent.getSerializableExtra(CommonConstants.FINGERPRINT_KEY);
                savePrints(prints);
            } /*else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }*/
        }
    }

    private void savePrints(List<WifiFingerPrint> prints){
        Log.d(TAG, "db service instance method saving  prints...");
        showToast("saving " + prints.size() + " fingerprints...", Toast.LENGTH_SHORT);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CommonConstants.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        if(prints != null && !prints.isEmpty()){
            for( WifiFingerPrint print : prints ){
                print.save();
            }
            broadcastIntent.putExtra(CommonConstants.SERVICE_RESPONSE_KEY, RESPONSE_SUCCESS);
        }else{
            broadcastIntent.putExtra(CommonConstants.SERVICE_RESPONSE_KEY, "nothing to save..");
            Log.d(TAG, "nothing to save...");
        }
        sendBroadcast(broadcastIntent);
    }

    //TODO make this async
    public static List<WifiFingerPrint> findPrintsGrouppedByLocation(int floorNumber){
        return WifiFingerPrint.find(
                WifiFingerPrint.class,                  //type
                "z = ?",                             //where clause
                new String[] {String.valueOf(floorNumber)},         //where args
                "x, y, z",                           //group by
                "x, y, z",                            //order by
                null                                 //limit
        );
    }

    //TODO make this async
    public static List<WifiFingerPrint> findAllPrintsOrderedByCoords(){
        return WifiFingerPrint.find(
                WifiFingerPrint.class,                  //type
                null,                             //where clause
                null,                              //where args
                null,                           //group by
                "x, y, z",                            //order by
                null                                 //limit
        );
    }



    //This should probably be part of the algorithm that resolves the fingerprint to  a location
    private List<WifiFingerPrint> averagePrints(List<WifiFingerPrint> printList){
        Log.d(TAG, "averaging the prints");
        //The keys are the mac addresses excluding the last two characters since it was said
        //in the lecture that it might not always stay the same
        Map<String, List<WifiFingerPrint>> printMap = new HashMap<>();
        List<WifiFingerPrint> retPrintList = new ArrayList<>();

        //Go through the list and put the prints into the map
        for(WifiFingerPrint print: printList){
            String mac = print.getMac();
            String shortenedMac = StringUtils.substringBeforeLast(mac, ":");
            Log.d(TAG, "network name: " + print.getNetworkName());
            Log.d(TAG, "mac: " + mac);
            Log.d(TAG, "shortenedMac: " + shortenedMac);
            List<WifiFingerPrint> list = printMap.get(shortenedMac);
            if(list == null){
                list = new ArrayList<>();
            }
            list.add(print);
        }

        for(String key: printMap.keySet()){
            int sum = 0;
            List<WifiFingerPrint> list = printMap.get(key);
            for(int i = 0; i < list.size(); i++){
                sum += list.get(i).getRssi();
            }
            int average = sum/list.size();
            WifiFingerPrint print = list.get(0);
            retPrintList.add(new WifiFingerPrint(
                    print.getX(),
                    print.getY(),
                    print.getZ(),
                    average,
                    print.getMac(),
                    print.getNetworkName(),
                    print.getTimeStamp()));
        }

        return retPrintList;
    }

    private void showToast(final String message, final int length){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, length).show();
            }
        });
    }
}
