package edu.neu.madcourse.adibalwani.finalproject.network;


import android.content.Context;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import edu.neu.madcourse.adibalwani.finalproject.Constants;
import edu.neu.madcourse.adibalwani.finalproject.leaderboard.User;

/**
 * Firebase Client to handle I/O with App Database
 */
public class FirebaseClient {

    public FirebaseClient(Context context) {
        Firebase.setAndroidContext(context);
    }

    public interface ResponseListener {
        /**
         * Method to call on success
         *
         * @param user The returned value
         */
        void onSuccess(User user);

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
     * @param user User Object
     */
    public void put(User user, final ResponseListener listener) {
        Firebase ref = new Firebase(Constants.FIREBASE_DB).child(Constants.USERS);
        ref.child(user.getName()).setValue(user, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    listener.onFailure(firebaseError.getMessage());
                } else {
                    listener.onSuccess(null);
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
        Firebase ref = new Firebase(Constants.FIREBASE_DB).child(Constants.USERS);
        ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                listener.onSuccess(user == null ? null : user);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                listener.onFailure(firebaseError.getMessage());
            }
        });
    }
}
