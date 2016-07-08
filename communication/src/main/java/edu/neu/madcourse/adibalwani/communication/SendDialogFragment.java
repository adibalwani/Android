package edu.neu.madcourse.adibalwani.communication;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

public class SendDialogFragment extends DialogFragment {
    private static final String BUNDLE_LAYOUT_ID = "1";
    private static final String GCM_API_KEY = "AIzaSyAz4xw3KQPrRRkU0SHzhb_KHs5F4TNwwBc";
    private static final String BASE_URL = "https://android.googleapis.com/gcm/send";

    private Activity mActivity;
    private int mLayoutId;
    private FirebaseClient mFirebaseClient;

    /**
     * Create new instance of RegisterDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of RegisterDialogFragment
     */
    static SendDialogFragment newInstance(int layoutId) {
        SendDialogFragment dialogFragment = new SendDialogFragment();
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
        mFirebaseClient = new FirebaseClient(mActivity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(mLayoutId, null);
        final EditText username =
                (EditText) view.findViewById(R.id.communication_send_dialog_username_edittext);
        final EditText message =
                (EditText) view.findViewById(R.id.communication_send_dialog_message_edittext);

        final AlertDialog dialog = builder
                .setView(view)
                .setPositiveButton(R.string.communication_send_label, new DialogInterface.OnClickListener() {
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
                        sendMessage(username.getText().toString(), message.getText().toString());
                    }
                });
            }
        });

        return dialog;
    }

    /**
     * Send the given message to the provided user
     *
     * @param username Username to send message to
     * @param message Message to send
     */
    private void sendMessage(final String username, final String message) {
        if (!isNetworkAvailable()) {
            displayToast("Failed: No Internet Available");
            return;
        }

        if (username.isEmpty()) {
            displayToast("Failed: No username specified");
            return;
        }

        mFirebaseClient.get(username, new FirebaseClient.ResponseListener() {
            @Override
            public void onSuccess(String value) {
                if (value == null) {
                    displayToast("No such user exists");
                } else {
                    new SendMessageTask().execute(value, message);
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
     * Make a network call and send message via GCM
     */
    private class SendMessageTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            String receiverRegId = params[0];
            String message = params[1];

            URL url;
            try {
                url = new URL(BASE_URL);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + BASE_URL);
            }

            StringBuilder builder = new StringBuilder();
            builder.append("data.message")
                    .append('=')
                    .append(message)
                    .append('&')
                    .append("registration_id=")
                    .append(receiverRegId);
            byte[] body = builder.toString().getBytes();
            HttpsURLConnection conn = null;

            try {
                conn = (HttpsURLConnection) url.openConnection();
                conn.setSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
                conn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                });
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "key=" + GCM_API_KEY);
                conn.setRequestMethod("POST");

                // post the request
                OutputStream out = conn.getOutputStream();
                out.write(body);
                out.close();

                // handle the response
                return conn.getResponseCode();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer status) {
            if (status != 200) {
                displayToast("Message failed... status: " + status);
            } else {
                displayToast("Message Sent");
            }
            SendDialogFragment.this.dismiss();
        }
    }
}
