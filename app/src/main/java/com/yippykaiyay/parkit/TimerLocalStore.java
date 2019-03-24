package com.yippykaiyay.parkit;

import android.content.Context;
import android.content.SharedPreferences;


public class TimerLocalStore {

    public static final String SP_NAME = "TimerDetails";

    SharedPreferences timerLocalDatabase;

    public TimerLocalStore(Context context) {
        timerLocalDatabase = context.getSharedPreferences(SP_NAME, 0);
    }

    public void storeTimerData(String timeString) {
        SharedPreferences.Editor parkedLocalDatabaseEditor = timerLocalDatabase.edit();
        parkedLocalDatabaseEditor.putString("timeString", timeString);


        parkedLocalDatabaseEditor.commit();
    }

    public void isTimerRunning (boolean running) {
        SharedPreferences.Editor parkedLocalDatabaseEditor = timerLocalDatabase.edit();
        parkedLocalDatabaseEditor.putBoolean("running", running);
        parkedLocalDatabaseEditor.commit();
    }

    public void clearTimerData() {
        SharedPreferences.Editor parkedLocalDatabaseEditor = timerLocalDatabase.edit();
        parkedLocalDatabaseEditor.clear();
        parkedLocalDatabaseEditor.commit();
    }

    public String getTimerInfo() {
        if (timerLocalDatabase.getBoolean("running", false) == false) {
            return null;
        }

        String timeString = timerLocalDatabase.getString("timeString", "");

        return timeString;
    }
}