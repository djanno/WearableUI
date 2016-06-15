package com.example.federico.wearableui.intraprocess_messaging;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * @author Federico Giannoni
 */

/**
 * Class that defines the Handler for the Main Thread. This Handler receives and manages all the
 * {@link Message}s that are dispatched towards the Main Thread and reacts to them by calling
 * the appropriate callback of the interface linked to it. These Messages that are received by the Handler,
 * are built around {@link IntraProcessMessage}s.
 *
 * This class is also a Singleton and can not function until a {@link IPMHCallbackInterface} has been set as
 * its callback interface. The interface can be set by calling the init() method.
 */
public class IntraProcessMessageHandler extends Handler {

    /**
     * Singleton instance.
     */
    private static IntraProcessMessageHandler INSTANCE = null;

    /**
     * Interface that implements the callbacks needed to react to all the IntraProcessMessages that can
     * be received by the Handler.
     */
    private final IPMHCallbackInterface callbackInterface;

    /**
     * Initializes the Handler by setting its callback interface.
     * @param callbackInterface an {@link IPMHCallbackInterface}.
     */
    public static void init(final IPMHCallbackInterface callbackInterface) {
        if(INSTANCE == null) {
            INSTANCE = new IntraProcessMessageHandler(callbackInterface);
        }
    }

    /**
     * Returns the Singleton instance.
     * @return the Singleton instance.
     */
    public static IntraProcessMessageHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor.
     * @param callbackInterface the {@link IPMHCallbackInterface} for the IntraProcessMessageHandler.
     */
    private IntraProcessMessageHandler(final IPMHCallbackInterface callbackInterface) {
        super(Looper.getMainLooper());
        this.callbackInterface = callbackInterface;
    }

    /**
     * Compares the passed {@link Message} with the passed {@link IntraProcessMessage}.
     * @param msg the Message received by the Handler.
     * @param ipm an IntraProcessMessage.
     * @return true if they represent the same message, false otherwise.
     */
    private boolean compare(final Message msg, final IntraProcessMessage ipm) {
        return msg.what == ipm.getMessageCode();
    }

    /**
     * Retrieves the parcelable content of the passed {@link Message}.
     * @param msg the Message received by the Handler.
     * @param ipm the {@link IntraProcessMessage} that represents the same Message as the one received by the Handler.
     * @return a {@link Parcelable} value contained in the received Message.
     */
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
        else if(this.compare(msg, IntraProcessMessage.RESET_CURSOR_POSITION)) {
            this.callbackInterface.resetCursorPosition();
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
