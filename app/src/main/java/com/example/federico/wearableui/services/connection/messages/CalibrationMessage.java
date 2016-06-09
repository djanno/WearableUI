package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.services.connection.messages.content.Content;

/**
 * Created by Federico on 19/05/2016.
 */
public class CalibrationMessage extends Message implements ICalibrationMessage {

    private final Quaternion calibration;

    public CalibrationMessage(final Quaternion calibration) {
        super(Content.CALIBRATION);
        this.calibration = calibration;
    }

    @Override
    public Quaternion getCalibration() {
        return this.calibration;
    }

}
