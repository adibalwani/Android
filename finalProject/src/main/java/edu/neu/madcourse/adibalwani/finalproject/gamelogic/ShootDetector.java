package edu.neu.madcourse.adibalwani.finalproject.gamelogic;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.google.common.collect.EvictingQueue;

/**
 * Class used to detect shot along with its' direction
 */
public class ShootDetector implements SensorEventListener {

    private static final String TAG = ShootDetector.class.getSimpleName();

    private OnShootListener mShootListener;
    private static final int Z_MIN = -15;
    private static final int Z_MAX = 15;
    private static final int Y_MIN = -15;
    private static final int Y_MAX = 12;
    private boolean aboutToShoot = false;
    EvictingQueue<Float> historyX = EvictingQueue.create(25);
    EvictingQueue<Float> historyY = EvictingQueue.create(15);
    EvictingQueue<Float> historyZ = EvictingQueue.create(15);
    EvictingQueue<Direction> historyDirection = EvictingQueue.create(10);
    private Direction dir = Direction.CENTER;

    float[] mRotationMatrix = new float[9];
    float[] orientationVals = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if (aboutToShoot) {
                    processAccelerometerData(event);
                }
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                if (!aboutToShoot) {
                    processRotationData(event);
                }
                break;
            default:
                break;
        }
    }

    public void processRotationData(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(mRotationMatrix,event.values);
        SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,
                SensorManager.AXIS_Z, mRotationMatrix);
        SensorManager.getOrientation(mRotationMatrix, orientationVals);

        orientationVals[0] = (float) Math.toDegrees(orientationVals[0]);
        orientationVals[1] = (float) Math.toDegrees(orientationVals[1]);
        orientationVals[2] = (float) Math.toDegrees(orientationVals[2]);

        float x = orientationVals[1];
        float y = orientationVals[2];

        if (x > -40 && x < 40 && y > 10 && y < 180) {
            Log.i(TAG, "Shoot detected");
            aboutToShoot = true;
            return;
        }
        aboutToShoot = false;
    }

    public void processAccelerometerData(SensorEvent event) {
        dir = Direction.CENTER;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        Log.i(TAG, "Acc : (x,y,z) :" + "[" + x + " , " + y + " , " + z + "]");

        if (historyX.remainingCapacity() == 0) {
            float sum = 0;
            for (float f : historyZ) {
                sum += f;
            }
            float avgz = sum / (float) 15;
            Log.i(TAG, "Avergagez: " + avgz);

            sum = 0;
            for (float f : historyY) {
                sum += f;
            }
            float avgy = sum / (float) 15;
            Log.i(TAG, "Avergagey: " + avgy);
            Log.i(TAG, "Avergagez: " + avgz + "Avergagey: " + avgy + "Root" + Math.sqrt(avgy * avgy + avgz * avgz));

            // Process for check
            boolean forward = false;
            boolean up = false;
            boolean nextPhase = false;
            int count = 0;
            float zMin = 0;
            float zMax = 0;
            for (float f : historyZ) {
                zMax = Math.max(zMax, f);
                zMin = Math.min(zMin, f);
                if (f < -9 && !nextPhase) {
                    count++;
                    if (count > 2) {
                        nextPhase = true;
                        count = 0;
                    }
                } else if (f > 9 && nextPhase) {
                    count++;
                    if (count > 2) {
                        Log.i(TAG, "Forward Motion detected");
                        forward = true;
                    }
                } else {
                    count = 0;
                }
            }
            nextPhase = false;
            count = 0;
            float yMin = 0;
            float yMax = 0;

            for (float f : historyY) {
                yMax = Math.max(yMax, f);
                yMin = Math.min(yMin, f);
                if (f > 4 && !nextPhase) {
                    count++;
                    if (count > 2) {
                        nextPhase = true;
                        count = 0;
                    }
                } else if (f < -8 && nextPhase) {
                    //if (f < -8) {
                        count++;
                        if (count > 2) {
                            Log.i(TAG, "Up Motion detected");
                            up = true;
                        }
                    } else {
                        count = 0;
                    }
                //}
            }

            nextPhase = false;
            count = 0;
            for (float f : historyX) {
                if (f > 8 && !nextPhase) {
                    count++;
                    if (count > 1) {
                        nextPhase = true;
                        count = 0;
                    }
                } else if (f < -8 && nextPhase) {
                    count++;
                    if (count > 0) {
                        dir = Direction.RIGHT;
                        Log.i(TAG, "Right Motion detected");
                    }
                } else {
                    count = 0;
                }
            }

            nextPhase = false;
            count = 0;
            for (float f : historyX) {
                if (f < -8 && !nextPhase) {
                    count++;
                    if (count > 1 && dir != Direction.RIGHT) {
                        nextPhase = true;
                        count = 0;
                    }
                } else if (f > 8 && nextPhase) {
                    count++;
                    if (count > 0) {
                        dir = Direction.LEFT;
                        Log.i(TAG, "Left Motion detected");
                    }
                } else {
                    count = 0;
                }
            }

            historyDirection.add(dir);
            if (forward) {
                Log.i(TAG, "ymin and ymax " +yMin +" " +yMax +"");
                if (yMin < -25) {
                    up = true;
                }
            }

            if (up) {
                Log.i(TAG, "zmin and zmax " +zMin +" " +zMax +"");
                if (zMin < -10) {
                    forward = true;
                }
            }

            if (forward && up) {
                //Log.i(TAG, "Detected correct motion " + dir);
                int total = 0;
                for (Direction d : historyDirection) {
                    total += d.ordinal();
                }
                float finalDirection = total / (float) historyDirection.size();
                if (finalDirection < 0.5) {
                    dir = Direction.LEFT;
                } else if (finalDirection < 1.5 && finalDirection > 0.5) {
                    dir = Direction.CENTER;
                } else {
                    dir = Direction.RIGHT;
                }
                Log.i(TAG, "Detected correct motion " + dir);
                mShootListener.onShoot(dir);
                aboutToShoot = false;
            }
        }

        historyX.add(x);
        historyY.add(y);
        historyZ.add(z);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface OnShootListener {
        /**
         * Method to call on a successful dribble
         */
        void onShoot(Direction dir);
    }

    public void setShootListener(OnShootListener dribbleListener) {
        mShootListener = dribbleListener;
    }
}
