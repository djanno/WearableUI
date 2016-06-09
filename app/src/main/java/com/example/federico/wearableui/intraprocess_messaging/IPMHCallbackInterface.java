package com.example.federico.wearableui.intraprocess_messaging;

/**
 * Created by Federico on 01/06/2016.
 */

import android.support.v7.app.AppCompatActivity;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * This class serves as an interface that defines the callbacks needed by the IntraProcessMessageHandler to handle the
 * Intra Process Messages. It is an abstract class and not an interface because the callback methods need to have
 * a "protected" access level and we can't obtain that by using an interface, which can only define "public" methods.
 *
 * In other words, the only component that can have the callbacks required by the IntraProcessMessageHandler is an activity
 * that extends this class.
 */
public abstract class IPMHCallbackInterface extends AppCompatActivity {

    protected abstract void redrawCursor();

    protected abstract void redrawViewport();

    protected abstract void resetCursorPosition();

    protected abstract void onGazeCalibrationStarted();

    protected abstract void onGazeCalibrationWillFinish();

    protected abstract void onGazeCalibrationFinished(final Quaternion calibration);

    protected abstract void onGazeOrientationUpdate(final Quaternion orientationUpdate);

    protected abstract void onFingerOrientationUpdate(final Quaternion orientationUpdate);

    protected abstract void onFingerCalibrationReceived(final Quaternion calibration);

    protected abstract void onCursorClickCommandReceived();

    protected abstract void onLockUnlockCommandReceived();

}
