package com.example.federico.wearableui.intraprocess_messaging;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * Created by Federico on 01/06/2016.
 */
public class IntraProcessMessageHandler extends Handler {

    private static IntraProcessMessageHandler INSTANCE = null;

    private final IPMHCallbackInterface callbackInterface;

    public static void init(final IPMHCallbackInterface callbackInterface) {
        if(INSTANCE == null) {
            INSTANCE = new IntraProcessMessageHandler(callbackInterface);
        }
    }

    public static IntraProcessMessageHandler getInstance() {
        return INSTANCE;
    }

    private IntraProcessMessageHandler(final IPMHCallbackInterface callbackInterface) {
        super(Looper.getMainLooper());
        this.callbackInterface = callbackInterface;
    }

    private boolean compare(final Message msg, final IntraProcessMessage ipm) {
        return msg.what == ipm.getMessageCode();
    }

    private Parcelable retrieveParcelable(final Message msg, final IntraProcessMessage ipm) {
        return msg.getData().getParcelable(ipm.getValueKey());
    }

    @Override
    public void handleMessage(final Message msg) {
        if(this.compare(msg, IntraProcessMessage.REDRAW_VIEWPORT)) {
            this.callbackInterface.redrawViewport();
        }
        else if(this.compare(msg, IntraProcessMessage.REDRAW_CURSOR)) {
            this.callbackInterface.redrawCursor();
        }
        else if(this.compare(msg, IntraProcessMessage.GAZE_ORIENTATION_UPDATE)) {
            final Parcelable gOrientationUpdate = this.retrieveParcelable(msg, IntraProcessMessage.GAZE_ORIENTATION_UPDATE);
            this.callbackInterface.onGazeOrientationUpdate((Quaternion) gOrientationUpdate);
        }
        else if(this.compare(msg, IntraProcessMessage.FINGER_ORIENTATION_UPDATE)) {
            final Parcelable fOrientationUpdate = this.retrieveParcelable(msg, IntraProcessMessage.FINGER_ORIENTATION_UPDATE);
            this.callbackInterface.onFingerOrientationUpdate((Quaternion) fOrientationUpdate);
        }
        else if(this.compare(msg, IntraProcessMessage.CURSOR_CLICK)) {
            this.callbackInterface.onCursorClickCommandReceived();
        }
        else if(this.compare(msg, IntraProcessMessage.LOCK_UNLOCK)) {
            this.callbackInterface.onLockUnlockCommandReceived();
        }
        else if(this.compare(msg, IntraProcessMessage.FINGER_CALIBRATION_RECEIVED)) {
            final Parcelable fCalibration = this.retrieveParcelable(msg, IntraProcessMessage.FINGER_CALIBRATION_RECEIVED);
            this.callbackInterface.onFingerCalibrationReceived((Quaternion) fCalibration);
        }
        else if(this.compare(msg, IntraProcessMessage.GAZE_CALIBRATION_STARTED)) {
            this.callbackInterface.onGazeCalibrationStarted();
        }
        else if(this.compare(msg, IntraProcessMessage.GAZE_CALIBRATION_WILL_FINISH)) {
            this.callbackInterface.onGazeCalibrationWillFinish();
        }
        else if(this.compare(msg, IntraProcessMessage.GAZE_CALIBRATION_FINISHED)) {
            final Parcelable gCalibration = this.retrieveParcelable(msg, IntraProcessMessage.GAZE_CALIBRATION_FINISHED);
            this.callbackInterface.onGazeCalibrationFinished((Quaternion) gCalibration);
        }
    }

}
