package com.example.federico.wearableui.services.imu_handling;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;


import com.example.federico.wearableui.representation.Quaternion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Federico Giannoni
 */
public abstract class ImuHandlerService extends Service implements SensorEventListener {

    /**
     * Enumerator for the calibration states of the Imu.
     */
    protected enum CalibrationState { WILL_START, STARTED, WILL_FINISH, FINISHED }

    /**
     * Constant specifying how many nanoseconds the calibration period will last - a shorter calibration period
     * will produce a more inconsistent calibration
     */
    protected static final float CALIBRATION_PERIOD = 15000000000.0f;

    /**
     * Constant specifying the sampling rate of the sensors in microseconds. Since every time a new
     * orientation is calculated it is supplied to the viewport to be redrawn, this constant also
     * dictates the refresh rate of the viewport - a 33333us sampling rate a.k.a. a 33ms rate,
     * coincides with a 30fps refresh rate of the viewport
     */
    private static final int SAMPLE_RATE_US = 33333;

    /**
     * Flag indicating if the calibration phase has ended.
     */
    protected CalibrationState calibrationState = CalibrationState.WILL_START;

    /**
     * Sync-token for syncing read/write to sensor-data from sensor manager and
     * fusion algorithm
     */
    protected final Object syncToken = new Object();

    /**
     * The list of sensors used by this provider
     */
    protected List<Sensor> sensorList = new ArrayList<>();

    /**
     * The list of handlers for each sensor
     */
    protected List<Handler> handlers = new ArrayList<>();

    /**
     * The quaternion that holds the current rotation
     */
    protected Quaternion currentOrientationQuaternion;

    /**
     * The sensor manager for accessing android sensors
     */
    protected SensorManager sensorManager;

    protected void registerSensors() {
        for (final Sensor sensor : sensorList) {
            // Start a new thread for this sensor
            final HandlerThread handlerThread = new HandlerThread(sensor.getName());
            handlerThread.start();
            // Create a handler for the thread and attach it
            final Handler handler = new Handler(handlerThread.getLooper());
            handlers.add(handler);
            // Register the sensor manager for this sensor - computations will be done on the new handler thread
            // that we've just created
            sensorManager.registerListener(this, sensor, SAMPLE_RATE_US, handler);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        // Initialise with identity
        currentOrientationQuaternion = new Quaternion();
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return new Binder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Make sure to turn our sensors off when the activity is paused
        for (final Sensor sensor : sensorList) {
            // Recover handlers
            final Handler handler = handlers.get(sensorList.indexOf(sensor));
            // Remove all callbacks and messages and close the thread
            handler.removeCallbacksAndMessages(null);
            handler.getLooper().quit();
            // Unregister this listener
            sensorManager.unregisterListener(this, sensor);
        }
        // Clear the handlers list
        handlers.clear();
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // Not doing anything
    }

    /**
     * Service binder class.
     */
    public class Binder extends android.os.Binder {

        /**
         * Asks the Service if the calibration phase has finished.
         * @return true if it has finished, false otherwise.
         */
        public boolean askHasCalibrationPhaseFinished() {
            return calibrationState.equals(CalibrationState.FINISHED);
        }
    }

}
