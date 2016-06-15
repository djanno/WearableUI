package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link CalibrationMessage}.
 */
public interface ICalibrationMessage extends IMessage {

    /**
     * Returns the calibration {@link Quaternion} associated to the message.
     * @return the calibration Quaternion associated to the message.
     */
    Quaternion getCalibration();

}
