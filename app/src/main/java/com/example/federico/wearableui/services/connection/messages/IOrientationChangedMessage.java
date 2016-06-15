package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link OrientationChangedMessage}.
 */
public interface IOrientationChangedMessage extends IMessage {

    /**
     * Returns the {@link Quaternion} representing the orientation update.
     * @return the Quaternion representing the orientation update.
     */
    Quaternion getOrientationUpdate();

}
