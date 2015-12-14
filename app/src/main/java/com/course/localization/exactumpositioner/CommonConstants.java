package com.course.localization.exactumpositioner;

/**
 * Created by Pete on 25.11.2015.
 */
public class CommonConstants {
    public static final String FINGERPRINT_KEY = "fingerprints";
    public static final String LIMIT_KEY = "limit";
    public static final String OFFSET_KEY = "offset";
    public static final String NEW_RECORDS = "newRecords";
    public static final int NUMBER_OF_SCANS = 1;
    public static final String ACTION_RESP =
            "com.course.localization.exactumpositioner.intent.action.MESSAGE_PROCESSED";
    public static final String SERVICE_RESPONSE_KEY = "response";
    public static final String SERVICE_ACTION_PERFORMED =
            "com.course.localization.exactumpositioner.intent.action.ACTION_PERFORMED";
    public static final String QUERY_LIMIT_PRINTS =  "SELECT * FROM WIFI_FINGER_PRINT " +
            "ORDER BY TIME_STAMP,X, Y, Z DESC, RSSI ASC " +
            "LIMIT ? OFFSET ?; ";
    public static int DEFAULT_LIMIT = 1000;
    //public static final String QUERY_FINGERPRINTS_GROU_BY_COORDINATES = "";
}
