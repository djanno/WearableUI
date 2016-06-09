package com.example.federico.wearableui.model.finger;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.example.federico.wearableui.controller.ViewportActivity;
import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessage;
import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessageHandler;
import com.example.federico.wearableui.representation.Quaternion;

/**
 * Created by Federico on 29/04/2016.
 */
public class Finger implements  IFinger {

    private static IFinger INSTANCE = null;

    private Quaternion rotation;
    private Quaternion orientation;
    private Quaternion calibration;

    private float fingerPitch;
    private float fingerYaw;

    private void computeCurrentOrientation() {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    final Quaternion startingDirection = calibration.clone();
                    startingDirection.inverse();

                    rotation.multiplyByQuat(startingDirection, orientation);

                    Finger.this.fingerPitch = (float) Math.toDegrees(orientation.getPitch());
                    //TODO: remove the (-1) in the future
                    Finger.this.fingerYaw = (float) Math.toDegrees(orientation.getYaw()) * (-1);

                    Message.obtain(IntraProcessMessageHandler.getInstance(), IntraProcessMessage.REDRAW_CURSOR.getMessageCode()).sendToTarget();

                    return null;
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static IFinger getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Finger();
        }

        return INSTANCE;
    }

    private Finger() {
        this.rotation = new Quaternion();
        this.orientation = new Quaternion();
    }

    @Override
    public void updateOrientation(final Quaternion orientationUpdate) {
        this.rotation = orientationUpdate;
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
