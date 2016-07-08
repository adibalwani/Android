package edu.neu.madcourse.adibalwani.finalproject.gamelogic;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Class used to detect dribble motion
 */
public class DribbleDetector implements SensorEventListener {

    // Dribble Parameters
    private static final int COUNT_THRESHOLD = 2;
    private static final int Z_ACCEL_MIN = 9;
    private static final int X_ROT_MIN = 50;
    private static final int X_ROT_MAX = 90;
    private static final int Y_ROT_MIN = 135;
    private static final int Y_ROT_MAX = 180;
    private static final String LOG_TAG = DribbleDetector.class.getSimpleName();

    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationVals = new float[3];
    private boolean mZAccel = false;
    private boolean down = false;

    private OnDribbleListener mDribbleListener;
    private int mCount = 0;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do Nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if (!mZAccel) {
                    processAccelerometerData(event);
                }
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                if (mZAccel) {
                    processRotationData(event);
                }
                break;
            default:
                break;
        }
    }

    private void processAccelerometerData(SensorEvent event) {
        float z = event.values[2];
        Log.i(LOG_TAG, "Acceleration: " + z);
        if (!down && z < -Z_ACCEL_MIN) {
            mCount++;
            if (mCount > COUNT_THRESHOLD) {
                down = true;
                mCount = 0;
            }
        } else if (down && z > Z_ACCEL_MIN) {
            mCount++;
            if (mCount > COUNT_THRESHOLD) {
                down = false;
                mZAccel = true;
                mCount = 0;
            }
        } else {
            mCount = 0;
        }
    }

    private void processRotationData(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
        SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,
                SensorManager.AXIS_Z, mRotationMatrix);
        SensorManager.getOrientation(mRotationMatrix, mOrientationVals);

        mOrientationVals[0] = (float) Math.toDegrees(mOrientationVals[0]);
        mOrientationVals[1] = (float) Math.toDegrees(mOrientationVals[1]);
        mOrientationVals[2] = (float) Math.toDegrees(mOrientationVals[2]);

        float x = mOrientationVals[1];
        float y = Math.abs(mOrientationVals[2]);
        float z = Math.abs(mOrientationVals[0]);
        if (x > X_ROT_MIN && x < X_ROT_MAX && y > Y_ROT_MIN && y < Y_ROT_MAX && mZAccel) {
            mDribbleListener.onDribble();
        } else if (!(x > X_ROT_MIN && x < X_ROT_MAX && y > Y_ROT_MIN && y < Y_ROT_MAX)) {
            mCount = 0;
        }
        mZAccel = false;
    }

    public interface OnDribbleListener {
        /**
         * Method to call on a successful dribble
         */
        void onDribble();
    }

    public void setDribbleListener(OnDribbleListener dribbleListener) {
        mDribbleListener = dribbleListener;
    }
}
