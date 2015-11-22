package com.course.localization.exactumpositioner;

import com.orm.SugarRecord;

/**
 * Created by Pete on 22.11.2015.
 */
public class WifiFingerPrint extends SugarRecord<WifiFingerPrint>{
    private float x;
    private float y;

    public WifiFingerPrint(){
    }

    public WifiFingerPrint(float x, float y){
        this.x = x;
        this.y = y;
    }


    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    @Override
    public String toString(){
        return "Fingerprint x: " + x + ", Fingerprint y: " + y;
    }
}
