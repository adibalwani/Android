package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class GameFragment extends Fragment {

    static final private int mLargeIds[] = {R.id.large1, R.id.large2, R.id.large3,
            R.id.large4, R.id.large5, R.id.large6, R.id.large7, R.id.large8,
            R.id.large9};
    static final private int mSmallIds[] = {R.id.small1, R.id.small2, R.id.small3,
            R.id.small4, R.id.small5, R.id.small6, R.id.small7, R.id.small8,
            R.id.small9};
    static final public int PHASE_1 = 1;
    static final public int PHASE_2 = 2;
    static final public int PHASE_3 = 3;
    static final private int PHASE_TIME = 91000;

    private Tile mLargeTiles[] = new Tile[9];
    private Tile mSmallTiles[][] = new Tile[9][9];
    private List<String[]> mDictionary;
    private Loader mLoader;
    private Random mRandomGenerator;
    private Singleton mSingletonInstance;
    private Vibrator mVibrator;
    private Activity mActivity;
    private ToneGenerator mToneGenerator;
    private CountDownTimer mTimer;
    private GCMManager mGcmManager;
    private GamePlayManager mGamePlayManager;
    private BroadcastReceiver mBroadcastReceiver;

    private ProgressBar mProgressBar;
    private int mProgressStatus;
    private GridLayout mLargeBoard;
    private TextView mTimeView;
    private TextView mWordFormed;
    private TextView mScoreView;
    private Button mPauseButton;
    private int mTime;
    private int mPhase;
    private int mScore;

    private List<Character> mSelection;
    private ArrayAdapter<String> mWordListAdapter;
    private List<String> mWordListData;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.twoplayerwordgame_fragment_game, container, false);
        mLargeBoard = (GridLayout) rootView.findViewById(R.id.twoplayerwordgame_large_board);
        initViews(rootView);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        initBoard();
        mSingletonInstance = Singleton.getInstance();
        mTime = PHASE_TIME;
        mPhase = PHASE_1;
        mScore = 0;
        mSelection = new ArrayList<Character>();
    }

    @Override
    public void onStart() {
        super.onStart();
        initInstance();
        initListViewAdapter();
        List<String[]> dictionary = mSingletonInstance.getDictionary();

        if (dictionary == null) {
            loadDictionary();
        } else {
            mDictionary = dictionary;
            showGame();
            if (mTime == PHASE_TIME && mPhase == PHASE_1) {
                initTiles();
            }
            initTimer();
            ((GameActivity) mActivity).restoreState();
            if (mTime == PHASE_TIME && mPhase == PHASE_1 && mGamePlayManager.hasGameTurn()) {
                mGcmManager.sendMessage(saveState(), true, mScore);
            }
            if (!mGamePlayManager.hasGameTurn()) {
                displayWaitForTurnDialog();
            }
        }

        registerBroadcastReceiver();
        registerSensorEvent();
    }

    @Override
    public void onPause() {
        super.onPause();

        mActivity.unregisterReceiver(mBroadcastReceiver);
        unregisterSensorEvent();

        // Save Instance state
        if (mDictionary != null) {
            mSingletonInstance.setDictionary(mDictionary);
            mTimer.cancel();
        }

        // Cancel all spawned threads
        if (mLoader != null && mLoader.getStatus() != AsyncTask.Status.FINISHED) {
            mLoader.cancel(true);
        }
    }

    /**
     * Initialize the View Adapter and data
     */
    private void initListViewAdapter() {
        ListView wordListView = (ListView) mActivity.findViewById(R.id.twoplayerwordgame_word_list_view);
        mWordListData = new ArrayList<String>();
        mWordListAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                mWordListData);
        wordListView.setAdapter(mWordListAdapter);
    }

    /**
     * Initialize all the instance variables
     */
    private void initInstance() {
        mActivity = getActivity();
        mRandomGenerator = new Random();
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        mVibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        mProgressBar = (ProgressBar) mActivity.findViewById(R.id.twoplayerwordgame_progress_bar);
        mTimeView = (TextView) mActivity.findViewById(R.id.twoplayerwordgame_timer);
        mScoreView = (TextView) mActivity.findViewById(R.id.twoplayerwordgame_score);
        mWordFormed = (TextView) mActivity.findViewById(R.id.twoplayerwordgame_word_formed);
        mWordFormed.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleWordFormedClick();
                return false;
            }
        });
        mPauseButton = (Button) mActivity.findViewById(R.id.twoplayerwordgame_pause_button);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePauseClick();
            }
        });
        mGcmManager = new GCMManager(mActivity);
        mGamePlayManager = new GamePlayManager(mActivity);
    }

    /**
     * Register the sensor manager to listen to shake events
     */
    private void registerSensorEvent() {
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                if (count >= 2 && mGamePlayManager.hasGameTurn()) {
                    switchTurn();
                }
            }
        });
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Unregister the sensor manager to listen to shake events
     */
    private void unregisterSensorEvent() {
        mSensorManager.unregisterListener(mShakeDetector);
    }

    /**
     * Handle Word Formed Click on phase 2
     */
    private void handleWordFormedClick() {
        if (mPhase != PHASE_2 || mSelection.isEmpty()) {
            return;
        }

        penalizePlayer();
        mSelection.clear();
        notifyWordChanged();
        resetGrid();
        notifyAllStateChanged();
        switchTurn();
    }

    /**
     * Handle Pause button click
     */
    private void handlePauseClick() {
        if (mLargeBoard.getVisibility() == View.VISIBLE) {
            pauseGame();
        } else {
            resumeGame();
        }
    }

    /**
     * Pause the game
     */
    private void pauseGame() {
        mLargeBoard.setVisibility(View.INVISIBLE);
        if (mTimer != null) {
            mTimer.cancel();
        }
        mPauseButton.setText(getResources().getString(R.string.twoplayerwordgame_continue_label));
        notifyTimeChanged();
    }

    /**
     * Resume the game
     */
    private void resumeGame() {
        mLargeBoard.setVisibility(View.VISIBLE);
        initTimer();
        mPauseButton.setText(getResources().getString(R.string.twoplayerwordgame_pause_label));
    }

    /**
     * Initialize the game timer
     */
    private void initTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new CountDownTimer(mTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                decrementTime(1);
                notifyTimeChanged();
                if (millisUntilFinished <= 5000 && millisUntilFinished > 1500) {
                    mToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                }
            }

            @Override
            public void onFinish() {
                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);
                handlePhaseTransition();
            }
        };
        mTimer.start();
    }

    /**
     * Convert the current time to mm:ss format
     *
     * @return The current time
     */
    private String getTime() {
        StringBuilder builder = new StringBuilder();
        int time = mTime / 1000;
        builder.append(time / 60);
        builder.append(':');
        int sec = time % 60;
        builder.append(sec < 10 ? "0" + sec : sec);
        return builder.toString();
    }

    /**
     * Decrement the time by given sec
     *
     * @param sec Number of seconds to decrease
     */
    private void decrementTime(int sec) {
        mTime = mTime - (sec * 1000);
    }

    /**
     * Update the UI - time
     */
    private void notifyTimeChanged() {
        mTimeView.setText(getTime());
    }

    /**
     * Update the UI - Word formed
     */
    private void notifyWordChanged() {
        String word = getWord();
        if (word.length() == 0) {
            mWordFormed.setVisibility(View.GONE);
        } else {
            mWordFormed.setVisibility(View.VISIBLE);
            mWordFormed.setText(word);
        }
    }

    /**
     * Display wait dialog box containing the layout
     * Pause the current game and save it's state
     */
    private void displayWaitForTurnDialog() {
        pauseGame();
        String gameData = saveState();
        mGamePlayManager.putGamePlay(gameData, true);
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                WaitForTurnDialogFragment.newInstance(R.layout.twoplayerwordgame_dialog_waiting_for_turn,
                        new WaitForTurnDialogFragment.DismissListener() {
                            @Override
                            public void onDismiss(boolean hasGameTurn) {
                                if (hasGameTurn) {
                                    mSelection.clear();
                                    notifyWordChanged();
                                    String gameData = mGamePlayManager.getGameData();
                                    restoreState(gameData);
                                    resumeGame();
                                }
                            }

                            @Override
                            public void onGameEnd(int score) {
                                mPhase = PHASE_3;
                                displayEndGameDialog(score);
                            }
                        }
                );
        dialogFragment.setCancelable(false);
        dialogFragment.show(fragmentTransaction, "WAIT_FOR_TURN_DIALOG");
    }

    /**
     * Display End game dialog box containing the layout and the given score
     *
     * @param score Game score
     */
    private void displayEndGameDialog(int score) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                EndGameDialogFragment.newInstance(R.layout.twoplayerwordgame_dialog_end, score);
        dialogFragment.setCancelable(false);
        dialogFragment.show(fragmentTransaction, "END_DIALOG");
    }

    /**
     * Register a broadcast receiver to handle player found
     */
    private void registerBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        filter.addCategory(mActivity.getPackageName());
        filter.setPriority(1);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (!extras.isEmpty()) {
                    String gameData = extras.getString("gamestate");
                    String wait = extras.getString("wait");
                    if (gameData != null && wait != null) {
                        if (gameData.equals("null")) {
                            mPhase = PHASE_3;
                            String score = extras.getString("score");
                            displayEndGameDialog(Integer.parseInt(score));
                        }
                        // GCM is indeed sending two broadcasts: Wierd
                        abortBroadcast();
                    }
                }
            }
        };
        mActivity.registerReceiver(
                mBroadcastReceiver,
                filter,
                "com.google.android.c2dm.permission.SEND",
                null
        );
    }

    /**
     * Update the UI - Score
     */
    private void notifyScoreChanged() {
        String score = String.valueOf(mScore);
        if (mScore < 10 && mScore >= 0) {
            score = "0" + score;
        } else if (mScore < 0) {
            score = "-0" + mScore * -1;
        }
        mScoreView.setText(score);
    }

    /**
     * Initialize the board tiles with random words of 9 letters
     */
    private void initTiles() {
        String[] words = mDictionary.get(0);
        for (int large = 0; large < 9; large++) {
            String word = words[generateNumber(0, words.length)];
            char[] wordArray = word.toCharArray();
            wordArray = shuffleWord(wordArray);
            for (int small = 0; small < 9; small++) {
                String text = wordArray[small] + "";
                setTileText(large, small, text);
            }
        }
    }

    /**
     * Load dictionary into memory
     */
    private void loadDictionary() {
        mLoader = new Loader();
        mLoader.execute();
    }

    /**
     * Vibrate the phone for 100ms
     */
    private void vibrate() {
        mVibrator.vibrate(100);
    }

    /**
     * Check whether the given exists in dictionary
     *
     * @param word The word to search for
     * @return true, iff it exists. False, otherwise
     */
    private boolean inDictionary(String word) {
        if (mDictionary == null) {
            return false;
        }

        for (int i = 0; i < mDictionary.size(); i++) {
            if (Arrays.binarySearch(mDictionary.get(i), word) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update the game score
     */
    private void updateScore() {
        if (mSelection.size() >= 9) {
            mScore += 10;
        }

        for (int i = 0; i < mSelection.size(); i++) {
            char ch = mSelection.get(i);
            switch (ch) {
                case 'e': case 'a': case 'i': case 'o': case 'n':
                case 'r': case 't': case 'l': case 's':
                    mScore += 1;
                    break;
                case 'd': case 'g':
                    mScore += 2;
                    break;
                case 'b': case 'c': case 'm': case 'p':
                    mScore += 3;
                    break;
                case 'f': case 'h': case 'v': case 'w': case 'y':
                    mScore += 4;
                    break;
                case 'k':
                    mScore += 5;
                    break;
                case 'j': case 'x':
                    mScore += 8;
                    break;
                case 'q': case 'z':
                    mScore += 10;
                    break;
                default:
                    break;
            }
        }
        notifyScoreChanged();
    }

    /**
     * Initialize the board views
     *
     * @param rootView Fragment View
     */
    private void initViews(View rootView) {
        for (int large = 0; large < 9; large++) {
            View outer = rootView.findViewById(mLargeIds[large]);
            mLargeTiles[large].setView(outer);

            for (int small = 0; small < 9; small++) {
                Button inner = (Button) outer.findViewById(mSmallIds[small]);
                final Tile smallTile = mSmallTiles[large][small];
                final int finalLarge = large;
                final int finalSmall = small;
                smallTile.setView(inner);
                smallTile.notifyStateChanged();
                makeGridAvailable();

                inner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        handleClick(finalLarge, finalSmall);
                    }
                });

            }
        }
    }

    /**
     * Handle click event on button
     *
     * @param large grid
     * @param small tile
     */
    private void handleClick(final int large, final int small) {
        switch (mPhase) {
            case PHASE_1:
                handlePhaseOneClick(large, small);
                break;
            case PHASE_2:
                handlePhaseTwoClick(large, small);
                break;
            default:
                break;
        }
    }

    /**
     * Handle click event on button for phase 1
     *
     * @param large grid
     * @param small tile
     */
    private void handlePhaseOneClick(final int large, final int small) {
        Tile largeTile = mLargeTiles[large];
        Tile smallTile = mSmallTiles[large][small];
        if (smallTile.isModifiable()) {
            makeMove(large, small);
        } else if (!largeTile.isLocked() && smallTile.isSelected()) {
            lockGrid(large);
            makeGridAvailable();
            penalizePlayer();
            mSelection.clear();
            notifyWordChanged();
            switchTurn();
        }
    }

    /**
     * Switch the game turn to the opponent
     */
    private void switchTurn() {
        String gameData = saveState();
        mGcmManager.sendMessage(gameData, false, mScore);
        mGamePlayManager.putWait(true);
        displayWaitForTurnDialog();
    }

    /**
     * Penalize player if unable to find new word
     */
    private void penalizePlayer() {
        if (!mWordListData.contains(getWord())) {
            mScore -= 3;
            notifyScoreChanged();
        }
    }

    /**
     * Handle transition from phases
     */
    private void handlePhaseTransition() {
        switch (mPhase) {
            case PHASE_1:
                mPhase = PHASE_2;
                mTime = PHASE_TIME;
                unlockAndHideGrid();
                notifyAllStateChanged();
                initTimer();
                break;
            case PHASE_2:
                mPhase = PHASE_3;
                endGame();
                break;
        }
        mSelection.clear();
        notifyWordChanged();
    }

    /**
     * End the current game
     */
    private void endGame() {
        updateLeaderBoard();
        mGcmManager.sendMessage(saveState(), false, mScore);
        displayEndGameDialog(mScore);
    }

    /**
     * Update the leaderboard
     */
    private void updateLeaderBoard() {
        final FirebaseClient firebaseClient = new FirebaseClient(mActivity);
        firebaseClient.get(LeaderBoardDialogFragment.LEADERBOARD, new FirebaseClient.ResponseListener() {
            @Override
            public void onSuccess(String value) {
                String leaderboard = getUpdatedLeaderboard(value);
                firebaseClient.put(LeaderBoardDialogFragment.LEADERBOARD, leaderboard,
                        new FirebaseClient.ResponseListener() {
                            @Override
                            public void onSuccess(String value) {
                                // Do Nothing
                            }

                            @Override
                            public void onFailure(String value) {
                                displayToast("Failed to save data to leaderboard");
                            }
                        });
            }

            @Override
            public void onFailure(String value) {
                displayToast("Failed to save data to leaderboard");
            }
        });
    }

    /**
     * Get an updated string of LeaderBoard
     *
     * @param leaderboard Current LeaderBoard
     * @return Updated LeaderBoard
     */
    private String getUpdatedLeaderboard(String leaderboard) {
        if (leaderboard == null) {
            leaderboard = "";
        }

        RegisterManager registerManager = new RegisterManager(mActivity);
        String username = registerManager.getUsername();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");
        String dateTime = simpleDateFormat.format(new Date());
        String opponent = registerManager.getOpponent();

        StringBuilder builder = new StringBuilder();
        builder.append(leaderboard);
        builder.append(' ');
        builder.append(username);
        builder.append(':');
        builder.append(dateTime);
        builder.append(':');
        builder.append(mScore);
        builder.append(':');
        builder.append(opponent);
        builder.append(' ');
        builder.append(opponent);
        builder.append(':');
        builder.append(dateTime);
        builder.append(':');
        builder.append(mScore);
        builder.append(':');
        builder.append(username);
        return builder.toString().trim();
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
     * Handle click event on button for phase 1
     *
     * @param large grid
     * @param small tile
     */
    private void handlePhaseTwoClick(final int large, final int small) {
        Tile largeTile = mLargeTiles[large];
        Tile smallTile = mSmallTiles[large][small];

        if (largeTile.isModifiable() && smallTile.isSelected()) {
            makeMove(large, small);
        }
    }

    /**
     * Get the word from the user selection
     *
     * @return The word
     */
    private String getWord() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < mSelection.size(); i++) {
            builder.append(mSelection.get(i));
        }
        return builder.toString();
    }

    /**
     * Highlight the word by blinking the grid for 400ms
     *
     * @param large grid
     */
    private void highlightSelection(final int large) {
        for (int small = 0; small < 9; small++) {
            Tile smallTile = mSmallTiles[large][small];
            smallTile.removeBackgroundDrawable();
        }

        new CountDownTimer(400, 400) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Do Nothing
            }

            @Override
            public void onFinish() {
                for (int small = 0; small < 9; small++) {
                    Tile smallTile = mSmallTiles[large][small];
                    smallTile.notifyStateChanged();
                }
                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_CALLDROP_LITE);
            }
        }.start();
    }

    /**
     * Initialize the board
     */
    public void initBoard() {
        for (int large = 0; large < 9; large++) {
            mLargeTiles[large] = new Tile(this, null);
            for (int small = 0; small < 9; small++) {
                mSmallTiles[large][small] = new Tile(this, mLargeTiles[large]);
            }
        }
    }

    /**
     * Do the required changes when the player makes a move
     *
     * @param large The larger grid
     * @param small The smaller tile
     */
    private void makeMove(int large, int small) {
        switch (mPhase) {
            case PHASE_1:
                makeMovePhaseOne(large, small);
                break;
            case PHASE_2:
                makeNeighbourAvailable(large);
                break;
            default:
                break;
        }

        // Vibrate the phone
        vibrate();

        // Add word to the list of selections
        Tile smallTile = mSmallTiles[large][small];
        mSelection.add(smallTile.getText().charAt(0));

        notifyWordChanged();
        String word = getWord();
        if (inDictionary(word) && !mWordListData.contains(word)) {
            updateScore();
            mWordListData.add(word);
            mWordListAdapter.notifyDataSetChanged();
            highlightSelection(large);
        }
    }

    /**
     * Set the tile state to slected, change the state for all other grids
     * to INACTIVE, make all tile neighbours available
     *
     * @param large The larger grid
     * @param small The smaller tile
     */
    private void makeMovePhaseOne(int large, int small) {
        setTileState(large, small, Tile.State.SELECTED);
        retainGridState(large, Tile.State.INACTIVE);
        makeNeighbourAvailable(large, small);
    }

    /**
     * Make all non-neighbours and the selected neighbours of
     * the given tile as inactive
     *
     * @param mLarge grid
     * @param mSmall tile
     */
    private void makeNeighbourAvailable(int mLarge, int mSmall) {
        for (int small = 0; small < 9; small++) {
            Tile smallTile = mSmallTiles[mLarge][small];

            if (smallTile.isSelected()) {
                continue;
            }

            if (!isNeighbour(mSmall, small)) {
                setTileState(mLarge, small, Tile.State.INACTIVE);
            } else {
                setTileState(mLarge, small, Tile.State.UNSELECTED);
            }
        }
    }

    /**
     * Make all non-neighbours as inactive
     *
     * @param mLarge grid
     */
    private void makeNeighbourAvailable(int mLarge) {
        for (int large = 0; large < 9; large++) {
            if (!isNeighbour(mLarge, large)) {
                setGridState(large, Tile.State.INACTIVE);
            } else {
                setGridState(large, Tile.State.UNSELECTED);
            }
        }
        notifyAllStateChanged();
    }

    /**
     * Lock the given large tile by changing state to lock and
     * hiding all non-selected characters
     *
     * @param large The larger grid
     */
    private void lockGrid(int large) {
        setGridState(large, Tile.State.LOCKED);
        for (int small = 0; small < 9; small++) {
            Tile smallTile = mSmallTiles[large][small];
            if (!smallTile.isSelected()) {
                setTileText(large, small, "");
                setTileState(large, small, Tile.State.INACTIVE);
            }
        }
    }

    /**
     * Unselect all available (not locked) grids
     */
    private void makeGridAvailable() {
        for (int large = 0; large < 9; large++) {
            if (!mLargeTiles[large].isLocked()) {
                setTileState(large, Tile.State.UNSELECTED);
            }
        }
    }

    /**
     * Reset the grid to it's initialial state
     */
    private void resetGrid() {
        for (int large = 0; large < 9; large++) {
            if (mLargeTiles[large].isInactive()) {
                setGridState(large, Tile.State.UNSELECTED);
            }
        }
    }

    /**
     * Unlock all locked grids and hide all inactive ones
     */
    private void unlockAndHideGrid() {
        for (int large = 0; large < 9; large++) {
            if (!mLargeTiles[large].isLocked()) {
                setTileText(large, "");
                setTileState(large, Tile.State.INACTIVE);
            } else {
                setGridState(large, Tile.State.UNSELECTED);
            }
        }
    }

    /**
     * Notify all tiles of change in state
     */
    private void notifyAllStateChanged() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                Tile smallTile = mSmallTiles[large][small];
                smallTile.notifyStateChanged();
            }
        }
    }

    /**
     * Make all grid except the give one and locked grids
     * in the state provided
     *
     * @param mLarge The Grid to keep active
     * @param state The state of the grid
     */
    private void retainGridState(int mLarge, Tile.State state) {
        for (int large = 0; large < 9; large++) {
            if (large != mLarge && !mLargeTiles[large].isLocked()) {
                setTileState(large, state);
            }
        }
    }

    /**
     * Make all tiles in the given grid state as provided
     *
     * @param large grid
     * @param state The state of the grid
     */
    private void setTileState(int large, Tile.State state) {
        for (int small = 0; small < 9; small++) {
            setTileState(large, small, state);
        }
    }

    /**
     * Make the given tile state as provided
     *
     * @param large grid
     * @param small tile
     * @param state The state of the tile
     */
    private void setTileState(int large, int small, Tile.State state) {
        Tile smallTile = mSmallTiles[large][small];
        smallTile.setState(state);
        smallTile.notifyStateChanged();
    }

    /**
     * Make the given grid state as provided
     *
     * @param large grid
     * @param state The state of the tile
     */
    private void setGridState(int large, Tile.State state) {
        Tile largeTile = mLargeTiles[large];
        largeTile.setState(state);
        largeTile.notifyStateChanged();
    }

    /**
     * Make the given tile text as provided
     *
     * @param large grid
     * @param small tile
     * @param text The text of the tile
     */
    private void setTileText(int large, int small, String text) {
        Tile smallTile = mSmallTiles[large][small];
        smallTile.setText(text);
        smallTile.notifyTextChanged();
    }

    /**
     * Make the given tile text as provided
     *
     * @param large grid
     * @param text The text of the tile
     */
    private void setTileText(int large, String text) {
        for (int small = 0; small < 9; small++) {
            setTileText(large, small, text);
        }
    }

    /**
     * Returns a pseudo-random number between min and max.
     * Incudes Min, excludes Max
     *
     * @param min Minimum value
     * @param max Maximum value
     * @return number between min and max
     */
    public int generateNumber(int min, int max) {
        return mRandomGenerator.nextInt(max - min) + min;
    }

    /**
     * Shuffle the given word by characters
     *
     * @param word The array to shuffle
     * @return The shuffled array
     */
    public char[] shuffleWord(char[] word) {
        int[][] shuffle = new int[][]{
                { 5, 8, 7, 6, 3, 0, 1, 2 },
                { 5, 2, 1, 0, 3, 6, 7, 8 },
                { 3, 0, 1, 2, 5, 8, 7, 6 },
                { 3, 6, 7, 8, 5, 2, 1, 0 }
        };
        char[] ans = new char[9];
        int index = generateNumber(0, word.length);
        int wordFilled = 0;
        ans[index] = word[wordFilled++];
        if (index != 4) {
            ans[4] = word[wordFilled++];
        }

        int[] strategy = shuffle[generateNumber(0, 4)];
        for (int i : strategy) {
            if (i != index) {
                ans[i] = word[wordFilled++];
            }
        }

        return ans;
    }

    /**
     * Check whether the given tile has the provided neighbour
     *
     * @param tile The given tile
     * @param neighbour The provided neighbour
     * @return true iff they are neighbours, false otherwise
     */
    private boolean isNeighbour(int tile, int neighbour) {
        switch (tile) {
            case 0:
                if (neighbour == 1 || neighbour == 3 || neighbour == 4) {
                    return true;
                }
                break;
            case 1:
                if (neighbour == 0 || neighbour == 2 || neighbour == 3 ||
                        neighbour == 4 || neighbour == 5) {
                    return true;
                }
                break;
            case 2:
                if (neighbour == 1 || neighbour == 4 || neighbour == 5) {
                    return true;
                }
                break;
            case 3:
                if (neighbour == 0 || neighbour == 1 || neighbour == 4 ||
                        neighbour == 6 || neighbour == 7) {
                    return true;
                }
                break;
            case 4:
                if (neighbour == 0 || neighbour == 1 || neighbour == 2 ||
                        neighbour == 3 || neighbour == 5 || neighbour == 6 ||
                        neighbour == 7 || neighbour == 8) {
                    return true;
                }
                break;
            case 5:
                if (neighbour == 1 || neighbour == 2 || neighbour == 4 ||
                        neighbour == 7 || neighbour == 8) {
                    return true;
                }
                break;
            case 6:
                if (neighbour == 3 || neighbour == 4 || neighbour == 7) {
                    return true;
                }
                break;
            case 7:
                if (neighbour == 3 || neighbour == 4 || neighbour == 5 ||
                        neighbour == 6 || neighbour == 8) {
                    return true;
                }
                break;
            case 8:
                if (neighbour == 4 || neighbour == 5 || neighbour == 7) {
                    return true;
                }
                break;
            default:
                break;
        }

        return false;
    }

    /**
     * Show the Game UI
     */
    private void showGame() {
        mProgressBar.setVisibility(View.GONE);
        mActivity.findViewById(R.id.twoplayerwordgame_game_frame).setVisibility(View.VISIBLE);
    }

    /**
     * Class used to Load Dictionary in Memory
     */
    private class Dictionary extends Thread {
        private final int fileId;
        private String[] dictionary;

        Dictionary(int fileId) {
            this.fileId = fileId;
        }

        /**
         * Load dictionary stored in file in a string array
         *
         * @param fileId ResourceId of the file
         * @return The loaded dictionary
         */
        private String[] loadDictionary(int fileId) {
            String[] dictionary = new String[74450];
            int index = 0;
            InputStream inputStream = getResources().openRawResource(fileId);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            try {
                String currentLine;

                while ((currentLine = bufferedReader.readLine()) != null) {
                    dictionary[index] = currentLine;
                    index++;
                    if (index % 7000 == 0 && mProgressBar != null) {
                        mProgressStatus += 2;
                        mProgressBar.setProgress(mProgressStatus);
                    }
                }

                if (index != dictionary.length) {
                    dictionary = Arrays.copyOfRange(dictionary, 0, index);
                }
            } catch (IOException e) {
                Log.e(Constants.TAG_IO_ERROR, "Failed to read: " + e.getMessage());
            } finally {
                try {
                    bufferedReader.close();
                    reader.close();
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(Constants.TAG_IO_ERROR, "Failed to close: " + e.getMessage());
                }
            }

            return dictionary;
        }

        public String[] getDictionary() {
            return dictionary;
        }

        @Override
        public void run() {
            dictionary = loadDictionary(fileId);
        }
    }

    /**
     * Class used to load Dictionary
     */
    private class Loader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            int[] files = {
                    R.raw.wordgame_xaa,
                    R.raw.wordgame_xab,
                    R.raw.wordgame_xac,
                    R.raw.wordgame_xad,
                    R.raw.wordgame_xae
            };

            // Spawn Threads
            Dictionary[] dictionaries = new Dictionary[files.length];
            for (int i = 0; i < dictionaries.length; i++) {
                dictionaries[i] = new Dictionary(files[i]);
            }
            for (int i = 0; i < dictionaries.length; i++) {
                dictionaries[i].start();
            }

            // Wait for those threads to complete
            try {
                for (int i = 0; i < dictionaries.length; i++) {
                    dictionaries[i].join();
                }
            } catch (InterruptedException e) {
                Log.e(Constants.TAG_INTERRUPTED_ERROR, "Failed to join Thread: " + e.getMessage());
            }

            Dictionary dictionary = new Dictionary(R.raw.wordlistofnine);
            dictionary.run();
            mDictionary = new ArrayList<String[]>();
            mDictionary.add(dictionary.getDictionary());
            for (int i = 0; i < dictionaries.length; i++) {
                mDictionary.add(dictionaries[i].getDictionary());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            initTiles();
            initTimer();
            ((GameActivity) mActivity).restoreState();
            if (mTime == PHASE_TIME && mPhase == PHASE_1 && mGamePlayManager.hasGameTurn()) {
                mGcmManager.sendMessage(saveState(), true, mScore);
            }
            showGame();
            if (!mGamePlayManager.hasGameTurn()) {
                displayWaitForTurnDialog();
            }
        }

    }

    /**
     * Create a string containing the state of the game
     *
     * @return Game state
     */
    public String saveState() {
        // Dictionary has not been loaded
        if (mDictionary == null || mPhase == PHASE_3) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (int large = 0; large < 9; large++) {
            builder.append(mLargeTiles[large].getState());
            builder.append(',');
            for (int small = 0; small < 9; small++) {
                builder.append(mSmallTiles[large][small].getState());
                builder.append(',');
            }
        }
        builder.append(mTime);
        builder.append(',');
        builder.append(mPhase);
        builder.append(',');
        builder.append(mScore);
        builder.append(',');
        builder.append(mLargeBoard.getVisibility());
        builder.append(',');
        builder.append(mWordListData.size());
        builder.append(',');
        for (String word : mWordListData) {
            builder.append(word);
            builder.append(',');
        }
        builder.append(getWord());
        builder.append(',');
        return builder.toString();
    }

    /**
     * Restore the saved word to the list
     *
     * @param word The word to restore
     */
    private void restoreWordList(String word) {
        mSelection.clear();
        for (int i = 0; i < word.length(); i++) {
            mSelection.add(word.charAt(i));
        }
        notifyWordChanged();
    }

    /**
     * Restore the state of the game from the given string
     *
     * @param gameData Game state
     */
    public void restoreState(String gameData) {
        String[] fields = gameData.split(",");
        int index = 0;
        for (int large = 0; large < 9; large++) {
            String[] gridState = fields[index++].split(":");
            setGridState(large, Tile.State.valueOf(gridState[1]));
            for (int small = 0; small < 9; small++) {
                String[] tileState = fields[index++].split(":");
                setTileText(large, small, tileState[0]);
                setTileState(large, small, Tile.State.valueOf(tileState[1]));
            }
        }
        mTime = Integer.parseInt(fields[index++]);
        initTimer();
        mPhase = Integer.parseInt(fields[index++]);
        notifyAllStateChanged();
        mScore = Integer.parseInt(fields[index++]);
        if (Integer.parseInt(fields[index++]) == View.INVISIBLE) {
            pauseGame();
        }
        notifyScoreChanged();
        int listSize = Integer.parseInt(fields[index++]);
        mWordListData.clear();
        for (int i = 0; i < listSize; i++) {
            mWordListData.add(fields[index++]);
        }
        mWordListAdapter.notifyDataSetChanged();
        if (index < fields.length) {
            restoreWordList(fields[index]);
        }
    }

    /**
     * Return the current phase
     *
     * @return Phase
     */
    public int getPhase() {
        return mPhase;
    }
}
