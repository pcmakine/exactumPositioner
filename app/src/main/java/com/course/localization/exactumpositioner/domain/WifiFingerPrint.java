package com.course.localization.exactumpositioner.domain;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by Pete on 22.11.2015.
 */
public class WifiFingerPrint extends SugarRecord<WifiFingerPrint> implements Serializable{
    private float x;
    private float y;
    private float z;
    private int rssi;
    private String mac;
    private String networkName;

    public WifiFingerPrint(){
    }

    public WifiFingerPrint(float x, float y, float z, Integer rssi, String mac, String networkName){
        this.x = x;
        this.y = y;
        this.z = z;
        this.rssi = rssi;
        this.mac = mac;
        this.networkName = networkName;
    }

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public int getRssi() {
        return rssi;
    }

    public String getMac() {
        return mac;
    }

    public String getNetworkName() {
        return networkName;
    }


    @Override
    public String toString(){
        return networkName + ": (" + x + ", " + y + ", " + z + "), " + rssi + ", " + mac;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WifiFingerPrint that = (WifiFingerPrint) o;

        if (Float.compare(that.x, x) != 0) return false;
        if (Float.compare(that.y, y) != 0) return false;
        if (Float.compare(that.z, z) != 0) return false;
        if (rssi != that.rssi) return false;
        if (mac != null ? !mac.equals(that.mac) : that.mac != null) return false;
        return !(networkName != null ? !networkName.equals(that.networkName) : that.networkName != null);

    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        result = 31 * result + rssi;
        result = 31 * result + (mac != null ? mac.hashCode() : 0);
        result = 31 * result + (networkName != null ? networkName.hashCode() : 0);
        return result;
    }
}
