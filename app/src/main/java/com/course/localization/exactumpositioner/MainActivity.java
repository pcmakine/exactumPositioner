package com.course.localization.exactumpositioner;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private CustomImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        imageView = (CustomImageView) findViewById(R.id.imageView);

        Button btn = (Button) findViewById(R.id.saveBtn);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);


        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.floor_names)));

    }

    public void saveRecord(View v){
        if(imageView != null && imageView.getLastPoint() != null){
            WifiFingerPrint fp = new WifiFingerPrint(imageView.getLastPoint().x, imageView.getLastPoint().y);
            fp.save();
        }
    }

    public void logRecords(View view){
        List<WifiFingerPrint> wifiFingerPrints = WifiFingerPrint.listAll(WifiFingerPrint.class);
        Log.d(TAG, "all fingerPrints: ");
        for(WifiFingerPrint print: wifiFingerPrints){
            Log.d(TAG, print.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
