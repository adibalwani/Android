package edu.neu.madcourse.adibalwani.trickiestpart;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ShootActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mGravity;
    private Sensor mAcceleration;
    private Sensor mRotation;
    private ShootDetector mShootDetector;
    private TextView mShoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoot);
        mShoot = (TextView) findViewById(R.id.trickiest_part_shoot_count);
        mShoot.setText(getResources().getString(R.string.trickiest_part_waiting_for_shoot));
        this.setTitle(getResources().getString(R.string.trickiest_part_titlebar_name));
    }

    /**
     * Register the sensor manager to listen to dribble events
     */
    private void registerSensorEvent() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGravity = mSensorManager
                .getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAcceleration = mSensorManager
                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotation = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mShootDetector = new ShootDetector();
        mShootDetector.setShootListener(new ShootDetector.OnShootListener() {
            @Override
            public void onShoot(ShootDetector.Direction dir) {
                mShoot.setText(dir.toString());
                //mShoot.setText(getResources().getString(R.string.trickiest_part_shot_taken));
            }
        });
        mSensorManager.registerListener(
                mShootDetector,
                mGravity,
                SensorManager.SENSOR_DELAY_NORMAL
        );
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
    protected void onStart() {
        super.onStart();
        registerSensorEvent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensorEvent();
    }
}
