package com.course.localization.exactumpositioner;

/**
 * Created by Pete on 14.12.2015.
 */
public class Title {
    private String date;
    private String x;
    private String y;
    private String z;

    public Title(String date, String x, String y, String z){
        this.date = date;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getZ() {
        return z;
    }

    public void setZ(String z) {
        this.z = z;
    }




}
