package com.course.localization.exactumpositioner;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by Pete on 19.11.2015.
 */

//http://vivin.net/2011/12/04/implementing-pinch-zoom-and-pandrag-in-an-android-view-on-the-canvas/2/
public class ZoomView extends View {
    private static final String TAG = ZoomView.class.getSimpleName();
    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 5f;
    private float latestX = 0;
    private float latestY = 0;

    private float scaleFactor = 1.f;
    private ScaleGestureDetector detector;

    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;

    private int mode;
    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float startX = 0f;
    private float startY = 0f;

    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float translateX = 0f;
    private float translateY = 0f;
    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;
    //This flag reflects whether the finger was actually dragged across the screen
    private boolean dragged = true;

    public ZoomView(Context context) {
        this(context, null);
    }

    public ZoomView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        detector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //This is the basic skeleton for our code. We examine each of the possible motion-events that can happen
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //The first finger has been pressed. The only action that the user can take now is to pan/drag so let's
                //set the mode to DRAG
                mode = DRAG;

                //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated
                //amount for each coordinates This works even when we are translating the first time because the initial
                //values for these two variables is zero.
                startX = event.getX() - previousTranslateX;
                startY = event.getY() - previousTranslateY;
                break;

            case MotionEvent.ACTION_MOVE:
                translateX = event.getX() - startX;
                translateY = event.getY() - startY;

                //We cannot use startX and startY directly because we have adjusted their values using the previous translation values. This is why we need to add those
                //values to startX and startY so that we can get the actual coordinates of the finger.
                double distance = Math.sqrt(Math.pow(event.getX() - (startX + previousTranslateX), 2) + Math.pow(event.getY() - (startY + previousTranslateY), 2));

                if(distance > 0) {
                    dragged = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //The second finger has been placed on the screen and so we need to set the mode to ZOOM
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_UP:
                //All fingers are off the screen and so we're neither dragging nor zooming.
                mode = NONE;
                dragged = false;
                //All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and
                //previousTranslateY
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                //The second finger is off the screen and so we're back to dragging.
                mode = DRAG;
                //This is not strictly necessary; we save the value of translateX and translateY into previousTranslateX
                //and previousTranslateY when the second finger goes up
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;
        }

        detector.onTouchEvent(event);

        //We redraw the canvas only in the following cases:
        //
        // o The mode is ZOOM
        //        OR
        // o The mode is DRAG and the scale factor is not equal to 1 (meaning we have zoomed) and dragged is
        //   set to true (meaning the finger has actually moved)
        if ((mode == DRAG && scaleFactor != 1f && dragged) || mode == ZOOM) {
            invalidate();
        }

        Log.d(TAG, "touched point x: " + event.getX() + " , y: " + event.getY());
        latestX = event.getX();
        latestY = event.getY();
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.exactum1, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;

        Log.d(TAG, "imageHeight: "+ options.outHeight);
        Log.d(TAG, "imageWidth: "+ options.outWidth);
        Log.d(TAG, "imageType: "+ options.outMimeType);

        int x = getWidth()/2;
        int y = getHeight()/2;
        canvas.save();
        canvas.scale(scaleFactor, scaleFactor, latestX, latestY);   //scale around the center
        /*
        Log.d(TAG, "X: " + x);
        Log.d(TAG, "Y: " + y);
        int radius;
        radius = 100;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);

        canvas.drawCircle(x, y, radius, paint);
*/
        Drawable d = ContextCompat.getDrawable(getContext(), R.drawable.exactum1);
                getResources().getDrawable(R.drawable.exactum1);
        d.setBounds(0, 0, 3000, 3000);
        d.draw(canvas);

        //If translateX times -1 is lesser than zero, let's set it to zero. This takes care of the left bound
        if((translateX * -1) < 0) {
            translateX = 0;
        }

        //This is where we take care of the right bound. We compare translateX times -1 to (scaleFactor - 1) * displayWidth.
        //If translateX is greater than that value, then we know that we've gone over the bound. So we set the value of
        //translateX to (1 - scaleFactor) times the display width. Notice that the terms are interchanged; it's the same
        //as doing -1 * (scaleFactor - 1) * displayWidth
        else if((translateX * -1) > (scaleFactor - 1) * getDisplay().getWidth()) {
            translateX = (1 - scaleFactor) * getDisplay().getWidth();
        }

        if(translateY * -1 < 0) {
            translateY = 0;
        }

        //We do the exact same thing for the bottom bound, except in this case we use the height of the display
        else if((translateY * -1) > (scaleFactor - 1) * getDisplay().getHeight()) {
            translateY = (1 - scaleFactor) * getDisplay().getHeight();
        }

        //We need to divide by the scale factor here, otherwise we end up with excessive panning based on our zoom level
        //because the translation amount also gets scaled according to how much we've zoomed into the canvas.
        canvas.translate(translateX / scaleFactor, translateY / scaleFactor);
        canvas.restore();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
            //invalidate();
            return true;
        }


    }
}
