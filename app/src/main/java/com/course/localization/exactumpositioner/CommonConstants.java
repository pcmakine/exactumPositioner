package com.course.localization.exactumpositioner;

/**
 * Created by Pete on 25.11.2015.
 */
public class CommonConstants {
    public static final String FINGERPRINT_KEY = "fingerprints";
    public static final String NEW_RECORDS = "newRecords";
    public static final int NUMBER_OF_SCANS = 10;
    public static final String ACTION_RESP =
            "com.course.localization.exactumpositioner.intent.action.MESSAGE_PROCESSED";
    public static final String SERVICE_RESPONSE_KEY = "response";
    public static final String QUERY_LIMIT_PRINTS =  "SELECT * FROM WIFI_FINGER_PRINT " +
            "ORDER BY TIME_STAMP, MAC DESC " +
            "LIMIT ? OFFSET ?; ";
    public static int DEFAULT_LIMIT = 20;
    //public static final String QUERY_FINGERPRINTS_GROU_BY_COORDINATES = "";
}
