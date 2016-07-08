package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.os.AsyncTask;
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

public class GCMManager {

    private static final String GCM_API_KEY = "AIzaSyAz4xw3KQPrRRkU0SHzhb_KHs5F4TNwwBc";
    private static final String BASE_URL = "https://android.googleapis.com/gcm/send";

    private Activity mActivity;
    private RegisterManager mRegisterManager;
    private FirebaseClient mFirebaseClient;
    private String mOpponent;
    private String mOpponentGCMId;
    private NetworkManager mNetworkManager;

    public GCMManager(Activity activity) {
        mActivity = activity;
        mRegisterManager = new RegisterManager(activity);
        mFirebaseClient = new FirebaseClient(activity);
        mNetworkManager = new NetworkManager(activity);
        mOpponent = mRegisterManager.getOpponent();
    }

    /**
     * Send the given message to the opponent
     *
     * @param message Message to send
     * @param wait Is it the waiting turn or not
     * @param score The game score
     */
    public void sendMessage(final String message, final boolean wait, final int score) {
        if (!mNetworkManager.isNetworkAvailable()) {
            displayToast("Failed: No Internet Available");
            return;
        }

        if (mOpponentGCMId == null) {
            mFirebaseClient.get(mOpponent, new FirebaseClient.ResponseListener() {
                @Override
                public void onSuccess(String value) {
                    if (value == null) {
                        displayToast("Your partner has deleted his profile");
                    } else {
                        mOpponentGCMId = value;
                        new SendMessageTask().execute(mOpponentGCMId, message, wait + "", score + "");
                    }
                }

                @Override
                public void onFailure(String value) {
                    displayToast("Retry: Unable to contact server");
                }
            });
        } else {
            new SendMessageTask().execute(mOpponentGCMId, message, wait + "", score + "");
        }
    }

    /**
     * Make a network call and send message via GCM
     */
    private class SendMessageTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            String receiverRegId = params[0];
            String message = params[1];
            String wait = params[2];
            String score = params[3];

            URL url;
            try {
                url = new URL(BASE_URL);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + BASE_URL);
            }

            StringBuilder builder = new StringBuilder();
            builder.append("data.gamestate")
                    .append('=')
                    .append(message)
                    .append('&')
                    .append("data.opponent")
                    .append('=')
                    .append(mRegisterManager.getUsername())
                    .append('&')
                    .append("data.wait")
                    .append('=')
                    .append(wait)
                    .append('&')
                    .append("data.score")
                    .append('=')
                    .append(score)
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
                //displayToast("Message Sent");
            }
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
}
