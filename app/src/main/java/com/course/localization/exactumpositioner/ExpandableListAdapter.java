package com.course.localization.exactumpositioner;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.util.ArrayList;

/**
 * Created by Pete on 13.12.2015.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {
    public static final String TAG = ExpandableListAdapter.class.getSimpleName();
    private ArrayList<Title> titles;
    private ArrayList<ArrayList<WifiFingerPrint>> childItems;
    private Context context;

    public ExpandableListAdapter(ArrayList<Title> titles, ArrayList<ArrayList<WifiFingerPrint>> childItems, Context context){
        this.titles = titles;
        this.childItems = childItems;
        this.context = context;
    }
    @Override
    public int getGroupCount() {
        return titles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if( childItems.size() == 0){
            return 0;
        }
        return childItems.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if( convertView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group, null);
        }
        ImageButton btn = (ImageButton) convertView.findViewById(R.id.discardBtn);
        btn.setFocusable(false);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "group with time " + titles.get(groupPosition) + " clicked!");
            }
        });
        CheckedTextView tw = (CheckedTextView) convertView.findViewById(R.id.checkedTW);
        Title title = titles.get(groupPosition);
        tw.setText(title.getDate());
        tw.setChecked(isExpanded);
        TextView coordsView = (TextView) convertView.findViewById(R.id.coordsView);
        coordsView.setText("Floor: " + title.getZ() + ". Position [" + title.getX() + ", " + title.getY() + "]");
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ArrayList<WifiFingerPrint> prints = childItems.get(groupPosition);
        ChildViewHolder holder;
        TextView tw;
        if( convertView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
            holder = new ChildViewHolder();
            holder.networkName = (TextView) convertView.findViewById(R.id.networkNameTW);
            holder.mac = (TextView) convertView.findViewById(R.id.macTW);
            holder.rssi = (TextView) convertView.findViewById(R.id.rssiTW);
            convertView.setTag(holder);
        }else{
            holder = (ChildViewHolder) convertView.getTag();
        }

        holder.networkName.setText(prints.get(childPosition).getNetworkName());
        holder.mac.setText(prints.get(childPosition).getMac());
        holder.rssi.setText(String.valueOf(prints.get(childPosition).getRssi()));
        return convertView;
    }

    private static class ChildViewHolder{
        public TextView networkName;
        public TextView mac;
        public TextView rssi;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }
}
