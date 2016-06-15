package com.example.federico.wearableui.intraprocess_messaging;

/**
 * @author Federico Giannoni
 */

/**
 * Enumerator that defines all the IntraProcessMessages that are exchanged between the different control flows
 * of the system.
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

    /**
     * An integer used to distinguish the content of a message. This flag is used as the "what" field of a
     * {@link android.os.Message}, since {@link android.os.Handler}s can't send customized message classes.
     */
    private final int messageCode;
    /**
     * String used to recover a value from a {@link android.os.Message}. It's null if the message has no extra
     * content.
     */
    private final String valueKey;

    /**
     * Constructor.
     * @param value a value for the message code.
     * @param valueKey a key to recover the value associated to the message.
     */
    IntraProcessMessage(final int value, final String valueKey) {
        this.messageCode = value;
        this.valueKey = valueKey;
    }

    /**
     * Returns the message code.
     * @return an integer used to distinguish the message from the others.
     */
    public int getMessageCode() {
        return this.messageCode;
    }

    /**
     * Returns a key to recover the value associated to the message.
     * @return a String used as a key to recover the value associated to the message or null if such message has no value associated to it.
     */
    public String getValueKey() {
        return this.valueKey;
    }

}
