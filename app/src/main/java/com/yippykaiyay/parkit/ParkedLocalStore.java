package com.yippykaiyay.parkit;

import android.content.Context;
import android.content.SharedPreferences;


public class ParkedLocalStore {

    public static final String SP_NAME = "ParkedDetails";

    SharedPreferences parkedLocalDatabase;

    public ParkedLocalStore(Context context) {
        parkedLocalDatabase = context.getSharedPreferences(SP_NAME, 0);
    }

    public void storeParkedData(Marker marker) {
        SharedPreferences.Editor parkedLocalDatabaseEditor = parkedLocalDatabase.edit();
        parkedLocalDatabaseEditor.putString("title", marker.title);
        parkedLocalDatabaseEditor.putString("snippet", marker.snippet);
        parkedLocalDatabaseEditor.putString("position", marker.position);

        parkedLocalDatabaseEditor.commit();
    }

    public void setUserParked(boolean parked) {
        SharedPreferences.Editor parkedLocalDatabaseEditor = parkedLocalDatabase.edit();
        parkedLocalDatabaseEditor.putBoolean("parked", parked);
        parkedLocalDatabaseEditor.commit();
    }

    public void clearParkedData() {
        SharedPreferences.Editor parkedLocalDatabaseEditor = parkedLocalDatabase.edit();
        parkedLocalDatabaseEditor.clear();
        parkedLocalDatabaseEditor.commit();
    }

    public Marker getParkedLocation() {
        if (parkedLocalDatabase.getBoolean("parked", false) == false) {
            return null;
        }

        String title = parkedLocalDatabase.getString("title", "");
        String snippet = parkedLocalDatabase.getString("snippet", "");
        String position = parkedLocalDatabase.getString("position", "");


        Marker marker = new Marker(title, snippet, position);
        return marker;
    }
}
