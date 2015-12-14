package com.course.localization.exactumpositioner;

import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import java.util.List;

/**
 * Created by Pete on 13.12.2015.
 *
 * This class is used in conjunction with DbService. It is needed if the user loads a lot of fingerprints.
 * In this case they cannot be put into intent and delivered that way to the activity. That's why the
 * DbService saves them here, and the activity can get the list of prints from this classa after being
 * notified by the DbService that the fetch has been done.
 */
public class FreshDataHolder {
    private static FreshDataHolder mInstance;
    private List<WifiFingerPrint> latestFetchedPrints;

    public static synchronized FreshDataHolder getInstance(){
        if(mInstance == null){
            mInstance = new FreshDataHolder();
        }
        return mInstance;
    }

    private FreshDataHolder(){
    }

    public void setLatestFetchedPrints(List<WifiFingerPrint> prints){
        this.latestFetchedPrints = prints;
    }

    public List<WifiFingerPrint> getLatestFetchedPrints(){
        return this.latestFetchedPrints;
    }

}
