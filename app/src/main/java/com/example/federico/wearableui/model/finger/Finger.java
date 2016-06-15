package com.example.federico.wearableui.model.finger;

import android.os.AsyncTask;
import android.os.Message;

import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessage;
import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessageHandler;
import com.example.federico.wearableui.representation.Quaternion;

/**
 * @author Federico Giannoni
 */

/**
 * This class models the user's Finger, by keeping track of its orientation. Such orientation is used as
 * an input source for the {@link com.example.federico.wearableui.viewport.drawable_content.cursor.Cursor},
 * that can be moved around on the {@link com.example.federico.wearableui.viewport.Viewport} accordingly to it.
 *
 * This class is also a Singleton, since there only has to be one instance of it.
 */
public class Finger implements  IFinger {

    /**
     * The Singleton instance.
     */
    private static IFinger INSTANCE = null;

    /**
     * The orientation of the Finger supplied via Bluetooth communication. This is not relative to the user's coordinate system.
     */
    private Quaternion suppliedOrientation;
    /**
     * The orientation of the Finger expressed in the user's coordinate system.
     */
    private Quaternion orientation;
    /**
     * The orientation taken as the starting orientation of the Finger. This is not expressed in the user's coordinate system.
     * Since both this and the suppliedOrientations are expressed in the same coordinate system though, by calculating the difference
     * between the two, we can obtain an orientation that is relative to the user's coordinate system.
     */
    private Quaternion calibration;

    /**
     * The pitch angle of the Finger relative to the user's coordinate system. The angle is referred to the starting orientation
     * (calibration).
     */
    private float fingerPitch;
    /**
     * The yaw angle of the Finger relative to the user's coordinate system. The angle is referred to the starting orientation
     * (calibration).
     */
    private float fingerYaw;

    /**
     * Computes, on an AsyncTask, the current Finger orientation relative to the user's coordinate system.
     */
    private void computeCurrentOrientation() {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    final Quaternion startingDirection = calibration.clone();
                    startingDirection.inverse();

                    suppliedOrientation.multiplyByQuat(startingDirection, orientation);

                    Finger.this.fingerPitch = (float) Math.toDegrees(orientation.getPitch());
                    //TODO: remove the (-1) in the future, it's needed right now for the Myo part.
                    Finger.this.fingerYaw = (float) Math.toDegrees(orientation.getYaw()) * (-1);

                    Message.obtain(IntraProcessMessageHandler.getInstance(), IntraProcessMessage.REDRAW_CURSOR.getMessageCode()).sendToTarget();

                    return null;
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * Returns the Singleton instance.
     * @return the Singleton instance.
     */
    public static IFinger getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Finger();
        }

        return INSTANCE;
    }

    /**
     * Constructor.
     */
    private Finger() {
        this.suppliedOrientation = new Quaternion();
        this.orientation = new Quaternion();
    }

    @Override
    public void updateOrientation(final Quaternion orientationUpdate) {
        this.suppliedOrientation = orientationUpdate;
        this.computeCurrentOrientation();
    }

    @Override
    public void calibrate(final Quaternion calibration) {
        this.calibration = calibration;
    }

    @Override
    public Quaternion getOrientation() {
        return this.orientation;
    }

    @Override
    public Quaternion getCurrentCalibration() {
        return this.calibration;
    }

    @Override
    public float getFingerPitch() {
        return this.fingerPitch;
    }

    @Override
    public float getFingerYaw() {
        return this.fingerYaw;
    }

}
