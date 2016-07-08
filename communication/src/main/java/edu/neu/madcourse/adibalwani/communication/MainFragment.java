package edu.neu.madcourse.adibalwani.communication;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainFragment extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_USERNAME = "username";
    public static final String PROPERTY_APP_VERSION = "appVersion";

    private Activity mActivity;
    private GoogleCloudMessaging mGCM;
    private String mRegId;
    private String mUsername;
    private FirebaseClient mFirebaseClient;
    private Button mRegisterButton;
    private Button mSendButton;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.communication_fragment_main, container, false);
        initButtonListener(rootView);
        mRegisterButton = (Button) rootView.findViewById(R.id.communication_register_button);
        mSendButton = (Button) rootView.findViewById(R.id.communication_send_button);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivity = getActivity();
        mGCM = GoogleCloudMessaging.getInstance(mActivity);
        mFirebaseClient = new FirebaseClient(mActivity);
        notifyRegisterChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();

        if (resourceId == R.id.communication_register_button) {
            if (!isRegistered()) {
                displayRegisterDialog(R.layout.communication_dialog_register);
            } else {
                if (!isNetworkAvailable()) {
                    displayToast("Failed: No Internet Available");
                    return;
                }
                new GCMUnregisterTask().execute();
            }
        } else if (resourceId == R.id.communication_send_button) {
            displaySendDialog(R.layout.communication_dialog_send);
        } else if (resourceId == R.id.communication_acknowledgements_button) {
            displayDialog(R.layout.communication_dialog_acknowledgement);
        } else if (resourceId == R.id.communication_quit_button) {
            endActivity();
        }
    }

    /**
     * Register a broadcast receiver to handle GCM messages
     */
    private void registerBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        filter.addCategory(mActivity.getPackageName());
        filter.setPriority(1);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (!extras.isEmpty()) {
                    String message = extras.getString("message");
                    if (message != null) {
                        displayToast("Received: " + message);
                    }
                }
                abortBroadcast();
            }
        };
        mActivity.registerReceiver(
                mBroadcastReceiver,
                filter,
                "com.google.android.c2dm.permission.SEND",
                null
        );
    }

    /**
     * Initialize onClickListener for buttons
     *
     * @param view Fragment view
     */
    private void initButtonListener(View view) {
        view.findViewById(R.id.communication_register_button).setOnClickListener(this);
        view.findViewById(R.id.communication_send_button).setOnClickListener(this);
        view.findViewById(R.id.communication_acknowledgements_button).setOnClickListener(this);
        view.findViewById(R.id.communication_quit_button).setOnClickListener(this);
    }

    /**
     * Display registeration dialog box containing the given layout
     *
     * @param layoutId Layout to be displayed
     */
    private void displayRegisterDialog(int layoutId) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                RegisterDialogFragment.newInstance(layoutId, new RegisterDialogFragment.DismissListener() {
                    @Override
                    public void onDismiss() {
                        notifyRegisterChanged();
                    }
                });
        dialogFragment.show(fragmentTransaction, "REGISTER_DIALOG");
    }

    /**
     * Display send dialog box containing the given layout
     *
     * @param layoutId Layout to be displayed
     */
    private void displaySendDialog(int layoutId) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        DialogFragment dialogFragment = SendDialogFragment.newInstance(layoutId);
        dialogFragment.show(fragmentTransaction, "SEND_DIALOG");
    }

    /**
     * Handle Quit by killing the current Activity
     */
    private void endActivity() {
        mActivity.finish();
    }

    /**
     * Check whether an Internet Connection is available or not
     *
     * @return true iff internet is available. False, otherwise
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
        editor.remove(PROPERTY_USERNAME);
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
    private String getRegisterId() {
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
    private String getUsername() {
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
     * Get the GCM Shared Preference
     *
     * @return GCM Shared Preference
     */
    private SharedPreferences getGCMPreferences() {
        return mActivity.getSharedPreferences(MainActivity.class.getSimpleName(), Activity.MODE_PRIVATE);
    }

    /**
     * Check if the user has registeration details available
     *
     * @return true iff the user is registered. False, otherwise
     */
    private boolean isRegistered() {
        return mRegId != null && mUsername != null;
    }

    /**
     * Notify change in state for the Register button
     */
    public void notifyRegisterChanged() {
        mRegId = getRegisterId();
        mUsername = getUsername();
        if (!isRegistered()) {
            mRegisterButton.setText(mActivity.getResources().getString(R.string.communication_register_label));
            mSendButton.setVisibility(View.GONE);
        } else {
            mRegisterButton.setText(mActivity.getResources().getString(R.string.communication_unregister_label));
            mSendButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Display a toast message with the given message for 1 second
     *
     * @param message The message to display
     */
    private void displayToast(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Display dialog box containing the given layout
     *
     * @param layoutId Layout to be displayed
     */
    private void displayDialog(int layoutId) {
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        android.app.DialogFragment dialogFragment = CustomDialogFragment.newInstance(layoutId);
        dialogFragment.show(fragmentTransaction, "dialog");
    }

    private class GCMUnregisterTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mGCM.unregister();
                removeRegisterId();
            } catch (IOException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mFirebaseClient.put(mUsername, null);
            removeUsername();
            notifyRegisterChanged();
        }
    }

}
