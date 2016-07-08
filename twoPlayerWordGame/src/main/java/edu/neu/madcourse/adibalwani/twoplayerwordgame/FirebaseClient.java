package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.content.Context;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

public class FirebaseClient {

    private static final String FIREBASE_DB = "https://fiery-fire-5077.firebaseio.com/";

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

        /**
         * Method to call on success
         *
         * @param value The returned value
         */
        void onFailure(String value);
    }

    /**
     * Save the Kay-Value pair in the Firebase DB
     *
     * @param key The key to store
     * @param value The value to store
     */
    public void put(String key, String value, final ResponseListener listener) {
        Firebase ref = new Firebase(FIREBASE_DB);
        Firebase usersRef = ref.child(key);
        usersRef.setValue(value, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    listener.onFailure(firebaseError.getMessage());
                } else {
                    listener.onSuccess("");
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
                listener.onFailure(firebaseError.getMessage());
            }
        });
    }
}
