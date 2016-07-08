package edu.neu.madcourse.adibalwani.finalproject;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Locale;

import edu.neu.madcourse.adibalwani.finalproject.dialog.DialogManager;
import edu.neu.madcourse.adibalwani.finalproject.dialog.EndGameDialogFragment;
import edu.neu.madcourse.adibalwani.finalproject.dialog.PauseDialogFragment;
import edu.neu.madcourse.adibalwani.finalproject.dialog.RegisterDialogFragment;
import edu.neu.madcourse.adibalwani.finalproject.gamelogic.Dribble;
import edu.neu.madcourse.adibalwani.finalproject.gamelogic.Shoot;
import edu.neu.madcourse.adibalwani.finalproject.leaderboard.LeaderBoardManager;
import edu.neu.madcourse.adibalwani.finalproject.network.NetworkManager;
import edu.neu.madcourse.adibalwani.finalproject.register.RegisterManager;

public class GameActivity extends AppCompatActivity
        implements Dribble.DribbleListener, Shoot.ShootListener {

    private final static String LOG_TAG = GameActivity.class.getSimpleName();

    private enum Phase {
        PHASE_DRIBBLE, PHASE_SHOOT
    }

    private Dribble mDribble;
    private Shoot mShoot;
    private Phase mPhase;
    private TextView mText;
    private TextView mScoreView;
    private ImageView mPauseButton;
    private LinearLayout rootView;
    private TextToSpeech mTextToSpeech;
    private int mScore;
    private RegisterManager mRegisterManager;
    private DialogManager mDialogManager;
    private boolean mGameEnded;
    private boolean mPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finalproject_activity_game);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initInstances();
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.US);
                }
            }
        });
        mPhase = Phase.PHASE_DRIBBLE;
        boolean restore = getIntent().getBooleanExtra(Constants.INTENT_KEY_RESTORE, false);
        mDribble = new Dribble(this, this, new Dribble.Config());
        if (restore) {
            restoreState();
        }

        if (mPaused) {
            //pauseGame();
        } else {
            if (mPhase == Phase.PHASE_DRIBBLE) {
                mDribble.onStart();
            } else {
                mShoot.onStart();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        saveState();
        if (!mGameEnded) {
            if (mPhase == Phase.PHASE_DRIBBLE) {
                mDribble.onPause();
            } else {
                mShoot.onPause();
            }
        }
    }

    @Override
    public void onSuccessDribble() {
        Log.i(LOG_TAG, "Dribble Phase Completed");
        mPhase = Phase.PHASE_SHOOT;
        mDribble = new Dribble(this, this, new Dribble.Config());
        mShoot.onStart();
    }

    @Override
    public void onSuccessShoot() {
        Log.i(LOG_TAG, "Shoot Phase Completed");
        mPhase = Phase.PHASE_DRIBBLE;
        incrementScore();
        mDribble.onStart();
    }

    @Override
    public void onGameEnd() {
        Log.i(LOG_TAG, "Game Ended");
        mText.setText(Constants.GAME_END);
        mTextToSpeech.speak(Constants.GAME_END, TextToSpeech.QUEUE_FLUSH, null);
        mGameEnded = true;
        removeState();
        mPauseButton.setVisibility(View.INVISIBLE);

        // Check Registeration Status
        if (!mRegisterManager.isRegistered()) {
            mDialogManager.displayRegisterDialog(R.layout.finalproject_dialog_register,
                    new RegisterDialogFragment.DismissListener() {
                @Override
                public void onDismiss() {
                    mRegisterManager.notifyRegisterChanged();
                    updateLeaderBoardAndEndGame();
                }
            });
        } else {
            updateLeaderBoardAndEndGame();
        }
    }

    /**
     * Initialize the instance variables
     */
    private void initInstances() {
        mShoot = new Shoot(this, this);
        mScore = 0;
        mRegisterManager = new RegisterManager(this);
        mDialogManager = new DialogManager(this);
        mGameEnded = false;
    }

    /**
     * Initialize the view instances
     */
    private void initViews() {
        rootView = (LinearLayout) findViewById(R.id.root_view_finalproject_activity_game);
        mText = (TextView) findViewById(R.id.finalproject_activity_game_text);
        mScoreView = (TextView) findViewById(R.id.finalproject_activity_game_score);
        mPauseButton = (ImageView) findViewById(R.id.finalproject_activity_game_pause);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseGame();
            }
        });
        TextViewFont font = new TextViewFont();
        font.overrideFonts(getApplicationContext(), rootView);
    }

    /**
     * Pause the current game
     */
    private void pauseGame() {
        mGameEnded = true;
        saveState();
        if (!mPaused) {
            if (mPhase == Phase.PHASE_DRIBBLE) {
                mDribble.onPause();
            } else {
                mShoot.onPause();
            }
        }
        mPaused = true;
        mDialogManager.displayPauseDialog(R.layout.finalproject_dialog_pause,
                new PauseDialogFragment.DismissListener() {
                    @Override
                    public void onEndClicked() {
                        onGameEnd();
                    }

                    @Override
                    public void onResumeClicked() {
                        mGameEnded = false;
                        mPaused = false;
                        if (mPhase == Phase.PHASE_DRIBBLE) {
                            mDribble.onStart();
                        } else {
                            mShoot.onStart();
                        }
                    }
                });
    }

    /**
     * Increment the current score and update the UI
     */
    private void incrementScore() {
        mScore++;
        mScoreView.setText(Constants.SCORE + mScore);
    }

    /**
     * Update the leaderboard (if username provided) and display the end game dialog
     */
    private void updateLeaderBoardAndEndGame() {
        updateLeaderBoard();
        /*mDialogManager.displayEndGameDialog(R.layout.finalproject_dialog_end, mScore,
                new EndGameDialogFragment.DismissListener() {
                    @Override
                    public void onDismiss() {
                        finish();
                    }
                }
        );*/
    }

    /**
     * Update the leaderboard (if username provided)
     */
    private void updateLeaderBoard() {
        String username = mRegisterManager.getUsername();
        if (username != null) {
            LeaderBoardManager leaderBoardManager = new LeaderBoardManager(this);
            leaderBoardManager.storeScore(mScore);
            if (NetworkManager.isNetworkAvailable(this)) {
                leaderBoardManager.syncWithFirebase();
            }
        }
    }

    /**
     * Save the current game state
     */
    private void saveState() {
        if (mGameEnded) {
            return;
        }
        GameSharedPreference gameSharedPreference = new GameSharedPreference(this);
        gameSharedPreference.putString(Constants.PROPERTY_GAME_SCORE, String.valueOf(mScore));
        gameSharedPreference.putString(Constants.PROPERTY_GAME_PAUSE, String.valueOf(mPaused));
        if (mPhase == Phase.PHASE_DRIBBLE) {
            gameSharedPreference.putString(Constants.PROPERTY_GAME_CONFIG,
                    new Gson().toJson(mDribble.getConfig()));
        }
    }

    /**
     * Remove the game state
     */
    private void removeState() {
        GameSharedPreference gameSharedPreference = new GameSharedPreference(this);
        gameSharedPreference.putString(Constants.PROPERTY_GAME_SCORE, null);
        gameSharedPreference.putString(Constants.PROPERTY_GAME_CONFIG, null);
        gameSharedPreference.putString(Constants.PROPERTY_GAME_PAUSE, null);
    }

    /**
     * Restore game state
     */
    private void restoreState() {
        GameSharedPreference gameSharedPreference = new GameSharedPreference(this);
        String score = gameSharedPreference.getString(Constants.PROPERTY_GAME_SCORE);
        mScore = Integer.parseInt(score);
        String pause = gameSharedPreference.getString(Constants.PROPERTY_GAME_PAUSE);
        mPaused = Boolean.parseBoolean(pause);
        String config = gameSharedPreference.getString(Constants.PROPERTY_GAME_CONFIG);
        Dribble.Config conf = new Gson().fromJson(config, new TypeToken<Dribble.Config>(){}.getType());
        mDribble.setConfig(conf);
    }
}
