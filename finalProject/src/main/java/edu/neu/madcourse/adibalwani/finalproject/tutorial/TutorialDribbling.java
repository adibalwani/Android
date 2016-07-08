package edu.neu.madcourse.adibalwani.finalproject.tutorial;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.neu.madcourse.adibalwani.finalproject.Constants;
import edu.neu.madcourse.adibalwani.finalproject.R;
import edu.neu.madcourse.adibalwani.finalproject.TextViewFont;
import edu.neu.madcourse.adibalwani.finalproject.gamelogic.DribbleDetector;

/**
 * @author rachit on 19-04-2016.
 */
public class TutorialDribbling extends Fragment implements DribbleDetector.OnDribbleListener {

    private MediaPlayer mDribbleSound;
    private Animation successAnimation;
    private Animation rotate;
    private int count = 0;
    private boolean mDribbleDetected;

    private SensorManager mSensorManager;
    private Sensor mLinearAcceleration;
    private Sensor mRotationVector;
    private DribbleDetector mDribbleDetector;
    private FragmentManager frag = null;
    private Timer mTimer;

    private TextView dribbleCount;
    private ImageView dribbleSuccessfull;
    private ImageView dribbleImage;
    private LinearLayout armLayout;
    private LinearLayout rootView;
    private static final int MAX_DRIBBLE = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_dribbling, container, false);
        rootView = (LinearLayout) view.findViewById(R.id.root_view_fragment_tutorial_dribbling);
        armLayout = (LinearLayout) view.findViewById(R.id.tutorial_dribble_phone_image_layout);
        dribbleCount = (TextView) view.findViewById(R.id.tutorial_dribbling_count);
        dribbleSuccessfull = (ImageView) view.findViewById(R.id.tutorial_dribble_phone_accomplishment);
        dribbleImage = (ImageView) view.findViewById(R.id.tutorial_dribble_phone_image);
        TextViewFont font = new TextViewFont();
        font.overrideFonts(getActivity().getApplicationContext(), rootView);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        successAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.scaleimage);
        rotate = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        dribbleImage.setAnimation(rotate);
        View parent = (View) dribbleImage.getParent();
        if (parent != null) {
            dribbleImage.setPivotX(0);
            dribbleImage.setPivotY((1/3)*parent.getHeight());
        }
        playDribbleSound();
        initSensorInstances();
        registerSensorEvent();
    }

    /**
     * Initialize the sensor's instance variables
     */
    private void initSensorInstances() {
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mDribbleDetector = new DribbleDetector();
        mDribbleDetector.setDribbleListener(this);
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
        mDribbleSound = MediaPlayer.create(getActivity(), R.raw.dribbling_sound);
        mDribbleSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mDribbleDetected) {
                    Log.i(Constants.TAG, "Tutorial Dribble Detected");
                    count++;
                    if (count >= MAX_DRIBBLE) {
                        count = MAX_DRIBBLE;
                        nextTutorial();
                    }
                    dribbleCount.setText(String.valueOf(MAX_DRIBBLE - count) + " ");
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
        if (mDribbleSound != null) {
            mDribbleSound.stop();
            mDribbleSound.reset();
            mDribbleSound.release();
        }
    }

    private void nextTutorial() {
        dribbleImage.clearAnimation();
        armLayout.setVisibility(View.GONE);
        dribbleImage.setVisibility(View.GONE);
        dribbleSuccessfull.setVisibility(View.VISIBLE);
        dribbleSuccessfull.setAnimation(successAnimation);
        successAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mTimer = new Timer(1000, 1000);
                mTimer.start();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public class Timer extends CountDownTimer {

        public Timer(long start, long tick) {
            super(start, tick);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            frag = getFragmentManager();
            frag.beginTransaction().replace(R.id.tutorial_fragment, new ShootGuide()).commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
        }
        stopDribbleSound();
        unregisterSensorEvent();
    }

    @Override
    public void onDribble() {
        if (!mDribbleDetected) {
            mDribbleDetected = true;
        }
    }
}
