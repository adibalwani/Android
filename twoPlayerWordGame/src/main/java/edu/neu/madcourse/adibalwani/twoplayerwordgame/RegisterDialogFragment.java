package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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
    private NetworkManager mNetworkManager;
    private RegisterManager mRegisterManager;

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
        mNetworkManager = new NetworkManager(mActivity);
        mRegisterManager = new RegisterManager(mActivity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(mLayoutId, null);
        mUsername = (EditText) view.findViewById(R.id.twoplayerwordgame_register_dialog_username_edittext);
        final AlertDialog dialog = builder
                .setView(view)
                .setPositiveButton(R.string.twoplayerwordgame_register_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do Nothing
                            }
                        }
                )
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
        if (!mNetworkManager.isNetworkAvailable()) {
            displayToast("Failed: No Internet Available");
            return;
        }

        String username = mUsername.getText().toString();

        if (username.isEmpty() || username.equals(LeaderBoardDialogFragment.LEADERBOARD) ||
                username.equals(MatchmakingManager.PRIVATE_QUEUE) ||
                username.equals(MatchmakingManager.PUBLIC_QUEUE) ||
                username.contains("-") || username.contains(" ")) {
            displayToast("This username is not allowed");
            return;
        }

        mFirebaseClient.get(username, new FirebaseClient.ResponseListener() {
            @Override
            public void onSuccess(String value) {
                // Check if username is available
                if (value == null && checkPlayServices()) {
                    new GCMRegisterTask().execute();
                } else {
                    displayToast("User already exists");
                }
            }

            @Override
            public void onFailure(String value) {
                displayToast("Retry: Failed to register");
            }
        });
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
     * Make a network call and register in GCM
     */
    private class GCMRegisterTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mRegId = mGCM.register(GCM_SENDER_ID);
            } catch (IOException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final String username = mUsername.getText().toString();
            mFirebaseClient.put(username, mRegId, new FirebaseClient.ResponseListener() {
                @Override
                public void onSuccess(String value) {
                    displayToast("Successfully Registered");
                    mRegisterManager.register(mRegId, username);
                    mDismissListener.onDismiss();
                    RegisterDialogFragment.this.getDialog().cancel();
                }

                @Override
                public void onFailure(String value) {
                    displayToast("Retry: Failed to register");
                }
            });
        }
    }

    private void setDismissListener(DismissListener dismissListener) {
        this.mDismissListener = dismissListener;
    }
}
