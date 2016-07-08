package edu.neu.madcourse.adibalwani.finalproject;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Shared Preference for GamePlay
 */
public class GameSharedPreference {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public GameSharedPreference(Activity activity) {
        prefs = activity.getSharedPreferences(Constants.PREF_FILE, 0);
        editor = prefs.edit();
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return prefs.getString(key, "");
    }
}
