package com.example.coveryourapps;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings extends Application {
    private static Settings thisSettings;

    private static boolean autoAddFriends;


    @Override
    public void onCreate() {
        super.onCreate();
        thisSettings = this;
    }
    public static Settings getInstance() {
        return thisSettings;
    }


    public static void setAutoAddFriends(MainActivity mainActivity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        autoAddFriends = prefs.getBoolean("autoAddFriends", true);
    }
    public static boolean isAutoAddFriends() {
        return autoAddFriends;
    }

}
