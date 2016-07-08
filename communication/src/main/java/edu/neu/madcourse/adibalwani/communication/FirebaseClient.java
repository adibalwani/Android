package edu.neu.madcourse.adibalwani.communication;


import android.content.Context;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

public class FirebaseClient {

    private static final String FIREBASE_DB = "https://fiery-fire-5077.firebaseio.com/";
    private static final String LOG_TAG = FirebaseClient.class.getSimpleName();

    public FirebaseClient(Context context) {
        Firebase.setAndroidContext(context);
    }

    public interface ResponseListener {
        /**
         * Method to call on success
         *
         * @param value The returned value
         */
        void onSuccess(String value);
    }

    /**
     * Save the Kay-Value pair in the Firebase DB
     *
     * @param key The key to store
     * @param value The value to store
     */
    public void put(String key, String value) {
        Firebase ref = new Firebase(FIREBASE_DB);
        Firebase usersRef = ref.child(key);
        usersRef.setValue(value, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.d(LOG_TAG, "Data could not be saved. " + firebaseError.getMessage());
                } else {
                    Log.d(LOG_TAG, "Data saved successfully.");
                }
            }
        });
    }

    /**
     * Fetch Value based on the given key
     *
     * @param key The key to search for
     * @param listener ResponseListener to call
     */
    public void get(String key, final ResponseListener listener) {

        Firebase ref = new Firebase(FIREBASE_DB + key);
        Query queryRef = ref.orderByKey();
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Object value = snapshot.getValue();
                listener.onSuccess(value == null ? null : value.toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG, firebaseError.getMessage());
                Log.e(LOG_TAG, firebaseError.getDetails());
            }
        });
    }
}
