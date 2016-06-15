package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.services.connection.messages.content.Content;

/**
 * @author Federico Giannoni
 */

/**
 * This class models a specific kind of {@link Message}s, that are used to supply {@link com.example.federico.wearableui.model.finger.Finger}
 * calibrations.
 */
public class CalibrationMessage extends Message implements ICalibrationMessage {

    /**
     * The new Finger calibration that will be provided by this Message.
     */
    private final Quaternion calibration;

    /**
     * Constructor.
     * @param calibration a {@link Quaternion} representing the new {@link com.example.federico.wearableui.model.finger.Finger}
     *                    calibration provided by this Message.
     */
    public CalibrationMessage(final Quaternion calibration) {
        super(Content.CALIBRATION);
        this.calibration = calibration;
    }

    @Override
    public Quaternion getCalibration() {
        return this.calibration;
    }

}
