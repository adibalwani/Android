package edu.neu.madcourse.adibalwani.finalproject.tutorial;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.neu.madcourse.adibalwani.finalproject.Constants;
import edu.neu.madcourse.adibalwani.finalproject.GameSharedPreference;
import edu.neu.madcourse.adibalwani.finalproject.HomeScreenActivity;
import edu.neu.madcourse.adibalwani.finalproject.R;
import edu.neu.madcourse.adibalwani.finalproject.TextViewFont;
import edu.neu.madcourse.adibalwani.finalproject.gamelogic.Direction;
import edu.neu.madcourse.adibalwani.finalproject.gamelogic.ShootDetector;

/**
 * @author rachit on 19-04-2016.
 */
public class TutorialShoot extends Fragment implements ShootDetector.OnShootListener, View.OnClickListener {

    private TextView shoot;
    private ImageView successShot;
    private LinearLayout timelefttoshoot;
    private TextView timecounter;
    private Animation successAnimation;
    private LinearLayout rootView;

    private CountDownTimer mTimer;
    private OneSecondTimer oneTimer;
    private static final int COUNTDOWN = 4000;
    private static final int PER_SECOND = 1000;
    private static final long FAST_TIME = 2000;
    private static final long MEDIUM_TIME = 4000;
    private static final long SLOW_TIME = 6000;
    private int count = 3;
    private SensorManager mSensorManager;
    private Sensor mAcceleration;
    private Sensor mRotation;
    private ShootDetector mShootDetector;
    private CountDownTimer mShootTimer;
    private Button retry;
    private View mView;
    private String[] shootTypes = {"MEDIUM SHOT", "SLOW SHOT", "FAST SHOT"};
    private boolean[] shotsDetected = new boolean[3];
    private int successfulShots = 0;
    private GameSharedPreference sharedPreference;
    private long mTimeSince;
    private long mGameTime;
    private HintTimer mHintTimer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_tutorial_shoot_medium_center, container, false);
        rootView = (LinearLayout) mView.findViewById(R.id.root_view_fragment_tutorial_shoot_medium_center);
        shoot = (TextView) mView.findViewById(R.id.tutorial_shoot_direction);
        retry = (Button) mView.findViewById(R.id.tutorial_shoot_lets_try);
        successShot = (ImageView) mView.findViewById(R.id.tutorial_shoot_phone_success);
        retry.setOnClickListener(this);
        timelefttoshoot = (LinearLayout) mView.findViewById(R.id.tutorial_shoot_time_left);
        timecounter = (TextView) mView.findViewById(R.id.tutorial_counter);
        sharedPreference = new GameSharedPreference(getActivity());
        TextViewFont font = new TextViewFont();
        font.overrideFonts(getActivity().getApplicationContext(), rootView);
        return mView;
    }

    private void startTimer() {
        timelefttoshoot.setVisibility(View.INVISIBLE);
        mTimer = new CountDownTimer(COUNTDOWN, PER_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                shoot.setText(String.valueOf(count));
                count--;
            }

            @Override
            public void onFinish() {
                Vibrator v = (Vibrator) getActivity().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
                int shotType = 0;
                for (Boolean b : shotsDetected) {
                    if (b == false) {
                        successfulShots = shotType;
                        break;
                    }
                    shotType++;
                }
                shoot.setText(shootTypes[successfulShots]);
                startShootTimer(successfulShots);
                mHintTimer = new HintTimer(mGameTime, 1000);
                mHintTimer.start();
                timelefttoshoot.setVisibility(View.VISIBLE);
            }
        };
        mTimer.start();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        startTimer();
        initSensorInstances();
        registerSensorEvent();
    }

    /**
     * Initialize and start the shoot countdown timer
     */
    private void startShootTimer(int value) {
        if (value == 0) {
            mGameTime = MEDIUM_TIME;
        } else if (value == 1) {
            mGameTime = SLOW_TIME;
        } else {
            mGameTime = FAST_TIME;
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
                shoot.setText("YOU ARE WAITING TOO LONG TO SHOOT. SYNCHRONIZE WITH THE TIMER");
                retry.setVisibility(View.VISIBLE);
            }
        };
        mShootTimer.start();
    }

    public class HintTimer extends CountDownTimer {

        public HintTimer(long start, long tick) {
            super(start, tick);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long time = millisUntilFinished/1000;
            timecounter.setText(String.valueOf(time));
        }

        @Override
        public void onFinish() {
            timecounter.setText("0");
        }
    }

    /**
     * Initialize the sensor's instance variables
     */
    private void initSensorInstances() {
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mShootDetector = new ShootDetector();
        mShootDetector.setShootListener(this);
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

    @Override
    public void onPause() {
        super.onPause();
        unregisterSensorEvent();
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mShootTimer  != null) {
            mShootTimer.cancel();
        }
        if (oneTimer != null) {
            oneTimer.cancel();
        }
    }

    public class OneSecondTimer extends CountDownTimer {

        public OneSecondTimer(long start, long tick) {
            super(start, tick);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if (shotsDetected[2] == true) {
                retry.setVisibility(View.INVISIBLE);
                sharedPreference.putString(Constants.TUTORIAL_COMPLETED, "TRUE");
                Intent intent = new Intent(getActivity(), HomeScreenActivity.class);
                startActivity(intent);
            } else {
                retry.setVisibility(View.INVISIBLE);
                successShot.clearAnimation();
                successShot.setVisibility(View.GONE);
                shoot.setVisibility(View.VISIBLE);
                count = 3;
                startTimer();
            }
        }
    }

    private void nextShot(int val) {
        if (val == 1) {
            successShot.setImageDrawable(getResources().getDrawable(R.drawable.shot1success));
        } else if (val == 2) {
            successShot.setImageDrawable(getResources().getDrawable(R.drawable.shot2success));
        }
        shoot.setVisibility(View.GONE);
        successShot.setVisibility(View.VISIBLE);
        successAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.scaleimage);
        successShot.setAnimation(successAnimation);
        successAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                oneTimer = new OneSecondTimer(1000, 1000);
                oneTimer.start();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void tutorialCompleted() {
        shoot.setVisibility(View.GONE);
        successShot.setVisibility(View.VISIBLE);
        successShot.setImageDrawable(getResources().getDrawable(R.drawable.tutorialcompleted));
        successAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.scaleimage);
        successShot.setAnimation(successAnimation);
        successAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                oneTimer = new OneSecondTimer(1000, 1000);
                oneTimer.start();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onShoot(Direction dir) {
        if (timelefttoshoot.getVisibility() != View.VISIBLE) {
            return;
        }
        onPause();
        Log.i(Constants.TAG, "Shot detected");
        switch ((int)mGameTime) {
            case (int)MEDIUM_TIME:
                if (!shotsDetected[0]) {
                    mHintTimer.cancel();
                    if (mGameTime == MEDIUM_TIME && mTimeSince < FAST_TIME) {
                        shoot.setText("YOU ARE TAKING THE SHOT TOO EARLY. WAIT AND THEN SHOOT");
                        retry.setVisibility(View.VISIBLE);
                    } else if (mGameTime == MEDIUM_TIME && mTimeSince < MEDIUM_TIME) {
                        shotsDetected[0] = true;
                        nextShot(1);
                    }
                }
                break;
            case (int)SLOW_TIME:
                if (!shotsDetected[1]) {
                    mHintTimer.cancel();
                    if (mGameTime == SLOW_TIME && mTimeSince < FAST_TIME) {
                        shoot.setText("YOU ARE TAKING THE SHOT TOO EARLY. WAIT AND THEN SHOOT");
                        retry.setVisibility(View.VISIBLE);
                    } else if (mGameTime == SLOW_TIME && mTimeSince < MEDIUM_TIME) {
                        shoot.setText("ALMOST THERE. WAIT A LITTLE MORE BEFORE TAKING THE SHOT");
                        retry.setVisibility(View.VISIBLE);
                    } else if (mGameTime == SLOW_TIME && mTimeSince < SLOW_TIME) {
                        shotsDetected[1] = true;
                        nextShot(2);
                    }
                }
                break;
            case (int)FAST_TIME:
                if (!shotsDetected[2]) {
                    if (mGameTime == FAST_TIME && mTimeSince < FAST_TIME) {
                        mHintTimer.cancel();
                        shotsDetected[2] = true;
                        tutorialCompleted();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();
        if (resourceId == R.id.tutorial_shoot_lets_try) {
            count = 3;
            onViewCreated(mView, null);
            retry.setVisibility(View.INVISIBLE);
        }
    }
}
