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
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.course.localization.exactumpositioner.CommonConstants;
import com.course.localization.exactumpositioner.DbService;
import com.course.localization.exactumpositioner.R;
import com.course.localization.exactumpositioner.Utils;
import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class ScanResults extends AppCompatActivity {
    public static final String TAG = ScanResults.class.getSimpleName();
    private List<WifiFingerPrint> prints;
    private List<String> printsAsStr;
    private Snackbar bar;
    private BroadcastReceiver receiver;
    private ListView listView;
    private int offSet;
    private int limit;
    private boolean newRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_finger_prints);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        newRecords = getIntent().getBooleanExtra(CommonConstants.NEW_RECORDS, false);

        prints = (ArrayList<WifiFingerPrint>) getIntent().getSerializableExtra(CommonConstants.FINGERPRINT_KEY);
        printsAsStr = new ArrayList<>();
        for(WifiFingerPrint print: prints){
            printsAsStr.add(print.toString());
        }
        Log.d(TAG, "eka, " + printsAsStr.size() + " prints");
        Log.d(TAG, "current prints: " + printsAsStr.toString());
        final ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, printsAsStr);
        listView = ((ListView) findViewById(R.id.fingerprintList));
        listView.setAdapter(listAdapter);
        if( !newRecords ){
            offSet = 0;
            limit = CommonConstants.DEFAULT_LIMIT;
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (listView.getLastVisiblePosition() == listView.getAdapter().getCount() - 1
                            && listView.getChildAt(listView.getChildCount() - 1).getBottom() <= listView.getHeight()) {
                        //scroll end reached
                        Log.d(TAG, "scroll end reached");
                        offSet = offSet + limit;
                        List<WifiFingerPrint> nextPrints = WifiFingerPrint.findWithQuery(WifiFingerPrint.class, CommonConstants.QUERY_LIMIT_PRINTS, String.valueOf(limit), String.valueOf(offSet));
                        prints.addAll(nextPrints);
                        for(WifiFingerPrint print: nextPrints){
                            printsAsStr.add(print.toString());
                        }
                        listAdapter.notifyDataSetChanged();
                        setTitle(listView.getCount() + " latest prints");
                    }
                }
            });
        }


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
            IntentFilter filter = new IntentFilter(CommonConstants.ACTION_RESP);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new ResponseReceiver();
            registerReceiver(receiver, filter);
        }else{
            fab.hide();
            setTitle(listView.getCount() + " latest prints");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void savePrints(){
        Utils.saveAll(prints, this, "it seems that saving prints is in progress" );



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

        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra(CommonConstants.SERVICE_RESPONSE_KEY);
            if(response.equals(DbService.RESPONSE_SUCCESS)){
                Toast.makeText(ScanResults.this, "Prints saved...", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(ScanResults.this, response, Toast.LENGTH_SHORT).show();
            }
            onBackPressed();
        }
    }
}
