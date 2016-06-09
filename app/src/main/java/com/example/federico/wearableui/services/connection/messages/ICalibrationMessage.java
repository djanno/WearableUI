package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * Created by Federico on 19/05/2016.
 */
public interface ICalibrationMessage extends IMessage {

    Quaternion getCalibration();

}
