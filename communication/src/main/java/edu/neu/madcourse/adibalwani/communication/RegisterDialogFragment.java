package edu.neu.madcourse.adibalwani.communication;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class RegisterDialogFragment extends DialogFragment {
    private static final String BUNDLE_LAYOUT_ID = "1";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String GCM_SENDER_ID = "413853235241";
    private static final String LOG_TAG = RegisterDialogFragment.class.getSimpleName();

    private Activity mActivity;
    private int mLayoutId;
    private GoogleCloudMessaging mGCM;
    private String mRegId;
    private EditText mUsername;
    private FirebaseClient mFirebaseClient;
    private DismissListener mDismissListener;

    public interface DismissListener {
        /**
         * Method to call on Dismiss
         */
        void onDismiss();
    }

    /**
     * Create new instance of RegisterDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of RegisterDialogFragment
     */
    static RegisterDialogFragment newInstance(int layoutId, DismissListener listener) {
        RegisterDialogFragment dialogFragment = new RegisterDialogFragment();
        dialogFragment.setDismissListener(listener);
        Bundle args = new Bundle();
        args.putInt(BUNDLE_LAYOUT_ID, layoutId);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mLayoutId = getArguments().getInt(BUNDLE_LAYOUT_ID);
        mGCM = GoogleCloudMessaging.getInstance(mActivity);
        mFirebaseClient = new FirebaseClient(mActivity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(mLayoutId, null);
        mUsername = (EditText) view.findViewById(R.id.communication_register_dialog_username_edittext);
        final AlertDialog dialog = builder
                .setView(view)
                .setPositiveButton(R.string.communication_register_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do Nothing
                    }
                })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        register();
                    }
                });
            }
        });

        return dialog;
    }

    /**
     * Register in GCM and store the username in Cloud Key-Value Store
     */
    private void register() {
        if (!isNetworkAvailable()) {
            displayToast("Failed: No Internet Available");
            return;
        }

        mFirebaseClient.get(mUsername.getText().toString(), new FirebaseClient.ResponseListener() {
            @Override
            public void onSuccess(String value) {
                // Check if username is available
                if (value == null && checkPlayServices()) {
                    new GCMRegisterTask().execute();
                } else {
                    displayToast("User already exists");
                }
            }
        });
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
     * Display a toast message with the given message for 1 second
     *
     * @param message The message to display
     */
    private void displayToast(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Check if play services are available on the phone
     *
     * @return true iff play services are available. False, otherwise
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                RegisterDialogFragment.this.getDialog().cancel();
            }
            return false;
        }
        return true;
    }

    /**
     * Store the registeration ID in Shared Preference
     */
    private void storeRegisterId() {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion();
        Log.i(LOG_TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MainFragment.PROPERTY_REG_ID, mRegId);
        editor.putInt(MainFragment.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Store the username in Shared Preference
     */
    private void storeUsername() {
        final SharedPreferences prefs = getGCMPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MainFragment.PROPERTY_USERNAME, mUsername.getText().toString());
        editor.commit();
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
     * Make a network call and register in GCM
     */
    private class GCMRegisterTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mRegId = mGCM.register(GCM_SENDER_ID);
                storeRegisterId();
            } catch (IOException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mFirebaseClient.put(mUsername.getText().toString(), mRegId);
            storeUsername();
            displayToast("Successfully Registered");
            mDismissListener.onDismiss();
            RegisterDialogFragment.this.getDialog().cancel();
        }
    }

    private void setDismissListener(DismissListener dismissListener) {
        this.mDismissListener = dismissListener;
    }
}
