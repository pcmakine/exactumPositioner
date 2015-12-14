package com.course.localization.exactumpositioner;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.util.List;

/**
 * Created by Pete on 12.12.2015.
 */
public class ListViewAdapterDeprecated extends ArrayAdapter<WifiFingerPrint> {
    public static final String TAG = ListViewAdapterDeprecated.class.getSimpleName();
    private final Context context;
    private List<WifiFingerPrint> data;


    public ListViewAdapterDeprecated(Context context, List<WifiFingerPrint> data) {
        super(context, -1, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if( convertView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.networkNameTW);
            holder.btn = (ImageButton) convertView.findViewById(R.id.discardBtn);
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }

       /* LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.fingerPrintTW);
        ImageButton btn = (ImageButton) rowView.findViewById(R.id.discardBtn);*/

        final int pos = position;
        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "position: " + pos + " print: " + data.get(pos).toString());
            }
        });
        holder.text.setText(data.get(position).toString());
        return convertView;
    }

    private static class ViewHolder{
        public TextView text;
        public ImageButton btn;
    }
}
