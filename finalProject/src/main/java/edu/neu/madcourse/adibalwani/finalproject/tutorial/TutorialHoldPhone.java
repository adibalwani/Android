package edu.neu.madcourse.adibalwani.finalproject.tutorial;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.neu.madcourse.adibalwani.finalproject.R;
import edu.neu.madcourse.adibalwani.finalproject.TextViewFont;

/**
 * @author rachit on 19-04-2016.
 */
public class TutorialHoldPhone extends Fragment implements HoldPhoneDetector.OnHoldListener {

    private TextView holdMessage;
    private Animation holdphone;
    private Animation successAnimation;
    private ImageView holdphoneImage;
    private ImageView successImage;
    private LinearLayout rootView;

    private FragmentManager frag = null;
    private Timer oneTimer;
    private Sensor mRotationVector;
    private SensorManager mSensorManager;
    private HoldPhoneDetector mHoldDetector;
    private boolean holdSuccessfully = false;
    private CountDownTimer mTimer;
    private static final int HOLD_PHONE_DURATION_TIME = 3000;
    private int count = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_hold_phone, container, false);
        initViews(view);
        initSensorInstances();
        startTimer();
        return view;
    }

    private void initViews(View view) {
        rootView = (LinearLayout) view.findViewById(R.id.root_view_fragment_tutorial_hold_phone);
        holdMessage = (TextView) view.findViewById(R.id.tutorialholdmessage);
        holdphoneImage = (ImageView) view.findViewById(R.id.tutorial_hold_phone_image);
        successImage = (ImageView) view.findViewById(R.id.tutorial_hold_phone_accomplishment);
        TextViewFont font = new TextViewFont();
        font.overrideFonts(getActivity().getApplicationContext(), rootView);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        holdphone = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
        successAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.scaleimage);
    }

    private void initSensorInstances() {
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mHoldDetector = new HoldPhoneDetector();
        mHoldDetector.setHoldListener(this);
        mSensorManager.registerListener(
                mHoldDetector,
                mRotationVector,
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    private void startTimer() {
        mTimer = new CountDownTimer(HOLD_PHONE_DURATION_TIME, HOLD_PHONE_DURATION_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Do  Nothing
            }

            @Override
            public void onFinish() {
                nextTutorial();
            }
        };
    }

    private void nextTutorial() {
        holdphoneImage.setVisibility(View.GONE);
        successImage.setVisibility(View.VISIBLE);
        successImage.setAnimation(successAnimation);
        successAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                oneTimer = new Timer(1000, 1000);
                oneTimer.start();
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
            frag.beginTransaction().replace(R.id.tutorial_fragment, new TutorialDribbling()).commit();
        }
    }

    private void unregisterSensor() {
        mSensorManager.unregisterListener(mHoldDetector);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (oneTimer != null) {
            oneTimer.cancel();
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        // TODO: Handle Home press key. Unregister on Home key too
        unregisterSensor();
    }

    @Override
    public void onHold(boolean holding) {
        if (!holding && mTimer != null) {
            holdSuccessfully = holding;
            mTimer.cancel();
            count++;
        } else if (holding && !holdSuccessfully) {
            holdSuccessfully = holding;
            mTimer.start();
        }
        if (count > 50) {
            holdMessage.setVisibility(View.VISIBLE);
            holdMessage.setAnimation(holdphone);
        }
    }
}
