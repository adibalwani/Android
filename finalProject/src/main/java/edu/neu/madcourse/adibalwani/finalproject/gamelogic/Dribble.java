package edu.neu.madcourse.adibalwani.finalproject.gamelogic;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

import edu.neu.madcourse.adibalwani.finalproject.Constants;
import edu.neu.madcourse.adibalwani.finalproject.R;

/**
 * Class used to handle Dribble Phase
 */
public class Dribble implements DribbleDetector.OnDribbleListener {

    private static final String LOG_TAG = Dribble.class.getSimpleName();

    public interface DribbleListener {
        /**
         * Method to call on a successful dribble
         */
        void onSuccessDribble();

        /**
         * Method to call on game end
         */
        void onGameEnd();
    }

    private static final int GAME_OVER_TIME = 5000;
    private static final int MIN_TIME = 3;
    private static final int MAX_TIME = 7;
    private static final double ACCURACY = 0.4;

    private Activity mActivity;
    private SensorManager mSensorManager;
    private Sensor mLinearAcceleration;
    private Sensor mRotationVector;
    private DribbleDetector mDribbleDetector;
    private MediaPlayer mDribbleSound;
    private boolean mDribbleDetected;
    private CountDownTimer mEndGameTimer;
    private CountDownTimer mDribblePhaseTimer;
    private boolean mGameEnded;
    private DribbleListener mDribbleListener;
    private TextView mText;
    private TextToSpeech mTextToSpeech;
    private Config mConfig;

    public Dribble(Activity activity, DribbleListener dribbleListener, Config config) {
        this.mActivity = activity;
        mDribbleDetected = false;
        mDribbleListener = dribbleListener;
        mConfig = config;
        initSensorInstances();
    }

    public void onStart() {
        initInstances();
        registerSensorEvent();
        initViews();
        playDribbleSound();
        startEndGameTimer(mConfig.endGameTimeLeft);
        startDribblePhaseTimer(mConfig.dribblePhaseTimeLeft);
    }

    public void onPause() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        if (!mGameEnded) {
            unregisterSensorEvent();
            stopDribbleSound();
            cancelEndGameTimer();
            cancelDribblePhaseTimer();
        }
    }

    public void reset() {
        mConfig = new Config();
    }

    @Override
    public void onDribble() {
        if (!mDribbleDetected) {
            mDribbleDetected = true;
            cancelEndGameTimer();
            mConfig.dribbleCount++;
        }
    }

    /**
     * Initialize the sensor's instance variables
     */
    private void initSensorInstances() {
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mDribbleDetector = new DribbleDetector();
        mDribbleDetector.setDribbleListener(this);
    }

    /**
     * Initialize the instance variables
     */
    private void initInstances() {
        mTextToSpeech = new TextToSpeech(mActivity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.US);
                }
            }
        });
        mDribbleSound = MediaPlayer.create(mActivity, R.raw.dribbling_sound);
    }

    /**
     * Initialize the views
     */
    private void initViews() {
        mText = (TextView) mActivity.findViewById(R.id.finalproject_activity_game_text);
        mText.setText(Constants.KEEP_DRIBBLING);
    }

    /**
     * Register the sensor manager to listen to dribble events
     */
    private void registerSensorEvent() {
        Log.i(LOG_TAG, "Registering sensor");
        mSensorManager.registerListener(
                mDribbleDetector,
                mLinearAcceleration,
                20000
                //SensorManager.SENSOR_DELAY_GAME
        );
        mSensorManager.registerListener(
                mDribbleDetector,
                mRotationVector,
                20000
                //SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    /**
     * Unregister the sensor manager to listen to dribble events
     */
    private void unregisterSensorEvent() {
        mSensorManager.unregisterListener(mDribbleDetector);
    }

    /**
     * Initialize and start the dribble phase countdown timer
     *
     * @param time The time to start for
     */
    private void startDribblePhaseTimer(final long time) {
        Log.i(LOG_TAG, "Time: " + time);
        mDribblePhaseTimer = new CountDownTimer(time, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                mConfig.dribblePhaseTimeLeft = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                Log.i(LOG_TAG, "Dribble Count: " + mConfig.dribbleCount);
                if (mConfig.dribbleCount >= ACCURACY * time / 1000) {
                    reset();
                    onPause();
                    mDribbleListener.onSuccessDribble();
                } else {
                    reset();
                    startDribblePhaseTimer(mConfig.dribblePhaseTimeLeft);
                }
            }
        };
        mDribblePhaseTimer.start();
    }

    /**
     * Initialize and start the end game countdown timer
     */
    private void startEndGameTimer(long time) {
        mEndGameTimer = new CountDownTimer(time, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                mConfig.endGameTimeLeft = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                endGame();
            }
        };
        mEndGameTimer.start();
    }

    /**
     * Cancel the dribble phase timer (if any)
     */
    private void cancelDribblePhaseTimer() {
        if (mDribblePhaseTimer != null) {
            mDribblePhaseTimer.cancel();
        }
    }

    /**
     * Cancel the end game timer (if any)
     */
    private void cancelEndGameTimer() {
        if (mEndGameTimer != null) {
            mEndGameTimer.cancel();
        }
    }

    /**
     * Play the basketball dribble sound in a loop
     */
    private void playDribbleSound() {
        mDribbleSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mDribbleDetected) {
                    startEndGameTimer(GAME_OVER_TIME);
                }
                mDribbleDetected = false;
                mDribbleSound.seekTo(0);
                mDribbleSound.start();
            }
        });
        mDribbleSound.start();
    }

    /**
     * Stop the basketball dribbling sound
     */
    private void stopDribbleSound() {
        mDribbleSound.stop();
        mDribbleSound.reset();
        mDribbleSound.release();
    }

    /**
     * End the dribble game
     */
    private void endGame() {
        mGameEnded = true;
        unregisterSensorEvent();
        stopDribbleSound();
        cancelEndGameTimer();
        cancelDribblePhaseTimer();
        mDribbleListener.onGameEnd();
    }

    /**
     * Configuration class
     */
    public static class Config {
        long dribblePhaseTimeLeft;
        long endGameTimeLeft;
        long dribbleCount;
        private static Random mRandomGenerator = new Random();

        public Config() {
            dribblePhaseTimeLeft = generateNumber(MIN_TIME, MAX_TIME + 1) * 1000;
            endGameTimeLeft = GAME_OVER_TIME;
            dribbleCount = 0;
        }

        /**
         * Returns a pseudo-random number between min and max.
         * Incudes Min, excludes Max
         *
         * @param min Minimum value
         * @param max Maximum value
         * @return number between min and max
         */
        private static int generateNumber(int min, int max) {
            return mRandomGenerator.nextInt(max - min) + min;
        }
    }

    public Config getConfig() {
        return mConfig;
    }

    public void setConfig(Config config) {
        this.mConfig = config;
    }
}
