package com.course.localization.exactumpositioner;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.widget.ImageView;

/**
 * Created by Pete on 23.11.2015.
 */

/**
 * Interface to be used in conjunction with the CustomImageView
 * The CustomImageView takes an object that implements this interface and delegates drawing to it
 * This way we can keep CustomImageView as re-usable as possible, so it can easily be used in future projects
 */
public interface ImageViewDrawer {
    public void onDraw(Canvas canvas, CustomImageView view);

    public boolean isPointInImage(PointF point, CustomImageView view);
}
