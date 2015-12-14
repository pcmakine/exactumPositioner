package com.course.localization.exactumpositioner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.course.localization.exactumpositioner.CommonConstants;
import com.course.localization.exactumpositioner.DbService;
import com.course.localization.exactumpositioner.ExpandableListAdapter;
import com.course.localization.exactumpositioner.FreshDataHolder;
import com.course.localization.exactumpositioner.R;
import com.course.localization.exactumpositioner.Title;
import com.course.localization.exactumpositioner.Utils;
import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class ScanResults extends AppCompatActivity {
    public static final String TAG = ScanResults.class.getSimpleName();
    private List<WifiFingerPrint> prints;
    private Snackbar bar;
    private BroadcastReceiver receiver;
    private ExpandableListView listView;
    private int offSet;
    private int limit;
    private boolean newRecords;
    private ArrayList<Title> titles;
    private ArrayList<ArrayList<WifiFingerPrint>> childItems;
    private ExpandableListAdapter listAdapter;
    private Button btnLoadMore;
    private AsyncTask updateDataTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_finger_prints);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        newRecords = getIntent().getBooleanExtra(CommonConstants.NEW_RECORDS, false);

        titles = new ArrayList<>();
        childItems = new ArrayList<>();
        prints = (ArrayList<WifiFingerPrint>) getIntent().getSerializableExtra(CommonConstants.FINGERPRINT_KEY);    //todo change this to user the fresh data holder class
        if( prints != null ){
            Collections.sort(prints, new Comparator<WifiFingerPrint>() {
                @Override
                public int compare(WifiFingerPrint o1, WifiFingerPrint o2) {
                    return o1.getTimeStamp().compareTo(o2.getTimeStamp());
                }
            });
            updateDataTask = new UpdateDataTask();
            updateDataTask.execute(new ArrayList[] {new ArrayList<>(prints)});
            //updateData(prints);
        }else{
            prints = new ArrayList<>();
        }
        listAdapter = new ExpandableListAdapter(titles, childItems, this);
        listView = ((ExpandableListView) findViewById(R.id.fingerprintList));
        listView.setAdapter(listAdapter);
        listView.setClickable(true);
        listView.expandGroup(0);
        findViewById(R.id.loadingLayout).bringToFront();    //make sure the view is not hidden behind the load more button
        if( !newRecords ){
            offSet = 0;
            limit = CommonConstants.DEFAULT_LIMIT;
            // Creating a button - Load More
            btnLoadMore = new Button(this);
            btnLoadMore.setText(getResources().getString(R.string.load_more));
            btnLoadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadMore(v);
                }
            });
            listView.addFooterView(btnLoadMore);
            loadMore(null);
            fab.hide();
            //setTitle((offSet + limit) + " latest prints");
        }else {
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
        }

        IntentFilter filter = new IntentFilter(CommonConstants.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
        if( getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void updateData(List<WifiFingerPrint> newPrints){
        if(titles == null){
            titles = new ArrayList<>();
        }
        if(childItems == null){
            childItems = new ArrayList<>();
        }
        Long currentTimeStamp = null;
        for(WifiFingerPrint print: newPrints){
            if( !print.getTimeStamp().equals(currentTimeStamp) ){
                //titles.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(print.getTimeStamp())));
                childItems.add(new ArrayList<WifiFingerPrint>());
                currentTimeStamp = print.getTimeStamp();
            }
            ArrayList<WifiFingerPrint> prints = childItems.get(childItems.size()-1);
            prints.add(print);
        }
    }

    private void showLoadingState(){
        if(btnLoadMore != null ){
            btnLoadMore.setEnabled(false);
        }
        findViewById(R.id.loadingLayout).setVisibility(View.VISIBLE);
    }

    private void showNormalState(){
        if( btnLoadMore != null){
            btnLoadMore.setEnabled(true);
        }
        LinearLayout layout = (LinearLayout) findViewById(R.id.loadingLayout);
        layout.setVisibility(View.INVISIBLE);
    }

    private void savePrints(){
        Utils.saveAll(prints, this, "it seems that saving prints is in progress" );
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
        if(updateDataTask != null ){
            updateDataTask.cancel(true);
        }
        removeReceiver();
        super.onPause();
    }

    public void loadMore(View view) {
        if( !prints.isEmpty() ){
            offSet = offSet + limit;
        }
        showLoadingState();
        DbService.findPrintsOrderByTimeStamp(limit, offSet, ScanResults.this);
    }

    public class ResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionPerformed = intent.getStringExtra(CommonConstants.SERVICE_ACTION_PERFORMED);
            if( actionPerformed.equals(DbService.ACTION_LOAD_ORDER_BY_TIMESTAMP) ){
                //ArrayList<WifiFingerPrint> nextPrints = (ArrayList<WifiFingerPrint>) intent.getSerializableExtra(CommonConstants.FINGERPRINT_KEY);
                List<WifiFingerPrint> nextPrints = FreshDataHolder.getInstance().getLatestFetchedPrints();
                prints.addAll(nextPrints);
                updateDataTask = new UpdateDataTask();
                List<WifiFingerPrint>[] arr = new ArrayList[1];
                arr[0] = nextPrints;
                updateDataTask.execute(arr);
            }else{
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

    private class UpdateDataTask extends AsyncTask<List<WifiFingerPrint>, String, Integer> {
        List<WifiFingerPrint> newPrints;

        @Override
        protected Integer doInBackground(List<WifiFingerPrint>... params) {
            newPrints = params[0];
            if(titles == null){
                titles = new ArrayList<>();
            }
            if(childItems == null){
                childItems = new ArrayList<>();
            }
            Long currentTimeStamp = null;
            Float currentX = null;
            Float currentY = null;
            Float currentZ = null;
            for(WifiFingerPrint print: newPrints){
                if( isCancelled() ){
                    Log.d(TAG, "async task cancelled");
                    break;
                }
                if( !print.getTimeStamp().equals( currentTimeStamp )
                        || (currentX == null || currentX != print.getX())
                        || (currentY == null || currentY != print.getY())
                        || (currentZ == null || currentZ != print.getZ())
                        ){
                    Title title = new Title(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(print.getTimeStamp())),
                            String.valueOf(print.getX()), String.valueOf(print.getY()), String.valueOf((int) print.getZ()));
                    titles.add(title);
                    childItems.add(new ArrayList<WifiFingerPrint>());
                    currentTimeStamp = print.getTimeStamp();
                    currentX = print.getX();
                    currentY = print.getY();
                    currentZ = print.getZ();
                }
                ArrayList<WifiFingerPrint> prints = childItems.get(childItems.size()-1);
                prints.add(print);
            }
            return newPrints.size();
        }

        protected void onPostExecute(Integer numberofNewPrints){
            if(numberofNewPrints == limit){
                setTitle((offSet + limit) + " latest prints");
            }else{
                setTitle((offSet + numberofNewPrints) + " latest prints");
            }
            listAdapter.notifyDataSetChanged();
            showNormalState();
            updateDataTask = null;
        }
    }
}
