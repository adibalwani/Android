package edu.neu.madcourse.adibalwani.finalproject.register;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import edu.neu.madcourse.adibalwani.finalproject.Constants;

/**
 * Class used to manage registeration in Shared Preference
 */
public class RegisterManager {

    private Context mContext;
    private String mUsername;

    public RegisterManager(Context context) {
        mContext = context;
        notifyRegisterChanged();
    }

    /**
     * Check if the user has registeration details available
     *
     * @return true iff the user is registered. False, otherwise
     */
    public boolean isRegistered() {
        return mUsername != null;
    }

    /**
     * Register with the given username in Shared Preference
     *
     * @param username Username
     */
    public void register(String username) {
        storeUsername(username);
        notifyRegisterChanged();
    }

    /**
     * Get the username
     *
     * @return Username if found. Null otherwise
     */
    public String getUsername() {
        return mUsername;
    }

    /**
     * Display a toast message - "Successfully Registered" for 1 second
     */
    public void displayRegisteredToast() {
        Toast.makeText(mContext, Constants.SUCCESSFULLY_REGISTERED, Toast.LENGTH_LONG).show();
    }

    /**
     * Notify change in state
     */
    public void notifyRegisterChanged() {
        mUsername = fetchUsername();
    }

    /**
     * Store the username in Shared Preference
     */
    private void storeUsername(String username) {
        SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PROPERTY_USERNAME, username);
        editor.commit();
    }

    /**
     * Get the Username from Shared Preference
     *
     * @return Username if found. Null otherwise
     */
    private String fetchUsername() {
        SharedPreferences prefs = getSharedPreferences();
        return prefs.getString(Constants.PROPERTY_USERNAME, null);
    }

    /**
     * Get the Shared Preference
     *
     * @return Shared Preference
     */
    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(Constants.PREF_FILE, Activity.MODE_PRIVATE);
    }
}
