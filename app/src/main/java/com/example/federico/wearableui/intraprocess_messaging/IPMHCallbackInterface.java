package com.example.federico.wearableui.intraprocess_messaging;

/**
 * @author Federico Giannoni
 */

import android.support.v7.app.AppCompatActivity;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * Interface that defines the callbacks used to handle the messages received by the {@link IntraProcessMessageHandler}.
 */
public abstract class IPMHCallbackInterface extends AppCompatActivity {

    /**
     * Callback used to redraw the {@link com.example.federico.wearableui.viewport.drawable_content.cursor.Cursor}
     * on the {@link com.example.federico.wearableui.viewport.Viewport}.
     */
    protected abstract void redrawCursor();

    /**
     * Callback used to redraw the {@link com.example.federico.wearableui.viewport.Viewport}.
     */
    protected abstract void redrawViewport();

    /**
     * Callback used to redraw the {@link com.example.federico.wearableui.viewport.drawable_content.cursor.Cursor} on
     * the {@link com.example.federico.wearableui.viewport.Viewport} in the center of the {@link com.example.federico.wearableui.viewport.Viewport.FieldOfView}.
     */
    protected abstract void resetCursorPosition();

    /**
     * Callback called as soon as the calibration phase of the {@link com.example.federico.wearableui.services.imu_handling.SensorFusionService}
     * has started.
     */
    protected abstract void onGazeCalibrationStarted();

    /**
     * Callback called after 80% of the calibration phase of the {@link com.example.federico.wearableui.services.imu_handling.SensorFusionService}
     * has passed.
     */
    protected abstract void onGazeCalibrationWillFinish();

    /**
     * Callback called as soon as the calibration phase of the {@link com.example.federico.wearableui.services.imu_handling.SensorFusionService}
     * has finished.
     * @param calibration the {@link Quaternion} representing the starting orientation of the device, expressed according to the World Coordinate System.
     */
    protected abstract void onGazeCalibrationFinished(final Quaternion calibration);

    /**
     * Callback called each time the orientation of the device is recalculated by a {@link com.example.federico.wearableui.services.imu_handling.ImuHandlerService}
     * (or one of its subclasses).
     * @param orientationUpdate a {@link Quaternion} representing the new orientation of the device, expressed according to the World Coordinate System.
     */
    protected abstract void onGazeOrientationUpdate(final Quaternion orientationUpdate);

    /**
     * Callback called each time a new {@link com.example.federico.wearableui.model.finger.Finger} orientation has been received by
     * the device.
     * @param orientationUpdate a {@link Quaternion} representing the new Finger orientation.
     */
    protected abstract void onFingerOrientationUpdate(final Quaternion orientationUpdate);

    /**
     * Callback called each time a new starting orientation for the {@link com.example.federico.wearableui.model.finger.Finger} has been received by the device.
     * @param calibration a {@link Quaternion} representing the starting orientation for the {@link com.example.federico.wearableui.model.finger.Finger}
     */
    protected abstract void onFingerCalibrationReceived(final Quaternion calibration);

    /**
     * Callback called each time a new click command has been received by the device.
     */
    protected abstract void onCursorClickCommandReceived();

    /**
     * Callback called each time a new lock-unlock command has been received by the device.
     */
    protected abstract void onLockUnlockCommandReceived();

}
