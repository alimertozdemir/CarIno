package com.carino.hackathon.speedscreen.utils;

import android.content.Context;
import android.content.ContextWrapper;

import com.carino.hackathon.speedscreen.CarInoApplication;
import com.pixplicity.easyprefs.library.Prefs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by alimertozdemir on 4.11.2017.
 */

public class ApplicationUtility {

    private static Context context = CarInoApplication.getContext();

    public static void initializePrefs() {
        new Prefs.Builder()
                .setContext(context)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(context.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

    public static String getHourAndMinutesFromDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a");
        String formattedDate = sdf.format(date);
        return formattedDate;
    }
}
