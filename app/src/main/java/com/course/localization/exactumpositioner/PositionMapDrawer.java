package com.course.localization.exactumpositioner;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Pete on 23.11.2015.
 */
public class PositionMapDrawer implements ImageViewDrawer{
    public static final String TAG = PositionMapDrawer.class.getSimpleName();
    private List<WifiFingerPrint> fingerPrints;
    private boolean showFingerPrints;
    private int floorNumber;
    private static final Map<Integer, Integer> floorPlans;
    static {
        Map<Integer, Integer> aMap = new HashMap<>();
        aMap.put(0, R.drawable.basement_0_exactum);
        aMap.put(1, R.drawable.floor_1_exactum);
        aMap.put(2, R.drawable.floor_2_exactum);
        aMap.put(3, R.drawable.floor_3_exactum);
        aMap.put(4, R.drawable.floor_4_exactum);
        floorPlans = Collections.unmodifiableMap(aMap);
    }

    public PositionMapDrawer(List<WifiFingerPrint> fingerPrints, int floorNumber, ImageView view){
        this.fingerPrints = fingerPrints;
        setFloorNumber(view, floorNumber);
    }

    public void onDraw(Canvas canvas, CustomImageView view) {
        if(fingerPrints != null && !fingerPrints.isEmpty() && showFingerPrints){
            for (WifiFingerPrint fingerPrint : fingerPrints){
                PointF point = imageCoordsToScreenCoords(new PointF(fingerPrint.getX(), fingerPrint.getY()), view);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.RED);
                canvas.drawCircle(point.x, point.y, 10, paint);
            }
        }else{
            if(isPointInImage(view.getLastPoint(), view)){
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.RED);
                canvas.drawCircle(view.getLastPoint().x, view.getLastPoint().y, 10, paint);

                PointF point = view.translateCoordinatesBack(view.getLastPoint());
                TextView xView = (TextView) ((Activity) view.getContext()).findViewById(R.id.xCoordinate);
                xView.setText(view.getContext().getResources().getString(R.string.xCoordinateLabelBase) + " " + point.x);
                TextView yView = (TextView) ((Activity) view.getContext()).findViewById(R.id.yCoordinate);
                yView.setText(view.getContext().getResources().getString(R.string.yCoordinateLabelBase) + " " + point.y);
            }
        }
    }

    public PointF imageCoordsToScreenCoords(PointF point, CustomImageView view){
        float[] pts = {point.x, point.y};
        Matrix matrix = view.getImageMatrix();
        matrix.mapPoints(pts);
        return new PointF(pts[0], pts[1]);
    }

    public boolean isPointInImage(PointF point, CustomImageView view){
        int width = view.getDrawable().getIntrinsicWidth();
        int height = view.getDrawable().getIntrinsicHeight();
        point = view.translateCoordinatesBack(point);
        return point.x >= 0 && point.y >= 0 && point.x <= width && point.y <= height;
    }

    public void toggleShowFingerPrints(List<WifiFingerPrint> fingerPrints, ImageView view){
        this.fingerPrints = fingerPrints;
        this.showFingerPrints = !showFingerPrints;
        view.invalidate();
    }

    public void setFloorNumber(ImageView view, int floorNumber){
        if(floorPlans.get(floorNumber) != null){
            this.floorNumber = floorNumber;
            this.fingerPrints = WifiFingerPrint.find(WifiFingerPrint.class, "z= ?", String.valueOf(floorNumber));
            TextView header = (TextView) ((Activity) view.getContext()).findViewById(R.id.floorNumberTitle);
            header.setText(view.getContext().getResources().getString(R.string.floor_base) + floorNumber);
            view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), floorPlans.get(floorNumber)));
        }
    }
    public int getFloorNumber(){
        return floorNumber;
    }

}
