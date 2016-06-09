package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * Created by Federico on 18/05/2016.
 */
public interface IOrientationChangedMessage extends IMessage {

    Quaternion getOrientationUpdate();

}
