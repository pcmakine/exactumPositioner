package com.course.localization.exactumpositioner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class CalibrationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = CalibrationActivity.class.getSimpleName();
    private CustomImageView imageView;
    private WifiManager mainWifi;
    private PowerManager.WakeLock wakeLock;
    private WifiReceiver receiverWifi;
    private List<ScanResult> wifiList;
    static StringBuilder fingerprint;
    static StringBuilder macs;
    static StringBuilder rssi;
    //Popup dialog that displays progress (also helps detect if the user has aborted the training process)
    static ProgressDialog progressDialog;
    //The maximum number of fingerprints we want to record (for a ballpark figure, assume approx. 1 fingerprint/second on current Android devices)
    static final int MAXPRINTS = 10;
    //Asynchronous task (thread). We capture the initialization so we can control it after it's started
    AsyncTask<Integer, String, Hashtable<String, List<Integer>>> task = new RecordFingerprints();
    //Root directory of the phone's SD card. We delve into subfolders from here
    static final File PATH = Environment.getExternalStorageDirectory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_activity_calibration);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        imageView = (CustomImageView) findViewById(R.id.imageView);
        imageView.setImageViewDrawer(new PositionMapDrawer(WifiFingerPrint.listAll(WifiFingerPrint.class), 1, imageView));

        //Initializations
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, "My wakelock");
        receiverWifi = new WifiReceiver();
        progressDialog= new ProgressDialog(CalibrationActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.setTitle("RECORDING");
        progressDialog.setMax(MAXPRINTS);
        fingerprint = new StringBuilder();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    }

    public void saveRecord(View v){
        if(imageView != null && imageView.getLastPoint() != null && imageView.getDrawer().isPointInImage(imageView.getLastPoint(), imageView)){
            PointF point = imageView.getLasPointInBitmapCoords();
            Log.d(TAG, "last point: " + point.toString());
            WifiFingerPrint fp = new WifiFingerPrint(point.x, point.y, ((PositionMapDrawer) imageView.getDrawer()).getFloorNumber());
            fp.save();
        }
    }

    public void toggleShowFingerPrints(){
        if(imageView != null){
            int floorNumber = ((PositionMapDrawer) imageView.getDrawer()).getFloorNumber();
            List<WifiFingerPrint> prints = WifiFingerPrint.find(WifiFingerPrint.class, "z= ?", String.valueOf(floorNumber));
            if(prints == null || prints.isEmpty()){
                Toast.makeText(this, "No fingerprints recorded yet for this floor!", Toast.LENGTH_LONG).show();
            }
            ((PositionMapDrawer) imageView.getDrawer()).toggleShowFingerPrints(prints, imageView);
        }
    }

    public void startScan(View view){
        mainWifi.startScan();
        progressDialog.show();
       /* //Task has been initialized but not run a single time yet
        if(task.getStatus()== AsyncTask.Status.PENDING){
            //Show the progress dialog
            progressDialog.setTitle("TRAINING CELL ");
            progressDialog.show();
            //Start the recording
            task.execute(0);

        }
        //Task has been allowed to finish
        if(task.getStatus()== AsyncTask.Status.FINISHED){
            //Re-initialize
            task = new RecordFingerprints();
            progressDialog.setTitle("TRAINING");
            progressDialog.show();
            task.execute(0);
        }*/
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calibration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_save){
            saveRecord(null);
        }else if(id == R.id.action_show_all){
            toggleShowFingerPrints();
        }else if(id == R.id.action_delete_all){
            AlertDialog diaBox = ConfirmDelete();
            diaBox.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.basement) {
            ((PositionMapDrawer) imageView.getDrawer()).setFloorNumber(imageView, 0);
        } else if (id == R.id.firstFloor) {
            ((PositionMapDrawer) imageView.getDrawer()).setFloorNumber(imageView, 1);
        }else if (id == R.id.secondFloor) {
            ((PositionMapDrawer) imageView.getDrawer()).setFloorNumber(imageView, 2);
        } else if (id == R.id.thirdFloor) {
            ((PositionMapDrawer) imageView.getDrawer()).setFloorNumber(imageView, 3);
        } else if (id == R.id.fourthFloor) {
            ((PositionMapDrawer) imageView.getDrawer()).setFloorNumber(imageView, 4);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private AlertDialog ConfirmDelete()
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Do you want to Delete")
                .setIcon(R.drawable.ic_delete_white_24dp)

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        WifiFingerPrint.deleteAll(WifiFingerPrint.class);
                        dialog.dismiss();
                    }

                })

                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .create();
        return myQuittingDialogBox;

    }


    class WifiReceiver extends BroadcastReceiver {
        public final String TAG = WifiReceiver.class.getSimpleName();

        /*
        What to do when our BroadcastReceiver (or in this case, the WifiReceiver that implements it) returns its result
         */
        public void onReceive(Context c, Intent intent) {
            Log.d("FINGER","Scan received");

            if(progressDialog.getProgress()<progressDialog.getMax()){
                wifiList = mainWifi.getScanResults();

                rssi = new StringBuilder();
                macs = new StringBuilder();
                for(int j=0;j<wifiList.size();j++){
                    macs.append(wifiList.get(j).BSSID);
                    if(j<wifiList.size()-1){
                        macs.append(",");
                    }
                    rssi.append(wifiList.get(j).level);
                    if(j<wifiList.size()-1){
                        rssi.append(",");
                    }
                }
                fingerprint.append(macs);
                fingerprint.append("\n");
                fingerprint.append(rssi);
                fingerprint.append("\n");

                if(progressDialog.getProgress() == 1){
                    Toast.makeText(CalibrationActivity.this, fingerprint.toString(), Toast.LENGTH_LONG).show();
                }
                progressDialog.incrementProgressBy(1);

                mainWifi.startScan();
                Log.d("FINGER", "Scan initiated");
                Log.d(TAG, "progress: " + progressDialog.getProgress());
            }else{
                progressDialog.dismiss();
                progressDialog.setProgress(0);
            }

        }
    }

    //Asynchronous task runs in background so we don't make the UI wait
    private class RecordFingerprints extends AsyncTask<Integer, String, Hashtable<String,List<Integer>>> {
        boolean running = true;
        protected Hashtable<String,List<Integer>> doInBackground(Integer... params) {
            progressDialog.setProgress(0);
            mainWifi.startScan();
            fingerprint = new StringBuilder();

            wakeLock.acquire();
            while(running){
                //Store the recorded fingerprint in a file named after the cell in which it was recorded
                if(!progressDialog.isShowing() || progressDialog.getProgress()>=progressDialog.getMax()){
                    File file = new File(PATH, "/fingerprints/"+params[0]+".txt");
                    try{
                        OutputStream os = new FileOutputStream(file,false);
                        os.write(fingerprint.toString().getBytes());
                        os.close();
                    }
                    catch(Exception e){Log.d("HELP","Need somebody");}
                    progressDialog.dismiss();
                    return null;
                }

                else{
                    publishProgress("");
                }

                Thread.currentThread();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            wakeLock.release();
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            //progressDialog.incrementProgressBy(1);
        }

        @SuppressWarnings("unused")
        protected void onPostExecute(ArrayList<Integer> result) {
            running = true;
        }

        @Override
        protected void onCancelled() {
            progressDialog.dismiss();
            running = false;
            return;
        }

    }

}
