package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainFragment extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = MainFragment.class.getSimpleName();

    private Activity mActivity;
    private RegisterManager mRegisterManager;
    private NetworkManager mNetworkManager;
    private MatchmakingManager mMatchmakingManager;
    private FirebaseClient mFirebaseClient;
    private GamePlayManager mGamePlayManager;
    private GoogleCloudMessaging mGCM;

    private Button mProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.twoplayerwordgame_fragment_main, container, false);
        initButtonListener(rootView);
        initViews(rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        initInstances();
        notifyRegisterChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mRegisterManager.isRegistered()) {
            displayRegisterDialog();
        }
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();

        if (resourceId == R.id.twoplayerwordgame_continue_button) {
            continueGame();
        } else if (resourceId == R.id.twoplayerwordgame_public_match_button) {
            handlePublicMatchClick();
        } else if (resourceId == R.id.twoplayerwordgame_private_match_button) {
            handlePrivateMatchClick();
        } else if (resourceId == R.id.twoplayerwordgame_leaderboard_button) {
            handleLeaderboardClick();
        } else if (resourceId == R.id.twoplayerwordgame_profile_button) {
            handleProfileClick();
        } else if (resourceId == R.id.twoplayerwordgame_music_button) {
            toggleMusic();
        } else if (resourceId == R.id.twoplayerwordgame_acknowledgements_button) {
            displayAcknowledgementDialog();
        } else if (resourceId == R.id.twoplayerwordgame_instructions_button) {
            displayInstructionsDialog();
        } else if (resourceId == R.id.twoplayerwordgame_quit_button) {
            endActivity();
        }
    }

    // Initialization Methods:

    /**
     * Initialize the instance variables
     */
    private void initInstances() {
        mActivity = getActivity();
        mRegisterManager = new RegisterManager(mActivity);
        mGCM = GoogleCloudMessaging.getInstance(mActivity);
        mFirebaseClient = new FirebaseClient(mActivity);
        mNetworkManager = new NetworkManager(mActivity);
        mMatchmakingManager = new MatchmakingManager(mActivity);
        mGamePlayManager = new GamePlayManager(mActivity);
    }

    /**
     * Initialize the views
     *
     * @param rootView Fragment View
     */
    private void initViews(View rootView) {
        mProfile = (Button) rootView.findViewById(R.id.twoplayerwordgame_profile_button);
    }

    /**
     * Initialize onClickListener for buttons
     *
     * @param view Fragment view
     */
    private void initButtonListener(View view) {
        view.findViewById(R.id.twoplayerwordgame_continue_button).setOnClickListener(this);
        view.findViewById(R.id.twoplayerwordgame_public_match_button).setOnClickListener(this);
        view.findViewById(R.id.twoplayerwordgame_private_match_button).setOnClickListener(this);
        view.findViewById(R.id.twoplayerwordgame_leaderboard_button).setOnClickListener(this);
        view.findViewById(R.id.twoplayerwordgame_profile_button).setOnClickListener(this);
        view.findViewById(R.id.twoplayerwordgame_music_button).setOnClickListener(this);
        view.findViewById(R.id.twoplayerwordgame_acknowledgements_button).setOnClickListener(this);
        view.findViewById(R.id.twoplayerwordgame_instructions_button).setOnClickListener(this);
        view.findViewById(R.id.twoplayerwordgame_quit_button).setOnClickListener(this);
    }

    // Handling Button Clicks Methods:

    /**
     * Handle Profile button click
     */
    private void handleProfileClick() {
        if (mRegisterManager.isRegistered()) {
            if (!mNetworkManager.isNetworkAvailable()) {
                displayToast("Failed: No Internet Available");
                return;
            }
            new GCMUnregisterTask().execute();
        } else {
            displayRegisterDialog();
        }
    }

    /**
     * Handle Public Match click
     */
    private void handlePublicMatchClick() {
        // Check if user is registered
        if (!mRegisterManager.isRegistered()) {
            displayRegisterDialog();
            return;
        }

        // Check if network is available
        if (!mNetworkManager.isNetworkAvailable()) {
            displayToast("Failed: No Internet Available");
            return;
        }

        // Check if user has an active game
        if (mGamePlayManager.getGameData() != null) {
            displaySessionQuitDialog();
            return;
        }

        mMatchmakingManager.findPublicMatch();
    }

    /**
     * Handle Private Match click
     */
    private void handlePrivateMatchClick() {
        // Check if user is registered
        if (!mRegisterManager.isRegistered()) {
            displayRegisterDialog();
            return;
        }

        // Check if network is available
        if (!mNetworkManager.isNetworkAvailable()) {
            displayToast("Failed: No Internet Available");
            return;
        }

        // Check if user has an active game
        if (mGamePlayManager.getGameData() != null) {
            displaySessionQuitDialog();
            return;
        }

        mMatchmakingManager.findPrivateMatch();
    }

    /**
     * Handle Leaderboard Click
     */
    private void handleLeaderboardClick() {
        // Check if network is available
        if (!mNetworkManager.isNetworkAvailable()) {
            displayToast("Failed: No Internet Available");
            return;
        }

        displayLeaderboardDialog();
    }

    // UI Update Methods:

    /**
     * Display registeration dialog box containing the layout
     */
    private void displayRegisterDialog() {
        mRegisterManager.displayRegisterDialog(R.layout.twoplayerwordgame_dialog_register,
                new RegisterDialogFragment.DismissListener() {
                    @Override
                    public void onDismiss() {
                        notifyRegisterChanged();
                    }
                }
        );
    }

    /**
     * Display leaderboard dialog box containing the layout
     */
    private void displayLeaderboardDialog() {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                LeaderBoardDialogFragment.newInstance(R.layout.twoplayerwordgame_dialog_leaderboard);
        dialogFragment.show(fragmentTransaction, "LEADERBOARD_DIALOG");
    }

    /**
     * Display acknowledgement dialog box containing the layout
     */
    private void displayAcknowledgementDialog() {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                AcknowledgementDialogFragment.newInstance(R.layout.twoplayerwordgame_dialog_acknowledgement);
        dialogFragment.show(fragmentTransaction, "ACKNOWLEDGEMENT_DIALOG");
    }

    /**
     * Display leaderboard dialog box containing the layout
     */
    private void displaySessionQuitDialog() {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                SessionQuitDialogFragment.newInstance(R.layout.twoplayerwordgame_dialog_session_quit,
                        new SessionQuitDialogFragment.DismissListener() {
                            @Override
                            public void onPositiveDismiss() {
                                new GCMManager(mActivity).sendMessage("null", false, 0);
                                mGamePlayManager.removeGamePlay();
                                notifyGameEndChanged();
                            }

                            @Override
                            public void onNegativeDismiss() {
                                // Do Nothing
                            }
                        });
        dialogFragment.show(fragmentTransaction, "SESSION_QUIT_DIALOG");
    }

    /**
     * Display instructions dialog box containing the layout
     */
    private void displayInstructionsDialog() {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                InstructionsDialogFragment.newInstance(R.layout.twoplayerwordgame_dialog_instruction);
        dialogFragment.show(fragmentTransaction, "INSRUCTIONS_DIALOG");
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
     * Notify change in registeration state
     */
    private void notifyRegisterChanged() {
        if (mRegisterManager.isRegistered()) {
            mProfile.setText(getResources().getString(R.string.twoplayerwordgame_delete_profile_label));
        } else {
            mProfile.setText(getResources().getString(R.string.twoplayerwordgame_create_profile_label));
            mActivity.findViewById(R.id.twoplayerwordgame_continue_button).setVisibility(View.GONE);
        }
    }

    /**
     * Notify change in game end state
     */
    private void notifyGameEndChanged() {
        if (mGamePlayManager.getGameData() == null) {
            mActivity.findViewById(R.id.twoplayerwordgame_continue_button).setVisibility(View.GONE);
        } else {
            mActivity.findViewById(R.id.twoplayerwordgame_continue_button).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handle Quit by killing the current Activity
     */
    private void endActivity() {
        mActivity.finish();
    }

    /**
     * Turn ON/OFF background music
     */
    private void toggleMusic() {
        boolean music = mGamePlayManager.isMusicOn();
        if (music) {
            ((MainActivity) mActivity).stopMusic();
        } else {
            ((MainActivity) mActivity).startMusic();
        }
    }

    /**
     * Continue Word Game - Scraggle
     */
    private void continueGame() {
        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(
                Context.NOTIFICATION_SERVICE
        );
        notificationManager.cancel(GCMIntentService.NOTIFICATION_ID);
        Intent intent = new Intent(mActivity, GameActivity.class);
        intent.putExtra(Constants.INTENT_KEY_RESTORE, true);
        mActivity.startActivity(intent);
    }

    /**
     * Task to unregister user
     */
    private class GCMUnregisterTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mGCM.unregister();
            } catch (IOException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mFirebaseClient.put(mRegisterManager.getUsername(), null,
                    new FirebaseClient.ResponseListener() {
                        @Override
                        public void onSuccess(String value) {
                            // Do Nothing
                        }

                        @Override
                        public void onFailure(String value) {
                            // Do Nothing
                        }
                    }
            );
            mRegisterManager.unregister();
            mGamePlayManager.removeGamePlay();
            if (mGamePlayManager.getGameData() != null) {
                new GCMManager(mActivity).sendMessage("null", false, 0);
            }
            notifyRegisterChanged();
        }
    }
}
