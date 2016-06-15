package com.example.federico.wearableui.model.gaze;

import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Message;

import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessage;
import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessageHandler;
import com.example.federico.wearableui.representation.Matrixf4x4;
import com.example.federico.wearableui.representation.Quaternion;

/**
 * @author Federico Giannoni
 */

/**
 * This class models the user's Gaze, by keeping track of its orientation. Such orientation is used to scroll
 * the {@link com.example.federico.wearableui.viewport.Viewport} accordingly.
 *
 * This class is also a Singleton, since there only has to be one instance of it.
 */
public class Gaze implements IGaze {

    /**
     * The Singleton instance.
     */
    private static IGaze INSTANCE = null;

    /**
     * Quaternion representing the Gaze orientation based on the World Coordinate System.
     */
    private Quaternion wcsBasedOrientation;
    /**
     * Quaternion representing the starting Gaze orientation based on the World Coordinate System.
     */
    private Quaternion calibration;

    /**
     * Pitch angle of the Gaze from the orientation expressed by the calibration field.
     */
    private float gazePitch;
    /**
     * Yaw angle of the Gaze from the orientation expressed by the calibration field.
     */
    private float gazeYaw;

    /**
     * Helper function to obtain the device pitch from a given orientation, expressed relative to the world
     * coordinate system. This is done by remapping such coordinate system to the device's coordinate system.
     *
     * @param orientation the orientation of the device expressed in the rotation vector coordinate system
     * @return the pitch of the device
     */
    private float getDevicePitch(final Quaternion orientation) {
        final Matrixf4x4 rm = new Matrixf4x4();
        // Get the rotation matrix from the given quaternion
        SensorManager.getRotationMatrixFromVector(rm.getMatrix(), orientation.ToArray());
        final Matrixf4x4 rmRemapped = new Matrixf4x4();
        // Remap the axes to the following, so that our pitch will be relative to the -x axis of the device
        // (tangential to the screen and pointing left) - therefore a pitch > 0 will indicate a rotation towards
        // the sky, a pitch < 0 will indicate a rotation towards the ground.
        // The y axis is remapped to the x axis so that the roll will be the angle around the z axis of the device
        // (coming out of the screen towards the user) - this is not used for now.
        SensorManager.remapCoordinateSystem(rm.getMatrix(), SensorManager.AXIS_MINUS_X, SensorManager.AXIS_Z,
                rmRemapped.getMatrix());

        float[] angles = new float[3];
        SensorManager.getOrientation(rmRemapped.getMatrix(), angles);

        return angles[1];
    }

    private void computeWearersPitchAndYaw() {

        // This task computes the pitch and yaw of the device relative to the device frame of reference
        // (not the world coordinate system that Android uses) and then calls for the viewport to be
        // invalidated and redrawn
        new AsyncTask<Void, Float, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                // Calculate rotation by multiplying the new wcsBasedOrientation quaternion by the inverse of the starting orientation, which
                // is our calibration quaternion
                final Quaternion startingDirection = calibration.clone();
                startingDirection.inverse();
                final Quaternion finishingDirection = new Quaternion();
                wcsBasedOrientation.multiplyByQuat(startingDirection, finishingDirection);

                // Now we need to calculate the pitch - this has to be computed relative to the device so that
                // our zero is relative to the device position - to do this we have to remap the coordinate system
                // or else we would be using the world coordinate system which is EAST(x) - NORTH(y) - UP(z) based
                final float deltaPitch = getDevicePitch(wcsBasedOrientation) - getDevicePitch(calibration);

                gazePitch = (float) Math.toDegrees(deltaPitch);
                gazeYaw = (float) Math.toDegrees(finishingDirection.getYaw());

                Message.obtain(IntraProcessMessageHandler.getInstance(), IntraProcessMessage.REDRAW_VIEWPORT.getMessageCode()).sendToTarget();

                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * Returns the Singleton instance.
     * @return the Singleton instance.
     */
    public static IGaze getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Gaze();
        }

        return INSTANCE;
    }

    /**
     * Constructor.
     */
    private Gaze() {
        this.wcsBasedOrientation = new Quaternion();
    }

    @Override
    public void updateGazeOrientation(final Quaternion orientation) {
        // Every time a new wcsBasedOrientation is set for the user's gaze, this method also automatically
        // computes the user's gaze pitch and yaw angles in degrees relative to the device.
        // Pitch will be the angle around the device -x axis, while yaw will be the angle around the axis
        // that points to the ground, where the zero is given by the yaw calculated from the calibration
        // quaternion.
        this.wcsBasedOrientation = orientation;
        this.computeWearersPitchAndYaw();
    }

    @Override
    public void calibrate(final Quaternion calibration) {
        this.calibration = calibration;
    }

    @Override
    public Quaternion getOrientationRelativeToWCS() {
        return this.wcsBasedOrientation;
    }

    @Override
    public Quaternion getCurrentCalibration() {
        return this.calibration;
    }

    @Override
    public float getGazePitch() {
        return this.gazePitch;
    }

    @Override
    public float getGazeYaw() {
        return this.gazeYaw;
    }
}
