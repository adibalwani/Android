package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkManager {

    private Context mContext;

    public NetworkManager(Context context) {
        mContext = context;
    }

    /**
     * Check whether an Internet Connection is available or not
     *
     * @return true iff internet is available. False, otherwise
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
