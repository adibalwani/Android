package edu.neu.madcourse.adibalwani.finalproject.leaderboard;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedList;
import java.util.List;

import edu.neu.madcourse.adibalwani.finalproject.Constants;
import edu.neu.madcourse.adibalwani.finalproject.network.FirebaseClient;
import edu.neu.madcourse.adibalwani.finalproject.register.RegisterManager;

/**
 * Class used to manage leaderboards in Shared Preference
 */
public class LeaderBoardManager {

    private final static String LOG_TAG = LeaderBoardManager.class.getSimpleName();
    private final static int HISTORY_LENGTH = 5;

    private Context mContext;
    private List<Integer> mHistory;
    private FirebaseClient mFirebaseClient;
    private RegisterManager mRegisterManager;

    public LeaderBoardManager(Context context) {
        mContext = context;
        mFirebaseClient = new FirebaseClient(context);
        mRegisterManager = new RegisterManager(context);
        notifyHistoryChanged();
    }

    /**
     * Check if the user has score history available
     *
     * @return true iff the user has score history. False, otherwise
     */
    public boolean doesHistoryExist() {
        return mHistory != null;
    }

    /**
     * Store the score in Shared Preference (if amongst the HISTORY LENGTH)
     */
    public void storeScore(int score) {
        if (!doesHistoryExist()) {
            mHistory = new LinkedList<Integer>();
            mHistory.add(score);
        } else {
            // Update Score List if amongst top
            int listSize =  mHistory.size();
            for (int i = 0; i < listSize; i++) {
                if (score > mHistory.get(i)) {
                    mHistory.add(i, score);
                }
            }
            if (mHistory.size() == listSize) {
                mHistory.add(score);
            }
            if (listSize + 1 > HISTORY_LENGTH) {
                mHistory.remove(HISTORY_LENGTH);
            }
        }

        SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PROPERTY_SCORE_HISTORY, new Gson().toJson(mHistory));
        editor.commit();
    }

    /**
     * Get the history list
     *
     * @return History list if found. Null otherwise
     */
    public List<Integer> getHistory() {
        return mHistory;
    }

    /**
     * Sync the top score of the user (if exists) with Firebase Key-Value store
     */
    public void syncWithFirebase() {
        if (!doesHistoryExist()) {
            return;
        }
        String username = mRegisterManager.getUsername();

        if (username != null) {
            mFirebaseClient.get(username, new FirebaseClient.ResponseListener() {
                @Override
                public void onSuccess(User user) {
                    int highScore = mHistory.get(0);
                    if (user.getScore() != highScore) {
                        user.setScore(highScore);
                        mFirebaseClient.put(user, new FirebaseClient.ResponseListener() {
                            @Override
                            public void onSuccess(User user) {
                                Log.i(LOG_TAG, "Top score updated for " + user);
                            }

                            @Override
                            public void onFailure(String value) {
                                Log.e(LOG_TAG, Constants.SERVER_FAILED + " because of " + value);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(String value) {
                    Log.e(LOG_TAG, Constants.SERVER_FAILED + " because of " + value);
                }
            });
        }
    }

    /**
     * Notify change in state
     */
    private void notifyHistoryChanged() {
        mHistory = fetchHistory();
    }

    /**
     * Get the history scores from Shared Preference
     *
     * @return Scores if found. Null otherwise
     */
    private List<Integer> fetchHistory() {
        SharedPreferences prefs = getSharedPreferences();
        String historyList = prefs.getString(Constants.PROPERTY_SCORE_HISTORY, null);
        if (historyList == null) {
            return null;
        }

        return new Gson().fromJson(historyList, new TypeToken<List<Integer>>(){}.getType());
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
