package edu.neu.madcourse.adibalwani.finalproject.tutorial;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import edu.neu.madcourse.adibalwani.finalproject.Constants;

/**
 * @author rachit on 19-04-2016.
 */
public class HoldPhoneDetector implements SensorEventListener {

    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationVals = new float[3];
    private static final int X_ROT_MIN = 50;
    private static final int X_ROT_MAX = 90;
    private static final int Y_ROT_MIN = 135;
    private static final int Y_ROT_MAX = 180;

    private OnHoldListener mHoldListener;

    @Override
    public void onSensorChanged(SensorEvent event) {
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
        if (x > X_ROT_MIN && x < X_ROT_MAX && y > Y_ROT_MIN && y < Y_ROT_MAX) {
            Log.i(Constants.TAG, "Holding phone correctly");
            mHoldListener.onHold(true);
        } else {
            Log.i(Constants.TAG, "Not holding phone correctly");
            mHoldListener.onHold(false);
        }
    }

    public interface OnHoldListener {
        /**
         * Method to call on a successful phone hold
         */
        void onHold(boolean holding);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setHoldListener(OnHoldListener holdListener) {
        mHoldListener = holdListener;
    }
}
