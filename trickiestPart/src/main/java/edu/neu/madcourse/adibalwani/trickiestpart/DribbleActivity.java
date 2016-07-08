package edu.neu.madcourse.adibalwani.trickiestpart;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class DribbleActivity extends AppCompatActivity {

    private static final int GAME_OVER_TIME = 5000;

    private SensorManager mSensorManager;
    private Sensor mLinearAcceleration;
    private Sensor mRotationVector;
    private Sensor mGyro;
    private DribbleDetector mDribbleDetector;
    private MediaPlayer mMediaPlayer;
    private boolean mDribbleDetected;
    private CountDownTimer mTimer;
    private boolean mGameEnded;
    private TextView count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trickiest_part_activity_dribble);
        initInstances();

        // Set ActionBar label
        this.setTitle(getResources().getString(R.string.trickiest_part_titlebar_name));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaPlayer = MediaPlayer.create(this, R.raw.basketball_dribble);
        registerSensorEvent();
        playDribbleSound();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mGameEnded) {
            unregisterSensorEvent();
            stopDribbleSound();
            cancelTimer();
        }
    }

    /**
     * Initialize the instance variables
     */
    private void initInstances() {
        mMediaPlayer = MediaPlayer.create(this, R.raw.basketball_dribble);
        mDribbleDetected = false;
        mGameEnded = false;
        count = (TextView) findViewById(R.id.dribble_count);
        initSensorInstances();
    }

    /**
     * Initialize the sensor's instance variables
     */
    private void initSensorInstances() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mDribbleDetector = new DribbleDetector();
        mDribbleDetector.setDribbleListener(new DribbleDetector.OnDribbleListener() {
            @Override
            public void onDribble() {
                if (!mDribbleDetected) {
                    count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                    mDribbleDetected = true;
                    cancelTimer();
                }
            }
        });
    }

    /**
     * Initialize and start a countdown timer
     */
    private void startTimer() {
        mTimer = new CountDownTimer(GAME_OVER_TIME, GAME_OVER_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Do  Nothing
            }

            @Override
            public void onFinish() {
                endGame();
            }
        };
        mTimer.start();
    }

    /**
     * Cancel the timer (if any)
     */
    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    /**
     * Register the sensor manager to listen to dribble events
     */
    private void registerSensorEvent() {
        mSensorManager.registerListener(
                mDribbleDetector,
                mLinearAcceleration,
                SensorManager.SENSOR_DELAY_GAME
        );
        mSensorManager.registerListener(
                mDribbleDetector,
                mRotationVector,
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    /**
     * Unregister the sensor manager to listen to dribble events
     */
    private void unregisterSensorEvent() {
        mSensorManager.unregisterListener(mDribbleDetector);
    }

    /**
     * Play the basketball dribble sound in a loop
     */
    private void playDribbleSound() {
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mDribbleDetected) {
                    startTimer();
                }
                mDribbleDetected = false;
                mMediaPlayer.seekTo(0);
                mMediaPlayer.start();
            }
        });
        mMediaPlayer.start();
    }

    /**
     * Stop the basketball dribbling sound
     */
    private void stopDribbleSound() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }

    /**
     * End the dribble game
     */
    private void endGame() {
        mGameEnded = true;
        unregisterSensorEvent();
        stopDribbleSound();
        cancelTimer();
        findViewById(R.id.trickiest_part_dribble_continue).setVisibility(View.GONE);
        findViewById(R.id.trickiest_part_dribble_game_over).setVisibility(View.VISIBLE);
    }
}
