package com.example.federico.wearableui.intraprocess_messaging;

/**
 * Created by Federico on 31/05/2016.
 */
public enum IntraProcessMessage {

    GAZE_CALIBRATION_STARTED(0, null),
    GAZE_CALIBRATION_WILL_FINISH(1, null),
    GAZE_CALIBRATION_FINISHED(2, "Gaze Calibration"),
    GAZE_ORIENTATION_UPDATE(3, "Gaze Update"),
    FINGER_CALIBRATION_RECEIVED(4, "Finger Calibration"),
    FINGER_ORIENTATION_UPDATE(5, "Finger Update"),
    CURSOR_CLICK(6, null),
    LOCK_UNLOCK(7, null),
    REDRAW_VIEWPORT(8, null),
    REDRAW_CURSOR(9, null),
    RESET_CURSOR_POSITION(10, null);

    private final int messageCode;
    private final String valueKey;

    IntraProcessMessage(final int value, final String valueKey) {
        this.messageCode = value;
        this.valueKey = valueKey;
    }

    public int getMessageCode() {
        return this.messageCode;
    }

    public String getValueKey() {
        return this.valueKey;
    }

}
