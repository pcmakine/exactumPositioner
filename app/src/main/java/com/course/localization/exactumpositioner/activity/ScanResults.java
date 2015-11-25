package com.course.localization.exactumpositioner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.course.localization.exactumpositioner.CommonConstants;
import com.course.localization.exactumpositioner.DbService;
import com.course.localization.exactumpositioner.R;
import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class ScanResults extends AppCompatActivity {
    public static final String TAG = ScanResults.class.getSimpleName();

    private List<WifiFingerPrint> prints;
    private Snackbar bar;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_finger_prints);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        prints = (ArrayList<WifiFingerPrint>) getIntent().getSerializableExtra(CommonConstants.FINGERPRINT_KEY);
        List<String> listForListView = new ArrayList<>();
        for(WifiFingerPrint print: prints){
            listForListView.add(print.toString());
        }
        ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listForListView);
        ((ListView) findViewById(R.id.fingerprintList)).setAdapter(listAdapter);
        if( getIntent().getBooleanExtra(CommonConstants.NEW_RECORDS, false) ){
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bar = Snackbar.make(view, "Would you like to save the fingerprints?", Snackbar.LENGTH_LONG)
                            .setAction("Save", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    bar.dismiss();
                                    savePrints();
                                }
                            });
                    bar.setActionTextColor(ContextCompat.getColor(ScanResults.this, android.R.color.holo_green_light));
                    bar.show();
                }
            });
            IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new ResponseReceiver();
            registerReceiver(receiver, filter);
        }else{
            fab.hide();
            setTitle("List all");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void savePrints(){
        if(prints != null){
            Intent intent = new Intent(this, DbService.class);
            intent.putExtra(CommonConstants.FINGERPRINT_KEY, (Serializable) prints);
            intent.setAction(DbService.ACTION_SAVE_ALL);
            startService(intent);
            prints = null;
        }else{
            Toast.makeText(this, "it seems that saving prints is in progress", Toast.LENGTH_SHORT).show();
        }


      /*  if(prints != null){
            for( WifiFingerPrint print : prints ){
                print.save();
            }
            Toast.makeText(this, "Prints saved", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }else{
            Log.d(TAG, "nothing to save...");
        }*/
    }


    private void removeReceiver(){
        try{
            unregisterReceiver(receiver);
        }catch (Exception e){
            Log.e(TAG, "tried to unregister receiver again");
        }
    }

    @Override
    protected void onPause() {
        removeReceiver();
        super.onPause();
    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.mamlambo.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra(DbService.RESPONSE_KEY);
            if(response.equals(DbService.RESPONSE_SUCCESS)){
                Toast.makeText(ScanResults.this, "Prints saved...", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(ScanResults.this, response, Toast.LENGTH_SHORT).show();
            }
            onBackPressed();
        }
    }
}
