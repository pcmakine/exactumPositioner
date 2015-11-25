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

import java.util.ArrayList;
import java.util.List;

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
    public static final String RESPONSE_KEY = "response";
    private static final String ACTION_BAZ = "com.course.localization.exactumpositioner.action.BAZ";

    public DbService() {
        super("DbService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
        showToast("saving...", Toast.LENGTH_SHORT);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ScanResults.ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        if(prints != null && !prints.isEmpty()){
            for( WifiFingerPrint print : prints ){
                print.save();
            }
            broadcastIntent.putExtra(RESPONSE_KEY, RESPONSE_SUCCESS);
        }else{
            broadcastIntent.putExtra(RESPONSE_KEY, "nothing to save..");
            Log.d(TAG, "nothing to save...");
        }
        sendBroadcast(broadcastIntent);
    }

    private void showToast(final String message, final int length){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, length).show();
            }
        });
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
