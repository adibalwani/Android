package edu.neu.madcourse.adibalwani.finalproject.network;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import edu.neu.madcourse.adibalwani.finalproject.Constants;

/**
 * Class used to manage network connections
 */
public class NetworkManager {

    /**
     * Check whether an Internet Connection is available or not
     *
     * @param context Current Context
     * @return true iff internet is available. False, otherwise
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Display a toast message - "No Internet Connection" for 1 second
     *
     * @param context Current Context
     */
    public static void displayNetworkUnavailableToast(Context context) {
        Toast.makeText(context, Constants.INTERNET_UNAVAILABLE, Toast.LENGTH_LONG).show();
    }

}
