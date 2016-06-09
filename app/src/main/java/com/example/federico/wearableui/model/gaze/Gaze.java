package com.example.federico.wearableui.model.gaze;

import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Message;

import com.example.federico.wearableui.controller.ViewportActivity;
import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessage;
import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessageHandler;
import com.example.federico.wearableui.representation.Matrixf4x4;
import com.example.federico.wearableui.representation.Quaternion;

/**
 * Created by Federico on 29/04/2016.
 */
public class Gaze implements IGaze {

    private static IGaze INSTANCE = null;

    private Quaternion rvcsBasedOrientation;
    private Quaternion calibration;

    //pitch and yaw relative to the device
    private float gazePitch;
    private float gazeYaw;

    /**
     * Helper function to obtain the device pitch from a given orientation, expressed in the rotation
     * vector coordinate system- this is done by remapping such coordinate system to the device's coordinate system
     *
     * @param orientation the orientation of the device expressed in the rotation vector coordinate system
     * @return the pitch of the device
     */
    private float getDevicePitch(final Quaternion orientation) {
        final Matrixf4x4 rm = new Matrixf4x4();
        //get the rotation matrix from the given quaternion
        SensorManager.getRotationMatrixFromVector(rm.getMatrix(), orientation.ToArray());
        final Matrixf4x4 rmRemapped = new Matrixf4x4();
        //remap the axes to the following, so that our pitch will be relative to the -x axis of the device
        //(tangential to the screen and pointing left) - therefore a pitch > 0 will indicate a rotation towards
        //the sky, a pitch < 0 will indicate a rotation towards the ground
        //the y axis is remapped to the x axis so that the roll will be the angle around the z axis of the device
        //(coming out of the screen towards the user) - this is not used for now
        SensorManager.remapCoordinateSystem(rm.getMatrix(), SensorManager.AXIS_MINUS_X, SensorManager.AXIS_Z,
                rmRemapped.getMatrix());

        float[] angles = new float[3];
        SensorManager.getOrientation(rmRemapped.getMatrix(), angles);

        return angles[1];
    }

    private void computeWearersPitchAndYaw() {

        //this task computes the pitch and yaw of the device relative to the device frame of reference
        //(not the world coordinate system that Android uses) and then calls for the viewport to be
        //invalidated and redrawn
        new AsyncTask<Void, Float, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                //calculate rotation by multiplying the new rvcsBasedOrientation quaternion by the inverse of the "center", which
                //is our calibration quaternion
                final Quaternion startingDirection = calibration.clone();
                startingDirection.inverse();
                final Quaternion finishingDirection = new Quaternion();
                rvcsBasedOrientation.multiplyByQuat(startingDirection, finishingDirection);

                //now we need to calculate the pitch - this has to be computed relative to the device so that
                //our zero is relative to the device position - to do this we have to remap the coordinate system
                //or else we would be using the rotation vector coordinate system which is EAST(x) - NORTH(y) - UP(z) based
                final float deltaPitch = getDevicePitch(rvcsBasedOrientation) - getDevicePitch(calibration);

                gazePitch = (float) Math.toDegrees(deltaPitch);
                gazeYaw = (float) Math.toDegrees(finishingDirection.getYaw());

                Message.obtain(IntraProcessMessageHandler.getInstance(), IntraProcessMessage.REDRAW_VIEWPORT.getMessageCode()).sendToTarget();

                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static IGaze getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Gaze();
        }

        return INSTANCE;
    }

    private Gaze() {
        this.rvcsBasedOrientation = new Quaternion();
    }

    @Override
    public void updateGazeOrientation(final Quaternion orientation) {
        //every time a new rvcsBasedOrientation is set for the user's gaze, this method also automatically
        //computes the user's pitch and yaw angles in degrees relative to the device
        //pitch will be the angle around the horizon axis, while yaw will be the angle around the axis
        //that points to the ground, where the zero is given by the yaw calculated from the calibration
        //quaternion
        this.rvcsBasedOrientation = orientation;
        this.computeWearersPitchAndYaw();
    }

    @Override
    public void calibrate(final Quaternion calibration) {
        this.calibration = calibration;
    }

    @Override
    public Quaternion getOrientationRelativeToRVCS() {
        return this.rvcsBasedOrientation;
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
