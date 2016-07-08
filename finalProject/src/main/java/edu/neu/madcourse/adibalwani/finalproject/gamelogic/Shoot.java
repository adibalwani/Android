package edu.neu.madcourse.adibalwani.finalproject.gamelogic;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

import edu.neu.madcourse.adibalwani.finalproject.Constants;
import edu.neu.madcourse.adibalwani.finalproject.R;

/**
 * Class used to handle Shoot Phase
 */
public class Shoot implements ShootDetector.OnShootListener {

    public interface ShootListener {
        /**
         * Method to call on a successful dribble
         */
        void onSuccessShoot();

        /**
         * Method to call on game end
         */
        void onGameEnd();
    }

    private static final long FAST_TIME = 2000;
    private static final long MEDIUM_TIME = 4000;
    private static final long SLOW_TIME = 6000;

    private Activity mActivity;
    private ShootListener mShootListener;
    private SensorManager mSensorManager;
    private Sensor mAcceleration;
    private Sensor mRotation;
    private ShootDetector mShootDetector;
    private TextView mText;
    private Direction mDirection;
    private Timing mTiming;
    private Random mRandomGenerator;
    private CountDownTimer mShootTimer;
    private Vibrator mVibrator;
    private TextToSpeech mTextToSpeech;
    private boolean mShotDetected;
    private long mTimeSince;
    private long mGameTime;

    public Shoot(Activity activity, ShootListener shootListener) {
        this.mActivity = activity;
        this.mShootListener = shootListener;
        mRandomGenerator = new Random();
        mVibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        mTextToSpeech = new TextToSpeech(mActivity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.US);
                }
            }
        });
        initSensorInstances();
    }

    public void onStart() {
        mShotDetected = false;
        mShootDetector = new ShootDetector();
        mShootDetector.setShootListener(this);
        Direction[] directions = Direction.values();
        mDirection = directions[generateNumber(0, directions.length)];
        Timing[] timings = Timing.values();
        mTiming = timings[generateNumber(0, timings.length)];
        // TODO: Uncomment if direction works as required
        mTextToSpeech.speak(mTiming.toString(), TextToSpeech.QUEUE_FLUSH, null);
        /*mTextToSpeech.speak(mTiming.toString() + " " + mDirection.toString(),
                TextToSpeech.QUEUE_FLUSH, null);*/
        mText = (TextView) mActivity.findViewById(R.id.finalproject_activity_game_text);
        // TODO: Uncomment if direction works as required
        mText.setText(mTiming.toString());
        //mText.setText(mTiming.toString() + " " + mDirection.toString());
        registerSensorEvent();
        startShootTimer();
        vibrate();
    }

    public void onPause() {
        cancelShootTimer();
        unregisterSensorEvent();
    }

    @Override
    public void onShoot(Direction dir) {
        if ((mGameTime == MEDIUM_TIME && mTimeSince < FAST_TIME) ||
                (mGameTime == SLOW_TIME && mTimeSince < MEDIUM_TIME)) {
            onPause();
            mText.setText(Constants.GAME_END);
            mShootListener.onGameEnd();
            return;
        }

        mShotDetected = true;
        onPause();
        // TODO: Uncomment if direction is detected properly
        mTextToSpeech.speak(Constants.NICE_SHOT, TextToSpeech.QUEUE_FLUSH, null);
        mShootListener.onSuccessShoot();
        /*if (dir == mDirection) {
            mShootListener.onSuccessShoot();
            mTextToSpeech.speak(Constants.NICE_SHOT, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            mText.setText(Constants.GAME_END);
            mShootListener.onEndClicked();
        }*/
    }

    /**
     * Initialize the sensor's instance variables
     */
    private void initSensorInstances() {
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    /**
     * Register the sensor manager to listen to dribble events
     */
    private void registerSensorEvent() {
        mSensorManager.registerListener(
                mShootDetector,
                mAcceleration,
                SensorManager.SENSOR_DELAY_GAME
        );
        mSensorManager.registerListener(
                mShootDetector,
                mRotation,
                SensorManager.SENSOR_DELAY_GAME
        );
    }

    /**
     * Unregister the sensor manager to listen to dribble events
     */
    private void unregisterSensorEvent() {
        mSensorManager.unregisterListener(mShootDetector);
    }

    /**
     * Initialize and start the shoot countdown timer
     */
    private void startShootTimer() {
        mGameTime = FAST_TIME;
        if (mTiming == Timing.MEDIUM) {
            mGameTime = MEDIUM_TIME;
        } else if (mTiming == Timing.SLOW) {
            mGameTime = SLOW_TIME;
        }
        final long time = mGameTime;
        mTimeSince = 0;
        mShootTimer = new CountDownTimer(time, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeSince = time - millisUntilFinished;
            }

            @Override
            public void onFinish() {
                if (!mShotDetected) {
                    onPause();
                    mText.setText(Constants.GAME_END);
                    mShootListener.onGameEnd();
                }
            }
        };
        mShootTimer.start();
    }

    /**
     * Cancel the shoot timer (if any)
     */
    private void cancelShootTimer() {
        if (mShootTimer != null) {
            mShootTimer.cancel();
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
     * Vibrate the phone for 500ms
     */
    private void vibrate() {
        mVibrator.vibrate(500);
    }
}
