package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Class used to manage registeration in Shared Preference
 */
public class RegisterManager {

    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_USERNAME = "username";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_OPPONENT = "opponent";
    public static final String PREF_FILE = MainActivity.class.getSimpleName();

    private Activity mActivity;
    private String mRegId;
    private String mUsername;
    private String mOpponent;

    public RegisterManager(Activity activity) {
        mActivity = activity;
        notifyRegisterChanged();
    }

    /**
     * Display registeration dialog box containing the given layout
     *
     * @param layoutId Layout to be displayed
     * @param listener Listener to call on dismiss
     */
    public void displayRegisterDialog(final int layoutId, final RegisterDialogFragment.DismissListener listener) {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                RegisterDialogFragment.newInstance(layoutId, new RegisterDialogFragment.DismissListener() {
                    @Override
                    public void onDismiss() {
                        notifyRegisterChanged();
                        listener.onDismiss();
                    }
                });
        dialogFragment.show(fragmentTransaction, "REGISTER_DIALOG");
    }

    /**
     * Check if the user has registeration details available
     *
     * @return true iff the user is registered. False, otherwise
     */
    public boolean isRegistered() {
        return mRegId != null && mUsername != null;
    }

    /**
     * Register with the given registeration ID, username
     * in Shared Preference
     *
     * @param regId Registeration ID
     * @param username Username
     */
    public void register(String regId, String username) {
        storeRegisterId(regId);
        storeUsername(username);
        notifyRegisterChanged();
    }

    /**
     * Unregister by deleting registeration ID, username
     * in Shared Preference
     */
    public void unregister() {
        removeRegisterId();
        removeUsername();
        notifyRegisterChanged();
    }

    /**
     * Register the given opponent in Shared Preference
     *
     * @param opponent Opoonent
     */
    public void registerOpponent(String opponent) {
        storeOpponent(opponent);
        notifyRegisterChanged();
    }

    /**
     * Unregister the given opponent from Shared Preference
     */
    public void unregisterOpponent() {
        removeOpponent();
        notifyRegisterChanged();
    }

    /**
     * Get the username
     *
     * @return Username if found. Null otherwise
     */
    public String getUsername() {
        mUsername = fetchUsername();
        return mUsername;
    }

    /**
     * Get the opponent
     *
     * @return Opponent if found. Null otherwise
     */
    public String getOpponent() {
        return mOpponent;
    }

    /**
     * Notify change in state
     */
    private void notifyRegisterChanged() {
        mRegId = fetchRegisterId();
        mUsername = fetchUsername();
        mOpponent = fetchOpponent();
    }

    /**
     * Store the registeration ID in Shared Preference
     */
    private void storeRegisterId(String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Store the username in Shared Preference
     */
    private void storeUsername(String username) {
        final SharedPreferences prefs = getGCMPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_USERNAME, username);
        editor.commit();
    }

    /**
     * Store the opponent in Shared Preference
     */
    private void storeOpponent(String opponent) {
        final SharedPreferences prefs = getGCMPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_OPPONENT, opponent);
        editor.commit();
    }

    /**
     * Remove the GCM registeration ID from Shared Preference
     */
    private void removeRegisterId() {
        final SharedPreferences prefs = getGCMPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_REG_ID);
        editor.commit();
    }

    /**
     * Remove the username from Shared Preference
     */
    private void removeUsername() {
        final SharedPreferences prefs = getGCMPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        //editor.remove(PROPERTY_USERNAME);
        editor.putString(PROPERTY_USERNAME, null);
        editor.commit();
    }

    /**
     * Remove the opponent from Shared Preference
     */
    private void removeOpponent() {
        final SharedPreferences prefs = getGCMPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_OPPONENT);
        editor.commit();
    }

    /**
     * Get the App Version
     *
     * @return App version
     */
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = mActivity.getPackageManager()
                    .getPackageInfo(mActivity.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Get the GCM registeration ID from Shared Preference
     *
     * @return ID of registeration if found. Null otherwise
     */
    private String fetchRegisterId() {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, null);
        if (registrationId == null) {
            return null;
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            return null;
        }
        return registrationId;
    }

    /**
     * Get the Username from Shared Preference
     *
     * @return Username if found. Null otherwise
     */
    private String fetchUsername() {
        final SharedPreferences prefs = getGCMPreferences();
        String username = prefs.getString(PROPERTY_USERNAME, null);
        if (username == null) {
            return null;
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            return null;
        }
        return username;
    }

    /**
     * Get the Opponent from Shared Preference
     *
     * @return Opponent if found. Null otherwise
     */
    private String fetchOpponent() {
        final SharedPreferences prefs = getGCMPreferences();
        String opponent = prefs.getString(PROPERTY_OPPONENT, null);
        if (opponent == null) {
            return null;
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            return null;
        }
        return opponent;
    }

    /**
     * Get the GCM Shared Preference
     *
     * @return GCM Shared Preference
     */
    private SharedPreferences getGCMPreferences() {
        return mActivity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
    }
}
