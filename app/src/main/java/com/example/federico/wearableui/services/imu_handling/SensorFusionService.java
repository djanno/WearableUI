package com.example.federico.wearableui.services.imu_handling;

/**
 * Created by Federico on 22/04/2016.
 */

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessage;
import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessageHandler;
import com.example.federico.wearableui.representation.Quaternion;

/**
 * @author Federico Giannoni
 * @author A. Pacha
 */
public class SensorFusionService extends ImuHandlerService {

    /**
     * Constant specifying the factor between a Nano-second and a second
     */
    private static final float NS2S = 1.0f / 1000000000.0f;

    /**
     * The quaternion that stores the difference that is obtained by the gyroscope.
     * Basically it contains a rotational difference encoded into a quaternion.
     *
     * To obtain the absolute orientation one must add this into an initial position by
     * multiplying it with another quaternion
     */
    private final Quaternion deltaQuaternion = new Quaternion();

    /**
     * The Quaternions that contain the current rotation (Angle and axis in Quaternion format) of the Gyroscope
     */
    private Quaternion quaternionGyroscope = new Quaternion();

    /**
     * The quaternion that contains the absolute orientation as obtained by the rotationVector sensor.
     */
    private Quaternion quaternionRotationVector = new Quaternion();

    /**
     * The time-stamp being used to record the time when the last gyroscope event occurred.
     */
    private long timestamp = 0;

    /**
     * The time-stamp used for calibration
     */
    private long calibrationTimestamp = 0;

    /**
     * This is a filter-threshold for discarding Gyroscope measurements that are below a certain level and
     * potentially are only noise and not real motion. Values from the gyroscope are usually between 0 (stop) and
     * 10 (rapid rotation), so 0.1 seems to be a reasonable threshold to filter noise (usually smaller than 0.1) and
     * real motion (usually > 0.1). Note that there is a chance of missing real motion, if the use is turning the
     * device really slowly, so this value has to find a balance between accepting noise (threshold = 0) and missing
     * slow user-action (threshold > 0.5). 0.1 seems to work fine for most applications.
     *
     */
    private static final double EPSILON = 0.1f;

    /**
     * Value giving the total velocity of the gyroscope (will be high, when the device is moving fast and low when
     * the device is standing still). This is usually a value between 0 and 10 for normal motion. Heavy shaking can
     * increase it to about 25. Keep in mind, that these values are time-depended, so changing the sampling rate of
     * the sensor will affect this value!
     */
    private double gyroscopeRotationVelocity = 0;

    /**
     * Counter that sums the number of consecutive frames, where the rotationVector and the gyroscope were
     * significantly different (and the dot-product was smaller than 0.7). This event can either happen when the
     * angles of the rotation vector explode (e.g. during fast tilting) or when the device was shaken heavily and
     * the gyroscope is now completely off.
     */
    private int panicCounter;

    /**
     * This weight determines directly how much the rotation sensor will be used to correct (in
     * Sensor-fusion-scenario 1 - SensorSelection.GyroscopeAndRotationVector). Must be a value between 0 and 1.
     * 0 means that the system entirely relies on the gyroscope, whereas 1 means that the system relies entirely on
     * the rotationVector.
     */
    private static final float DIRECT_INTERPOLATION_WEIGHT = 0.005f;

    /**
     * The threshold that indicates an outlier of the rotation vector. If the dot-product between the two vectors
     * (gyroscope orientation and rotationVector orientation) falls below this threshold (ideally it should be 1,
     * if they are exactly the same) the system falls back to the gyroscope values only and just ignores the
     * rotation vector.
     *
     * This value should be quite high (> 0.7) to filter even the slightest discrepancies that causes jumps when
     * tiling the device. Possible values are between 0 and 1, where a value close to 1 means that even a very small
     * difference between the two sensors will be treated as outlier, whereas a value close to zero means that the
     * almost any discrepancy between the two sensors is tolerated.
     */
    private static final float OUTLIER_THRESHOLD = 0.85f;

    /**
     * The threshold that indicates a massive discrepancy between the rotation vector and the gyroscope orientation.
     * If the dot-product between the two vectors
     * (gyroscope orientation and rotationVector orientation) falls below this threshold (ideally it should be 1, if
     * they are exactly the same), the system will start increasing the panic counter (that probably indicates a
     * gyroscope failure).
     *
     * This value should be lower than OUTLIER_THRESHOLD (0.5 - 0.7) to only start increasing the panic counter,
     * when there is a
     * huge discrepancy between the two fused sensors.
     */
    private static final float OUTLIER_PANIC_THRESHOLD = 0.65f;

    /**
     * The threshold that indicates that a chaos state has been established rather than just a temporary peak in the
     * rotation vector (caused by exploding angled during fast tilting).
     *
     * If the chaosCounter is bigger than this threshold, the current position will be reset to whatever the
     * rotation vector indicates.
     */
    private static final int PANIC_THRESHOLD = 60;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
        this.registerSensors();
        Log.i("Sensor Fusion Service", "created");
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Resetting calibration phase indicator, just to be sure
        calibrationState = CalibrationState.WILL_START;
        Log.i("Sensor Fusion Service", "destroyed");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // Process rotation vector (just safe it)

            float[] q = new float[4];
            // Calculate angle. Starting with API_18, Android will provide this value as event.values[3], but if not, we have to calculate it manually.
            SensorManager.getQuaternionFromVector(q, event.values);

            // Store in quaternion
            quaternionRotationVector.setXYZW(q[1], q[2], q[3], -q[0]);
            if (calibrationState.equals(CalibrationState.WILL_START)) {
                // Position initialised
                quaternionGyroscope.set(quaternionRotationVector);
                calibrationState = CalibrationState.STARTED;
                calibrationTimestamp = event.timestamp;
                messageForIPMHandler(IntraProcessMessage.GAZE_CALIBRATION_STARTED).sendToTarget();
            }

        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE && !calibrationState.equals(CalibrationState.WILL_START)) {
            // Process Gyroscope and perform fusion

            // This timestep's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
            if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                // Calculate the angular speed of the sample
                gyroscopeRotationVelocity = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                if (gyroscopeRotationVelocity > EPSILON) {
                    axisX /= gyroscopeRotationVelocity;
                    axisY /= gyroscopeRotationVelocity;
                    axisZ /= gyroscopeRotationVelocity;
                }

                // Integrate around this axis with the angular speed by the timestep
                // in order to get a delta rotation from this sample over the timestep
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                double thetaOverTwo = gyroscopeRotationVelocity * dT / 2.0f;
                double sinThetaOverTwo = Math.sin(thetaOverTwo);
                double cosThetaOverTwo = Math.cos(thetaOverTwo);
                deltaQuaternion.setX((float) (sinThetaOverTwo * axisX));
                deltaQuaternion.setY((float) (sinThetaOverTwo * axisY));
                deltaQuaternion.setZ((float) (sinThetaOverTwo * axisZ));
                deltaQuaternion.setW(-(float) cosThetaOverTwo);

                // Move current gyro orientation
                deltaQuaternion.multiplyByQuat(quaternionGyroscope, quaternionGyroscope);

                // Calculate dot-product to calculate whether the two orientation sensors have diverged
                // (if the dot-product is closer to 0 than to 1), because it should be close to 1 if both are the same.
                float dotProd = quaternionGyroscope.dotProduct(quaternionRotationVector);

                // If they have diverged, rely on gyroscope only (this happens on some devices when the rotation vector "jumps").
                if (Math.abs(dotProd) < OUTLIER_THRESHOLD) {
                    // Increase panic counter
                    if (Math.abs(dotProd) < OUTLIER_PANIC_THRESHOLD) {
                        panicCounter++;
                    }

                    // Directly use Gyro
                    setOrientationQuaternion(quaternionGyroscope);

                } else {
                    // Both are nearly saying the same. Perform normal fusion.

                    // Interpolate with a fixed weight between the two absolute quaternions obtained from gyro and rotation vector sensors
                    // The weight should be quite low, so the rotation vector corrects the gyro only slowly, and the output keeps responsive.
                    Quaternion interpolate = new Quaternion();
                    quaternionGyroscope.slerp(quaternionRotationVector, interpolate, DIRECT_INTERPOLATION_WEIGHT);

                    // Use the interpolated value between gyro and rotationVector
                    setOrientationQuaternion(interpolate);
                    // Override current gyroscope-orientation
                    quaternionGyroscope.copyVec4(interpolate);

                    // Reset the panic counter because both sensors are saying the same again
                    panicCounter = 0;
                }

                if (panicCounter > PANIC_THRESHOLD) {
                    Log.d("Rotation Vector",
                            "Panic counter is bigger than threshold; this indicates a Gyroscope failure. Panic reset is imminent.");

                    if (gyroscopeRotationVelocity < 3) {
                        Log.d("Rotation Vector",
                                "Performing Panic-reset. Resetting orientation to rotation-vector value.");

                        // Restart calibration phase
                        calibrationState = CalibrationState.WILL_START;
                        calibrationTimestamp = event.timestamp;

                        panicCounter = 0;

                    } else {
                        Log.d("Rotation Vector",
                                String.format(
                                        "Panic reset delayed due to ongoing motion (user is still shaking the device). Gyroscope Velocity: %.2f > 3",
                                        gyroscopeRotationVelocity));
                    }
                }
            }
            timestamp = event.timestamp;
        }
    }

    /**
     * Sets the output quaternion and matrix with the provided quaternion and synchronises the setting
     *
     * @param quaternion The Quaternion to set (the result of the sensor fusion)
     */
    private void setOrientationQuaternion(Quaternion quaternion) {
        synchronized (syncToken) {
            // Use gyro only
            currentOrientationQuaternion.copyVec4(quaternion);
        }
        // Every time we set the current orientation, we communicate it
        this.communicateChanges(quaternion);
    }

    private void communicateChanges(final Quaternion quaternion) {
        final Quaternion correctedQuat = quaternion.clone();
        // We inverted w in the deltaQuaternion, because currentOrientationQuaternion required it.
        // Before converting it back to matrix representation, we need to revert this process
        correctedQuat.w(-correctedQuat.w());
        // If the calibration phase is over we communicate so
        if(timestamp - calibrationTimestamp >= CALIBRATION_PERIOD) {
            if(!calibrationState.equals(CalibrationState.FINISHED)) {
                // We take the current orientation as our zero
                final Message calibrationMessage = this.messageForIPMHandler(IntraProcessMessage.GAZE_CALIBRATION_FINISHED);
                final Bundle bundle = new Bundle();
                bundle.putParcelable(IntraProcessMessage.GAZE_CALIBRATION_FINISHED.getValueKey(), correctedQuat);
                calibrationMessage.setData(bundle);
                calibrationMessage.sendToTarget();
                calibrationState = CalibrationState.FINISHED;
            }
            final Message orientationMessage = this.messageForIPMHandler(IntraProcessMessage.GAZE_ORIENTATION_UPDATE);
            final Bundle bundle = new Bundle();
            bundle.putParcelable(IntraProcessMessage.GAZE_ORIENTATION_UPDATE.getValueKey(), correctedQuat);
            orientationMessage.setData(bundle);
            orientationMessage.sendToTarget();
        }
        // Else, if the calibration phase one is over, we communicate that the calibration is about to end
        else if(timestamp - calibrationTimestamp >= CALIBRATION_PERIOD * 0.8 && calibrationState.equals(CalibrationState.STARTED)) {
            this.messageForIPMHandler(IntraProcessMessage.GAZE_CALIBRATION_WILL_FINISH).sendToTarget();
            calibrationState = CalibrationState.WILL_FINISH;
        }
    }

    /**
     * Creates a {@link Message} that reflects the passed {@link IntraProcessMessage}.
     * @param message an {@link IntraProcessMessage} to use as base to create the {@link Message} that will be sent to the
     *                {@link IntraProcessMessageHandler}.
     * @return a {@link Message} to be sent to the {@link IntraProcessMessageHandler}.
     */
    private Message messageForIPMHandler(final IntraProcessMessage message) {
        return Message.obtain(IntraProcessMessageHandler.getInstance(), message.getMessageCode());
    }

}