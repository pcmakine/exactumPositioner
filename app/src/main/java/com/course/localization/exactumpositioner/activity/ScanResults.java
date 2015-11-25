package com.course.localization.exactumpositioner.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.course.localization.exactumpositioner.CommonConstants;
import com.course.localization.exactumpositioner.R;
import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.util.List;
import java.util.ArrayList;

public class ScanResults extends AppCompatActivity {
    public static final String TAG = ScanResults.class.getSimpleName();

    private List<WifiFingerPrint> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_finger_prints);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        list = (ArrayList<WifiFingerPrint>) getIntent().getSerializableExtra(CommonConstants.FINGERPRINT_KEY);
        List<String> listForListView = new ArrayList<>();
        for(WifiFingerPrint print: list){
            listForListView.add(print.toString());
        }
        ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listForListView);
        ((ListView) findViewById(R.id.fingerprintList)).setAdapter(listAdapter);
    }

}
