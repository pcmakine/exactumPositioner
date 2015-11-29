package com.course.localization.exactumpositioner;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.io.Serializable;
import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class DataExportService extends IntentService {
    private static final String TAG = IntentService.class.getSimpleName();
    public static final String ACTION_EXPORT_DATA = "com.course.localization.exactumpositioner.action.EXPORT_FINGERPRINTS";
    public static final String PARAM_FILENAME = "com.course.localization.exactumpositioner.param.FILENAME";
    public static final String RESPONSE_SUCCESS = "successDataExport";
    public static final String RESPONSE_FAILURE = "failureDataExport";
    public static final String ERROR_MESSAGE_KEY = "error";

    public DataExportService() {
        super("DataExportService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_EXPORT_DATA.equals(action)) {
                long startTime = System.currentTimeMillis();

                final String fileName = intent.getStringExtra(PARAM_FILENAME);
                //List<WifiFingerPrint> prints =  WifiFingerPrint.listAll(WifiFingerPrint.class);
                List<WifiFingerPrint> prints =
                        WifiFingerPrint.find(
                        WifiFingerPrint.class,                  //type
                        null,                             //where clause
                        null,                              //where args
                        null,                           //group by
                        "x, y, z",                            //order by
                        null                                 //limit
                );
                Log.d(TAG, "fetched all the fingerprints");
                String result = DataExporter.writePrintsToFile(
                        prints,
                        getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                        fileName
                );
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(CommonConstants.ACTION_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                if( result.isEmpty() ){
                    broadcastIntent.putExtra(CommonConstants.SERVICE_RESPONSE_KEY, RESPONSE_SUCCESS);
                }else{
                    broadcastIntent.putExtra(CommonConstants.SERVICE_RESPONSE_KEY, RESPONSE_FAILURE);
                    broadcastIntent.putExtra(ERROR_MESSAGE_KEY, result);
                }
                sendBroadcast(broadcastIntent);
                Long endTime = System.currentTimeMillis();
                Log.d(TAG, "Export took " + (endTime - startTime)/1000 + " seconds, " + prints.size() + " records");
            }
        }
    }

    public static void exportAllData(String fileName, Context context){
        if( fileName != null){
            Toast.makeText(context, "Started export", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, DataExportService.class);
            intent.putExtra(DataExportService.PARAM_FILENAME, fileName);
            intent.setAction(DataExportService.ACTION_EXPORT_DATA);
            context.startService(intent);
        }else{
            Toast.makeText(context, "You must specify a file name", Toast.LENGTH_SHORT).show();
        }
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
