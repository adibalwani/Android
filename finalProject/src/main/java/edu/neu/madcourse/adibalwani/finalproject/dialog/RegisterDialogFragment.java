package edu.neu.madcourse.adibalwani.finalproject.dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import edu.neu.madcourse.adibalwani.finalproject.Constants;
import edu.neu.madcourse.adibalwani.finalproject.R;
import edu.neu.madcourse.adibalwani.finalproject.leaderboard.User;
import edu.neu.madcourse.adibalwani.finalproject.network.FirebaseClient;
import edu.neu.madcourse.adibalwani.finalproject.network.NetworkManager;
import edu.neu.madcourse.adibalwani.finalproject.register.RegisterManager;

public class RegisterDialogFragment extends DialogFragment implements View.OnClickListener {

    public interface DismissListener {
        /**
         * Method to call on dismiss
         */
        void onDismiss();
    }

    private static final String BUNDLE_LAYOUT_ID = "1";
    private static final String LOG_TAG = RegisterDialogFragment.class.getSimpleName();

    private Activity mActivity;
    private int mLayoutId;
    private EditText mUsername;
    private TextView mErrorMessage;
    private TextView mErrorRetry;
    private FloatingActionButton mRegister;
    private FirebaseClient mFirebaseClient;
    private RegisterManager mRegisterManager;
    private DismissListener mDismissListener;

    /**
     * Create new instance of RegisterDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of RegisterDialogFragment
     */
    public static RegisterDialogFragment newInstance(int layoutId, DismissListener listener) {
        RegisterDialogFragment dialogFragment = new RegisterDialogFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_LAYOUT_ID, layoutId);
        dialogFragment.setArguments(args);
        dialogFragment.mDismissListener = listener;
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mLayoutId = getArguments().getInt(BUNDLE_LAYOUT_ID);
        mFirebaseClient = new FirebaseClient(mActivity);
        mRegisterManager = new RegisterManager(mActivity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(mLayoutId, null);
        mUsername = (EditText) view.findViewById(R.id.finalproject_dialog_register_username);
        mErrorMessage = (TextView) view.findViewById(R.id.finalproject_dialog_register_error);
        mErrorRetry = (TextView) view.findViewById(R.id.finalproject_dialog_register_error_retry);
        mRegister = (FloatingActionButton) view.findViewById(R.id.finalproject_dialog_register_ok);
        mRegister.setOnClickListener(this);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.finalproject_dialog_register_ok) {
            register();
        }
    }

    @Override
    public void dismiss() {
        mDismissListener.onDismiss();
        super.dismiss();
    }

    /**
     * Register the username in Cloud Key-Value Store
     */
    private void register() {
        mErrorRetry.setText("");
        mErrorMessage.setText("");

        if (!NetworkManager.isNetworkAvailable(mActivity)) {
            mErrorMessage.setText(Constants.INTERNET_UNAVAILABLE);
            mErrorRetry.setText(Constants.RETRY_INTERNET_UNAVAILABLE);
            return;
        }

        final String username = mUsername.getText().toString();

        if (username.isEmpty()) {
            mErrorMessage.setText(Constants.USERNAME_INVALID);
            return;
        }

        mFirebaseClient.get(username, new FirebaseClient.ResponseListener() {
            @Override
            public void onSuccess(User user) {
                // Check if username is available
                if (user == null) {
                    User newUser = new User(username, 0);
                    mFirebaseClient.put(newUser, new FirebaseClient.ResponseListener() {
                        @Override
                        public void onSuccess(User user) {
                            mRegisterManager.register(username);
                            mRegisterManager.displayRegisteredToast();
                            RegisterDialogFragment.this.dismiss();
                        }

                        @Override
                        public void onFailure(String value) {
                            mErrorMessage.setText(Constants.SERVER_FAILED);
                            mErrorRetry.setText(Constants.RETRY_SERVER_FAILED);
                            Log.e(LOG_TAG, Constants.SERVER_FAILED + " because of " + value);
                        }
                    });
                } else {
                    mErrorMessage.setText(Constants.USERNAME_EXISTS);
                    mErrorRetry.setText(Constants.RETRY_USERNAME_EXISTS);
                }
            }

            @Override
            public void onFailure(String value) {
                mErrorMessage.setText(Constants.SERVER_FAILED);
                mErrorRetry.setText(Constants.RETRY_SERVER_FAILED);
                Log.e(LOG_TAG, Constants.SERVER_FAILED + " because of " + value);
            }
        });
    }
}
