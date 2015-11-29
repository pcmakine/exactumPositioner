package com.course.localization.exactumpositioner;

import com.course.localization.exactumpositioner.domain.WifiFingerPrint;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Pete on 29.11.2015.
 */
public class DataExporterTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void printStringBuiltCorrectlyOneMacPerLocation() throws Exception{
        List<WifiFingerPrint> prints = new ArrayList<>();
        WifiFingerPrint print = new WifiFingerPrint(
                2,
                3,
                4,
                -77,
                "d8:b1:90:45:18:de",
                "test",
                System.currentTimeMillis()
        );
        WifiFingerPrint print2 = new WifiFingerPrint(
                3,
                3,
                4,
                -77,
                "d8:b1:90:45:18:dd",
                "test",
                System.currentTimeMillis()
        );
        prints.add(print);
        prints.add(print2);
        String expected = "4.0;2.0;3.0;238257141258462;-77\n4.0;3.0;3.0;238257141258461;-77";
        assertEquals(expected, DataExporter.printsToString(prints));
    }

    @Test
    public void printStringBuiltCorrectlyMultipleMacsPerLocation() throws Exception{
        List<WifiFingerPrint> prints = new ArrayList<>();
        WifiFingerPrint print = new WifiFingerPrint(
                2,
                3,
                4,
                -77,
                "d8:b1:90:45:18:de",
                "test",
                System.currentTimeMillis()
        );
        WifiFingerPrint print2 = new WifiFingerPrint(
                2,
                3,
                4,
                -75,
                "d8:b1:90:45:18:dd",
                "test",
                System.currentTimeMillis()
        );
        WifiFingerPrint print3 = new WifiFingerPrint(
                2,
                5,
                4,
                -20,
                "d8:b1:90:45:18:aa",
                "test",
                System.currentTimeMillis()
        );
        prints.add(print);
        prints.add(print2);
        prints.add(print3);
        String expected = "4.0;2.0;3.0;238257141258462;-77;238257141258461;-75\n4.0;2.0;5.0;238257141258410;-20";
        assertEquals(expected, DataExporter.printsToString(prints));
    }
}
