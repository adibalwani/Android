package edu.neu.madcourse.adibalwani.twoplayerwordgame;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.widget.Toast;

public class MatchmakingManager {

    public static final String PUBLIC_QUEUE = "PUBLIC-MATCH-QUEUE";
    public static final String PRIVATE_QUEUE = "PRIVATE-MATCH-QUEUE";

    private Activity mActivity;
    private FirebaseClient mFirebaseClient;
    private RegisterManager mRegisterManager;
    private NetworkManager mNetworkManager;
    private GamePlayManager mGamePlayManager;

    public MatchmakingManager(Activity activity) {
        mActivity = activity;
        mRegisterManager = new RegisterManager(activity);
        mFirebaseClient = new FirebaseClient(activity);
        mNetworkManager = new NetworkManager(activity);
        mGamePlayManager = new GamePlayManager(activity);
    }

    /**
     * Find a public match for the registered user
     * If no matches are found, player is placed in Queue and waits for others to join
     * If matches are found, player is joined
     */
    public void findPublicMatch() {
        mFirebaseClient.get(PUBLIC_QUEUE, new FirebaseClient.ResponseListener() {
            @Override
            public void onSuccess(String value) {
                String queue = value;
                if (queue == null) {
                    waitForOtherPlayers(true);
                } else if (isEnqueued(queue, mRegisterManager.getUsername())) {
                    displayWaitDialog(true);
                } else {
                    String[] players = value.split("\\s+");
                    String opponent = players[0];
                    removeUserFromQueue(true, opponent);
                    startNewGame(opponent);
                }
            }

            @Override
            public void onFailure(String value) {
                displayToast("Retry: Failed to contact server");
            }
        });
    }

    /**
     * Find a private match for the registered user
     */
    public void findPrivateMatch() {
        displayPrivateMatchDialog();
    }

    /**
     * Wait for other players to connect in the game
     * Create a new Queue and enqueue user
     *
     * @param publicMatch true iff user is waiting for a player in public match. False, otherwise
     */
    private void waitForOtherPlayers(final boolean publicMatch) {
        String queue = PUBLIC_QUEUE;
        if (!publicMatch) {
            queue = PRIVATE_QUEUE;
        }

        mFirebaseClient.put(queue, mRegisterManager.getUsername(),
                new FirebaseClient.ResponseListener() {
                    @Override
                    public void onSuccess(String value) {
                        displayWaitDialog(publicMatch);
                    }

                    @Override
                    public void onFailure(String value) {
                        displayToast("Retry: Failed to contact server");
                    }
                }
        );
    }

    /**
     * Remove the registered user from the Matchmaking Queue
     *
     * @param publicMatch true iff user is waiting for a player in public match. False, otherwise
     */
    private void removeUserFromQueue(final boolean publicMatch, final String username) {
        String queue = PUBLIC_QUEUE;
        if (!publicMatch) {
            queue = PRIVATE_QUEUE;
        }

        mFirebaseClient.get(queue, new FirebaseClient.ResponseListener() {
            @Override
            public void onSuccess(String value) {
                if (value == null) {
                    return;
                }

                if (!isEnqueued(value, username)) {
                    return;
                }

                String[] players = value.split("\\s+");
                StringBuilder updatedQueue = new StringBuilder();
                for (int i = 0; i < players.length; i++) {
                    if (!players[i].equals(username)) {
                        updatedQueue.append(players[i]);
                        updatedQueue.append(' ');
                    }
                }

                String queue = PUBLIC_QUEUE;
                if (!publicMatch) {
                    queue = PRIVATE_QUEUE;
                }
                String queueValue = null;
                if (updatedQueue.length() != 0) {
                    queueValue = updatedQueue.toString().trim();
                }

                mFirebaseClient.put(queue, queueValue, new FirebaseClient.ResponseListener() {
                    @Override
                    public void onSuccess(String value) {
                        // Do Nothing
                    }

                    @Override
                    public void onFailure(String value) {
                        displayToast("Retry: Failed to contact server");
                    }
                });
            }

            @Override
            public void onFailure(String value) {
                displayToast("Retry: Failed to contact server");
            }
        });
    }

    /**
     * Display wait dialog box containing the layout
     * If the dialog was dismissed, remove the player from wait queue
     *
     * @param publicMatch true iff user is waiting for a player in public match. False, otherwise
     */
    private void displayWaitDialog(final boolean publicMatch) {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                WaitDialogFragment.newInstance(R.layout.twoplayerwordgame_dialog_waiting,
                        new WaitDialogFragment.DismissListener() {
                            @Override
                            public void onDismiss(boolean gameFound) {
                                if (!gameFound) {
                                    removeUserFromQueue(publicMatch, mRegisterManager.getUsername());
                                } else {
                                    continueGame();
                                }
                            }
                        }
                );
        dialogFragment.setCancelable(false);
        dialogFragment.show(fragmentTransaction, "WAIT_DIALOG");
    }

    /**
     * Display private match dialog box containing the layout
     */
    private void displayPrivateMatchDialog() {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                PrivateMatchDialogFragment.newInstance(R.layout.twoplayerwordgame_dialog_private_match,
                        new PrivateMatchDialogFragment.ClickListener() {
                            @Override
                            public void onJoinGameClick() {
                                handleJoinGameClick();
                            }

                            @Override
                            public void onHostGameClick() {
                                handleHostGameClick();
                            }
                        }
                );
        dialogFragment.show(fragmentTransaction, "PRIVATE_MATCH_DIALOG");
    }

    /**
     * Display join game dialog box containing the layout
     */
    private void displayJoinGameDialog() {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                JoinGameDialogFragment.newInstance(R.layout.twoplayerwordgame_dialog_join_game,
                        new JoinGameDialogFragment.DismissListener() {
                            @Override
                            public void onDismiss(String username) {
                                handleJoinGame(username);
                            }
                        }
                );
        dialogFragment.show(fragmentTransaction, "JOIN_GAME_DIALOG");
    }

    /**
     * Handle a private match - Game Join request
     */
    private void handleJoinGameClick() {
        displayJoinGameDialog();
    }

    /**
     * Join the provided player iff the player exists and
     * is in waiting in private MM Queue
     *
     * @param opponent The player to join
     */
    private void handleJoinGame(final String opponent) {
        if (!mNetworkManager.isNetworkAvailable()) {
            displayToast("Failed: No Internet Available");
            return;
        }

        if (opponent.isEmpty()) {
            displayToast("Failed: No username specified");
            return;
        }

        mFirebaseClient.get(PRIVATE_QUEUE, new FirebaseClient.ResponseListener() {
            @Override
            public void onSuccess(String value) {
                if (value == null || !isEnqueued(value, opponent)) {
                    displayToast("No such user is hosting the game");
                } else {
                    removeUserFromQueue(false, opponent);
                    startNewGame(opponent);
                }
            }

            @Override
            public void onFailure(String value) {
                displayToast("Retry: Failed to contact server");
            }
        });
    }

    /**
     * Check whether the provided user in the queue
     *
     * @param queue User queue
     * @param username The user to find
     * @return true iff the user is enqueued. False, otherwise
     */
    private boolean isEnqueued(String queue, String username) {
        String[] players = queue.split("\\s+");
        for (int i = 0; i < players.length; i++) {
            if (players[i].equals(username)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Handle a private match - Game Host request
     * Add the registered user to the Matchmaking Queue and
     * waits for other players to connect in the game
     */
    private void handleHostGameClick() {
        mFirebaseClient.get(PRIVATE_QUEUE, new FirebaseClient.ResponseListener() {
            @Override
            public void onSuccess(String value) {
                if (value == null) {
                    waitForOtherPlayers(false);
                } else if (isEnqueued(value, mRegisterManager.getUsername())) {
                    displayWaitDialog(false);
                } else {
                    StringBuilder updatedQueue = new StringBuilder();
                    updatedQueue.append(value);
                    updatedQueue.append(' ');
                    updatedQueue.append(mRegisterManager.getUsername());

                    mFirebaseClient.put(PRIVATE_QUEUE, updatedQueue.toString(),
                            new FirebaseClient.ResponseListener() {
                                @Override
                                public void onSuccess(String value) {
                                    displayWaitDialog(false);
                                }

                                @Override
                                public void onFailure(String value) {
                                    displayToast("Retry: Failed to contact server");
                                }
                            }
                    );
                }
            }

            @Override
            public void onFailure(String value) {
                displayToast("Retry: Failed to contact server");
            }
        });
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
     * Start a new Word Game - Scraggle
     *
     * @param opponent The opponent to start the game with
     */
    private void startNewGame(String opponent) {
        mRegisterManager.registerOpponent(opponent);
        Intent intent = new Intent(mActivity, GameActivity.class);
        mGamePlayManager.putWait(false);
        mActivity.startActivity(intent);
    }

    /**
     * Continue Word Game - Scraggle
     */
    private void continueGame() {
        Intent intent = new Intent(mActivity, GameActivity.class);
        intent.putExtra(Constants.INTENT_KEY_RESTORE, true);
        mGamePlayManager.putWait(true);
        mActivity.startActivity(intent);
    }
}
