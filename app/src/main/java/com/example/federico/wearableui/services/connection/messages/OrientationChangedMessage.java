package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.services.connection.messages.content.Content;

/**
 * @author Federico Giannoni
 */

/**
 * This class models a specific kind of {@link Message}s, that are used to supply {@link com.example.federico.wearableui.model.finger.Finger}
 * orientation updates.
 */
public class OrientationChangedMessage extends Message implements IOrientationChangedMessage {

    /**
     * The orientation update that will be provided by this Message.
     */
    private final Quaternion orientationUpdate;

    /**
     * Constructor.
     * @param orientationUpdate a {@link Quaternion} representing the {@link com.example.federico.wearableui.model.finger.Finger}
     *                          orientation update to be provided by this Message.
     */
    public OrientationChangedMessage(final Quaternion orientationUpdate) {
        super(Content.NEW_ORIENTATION);
        this.orientationUpdate = orientationUpdate;
    }

    @Override
    public Quaternion getOrientationUpdate() {
        return this.orientationUpdate;
    }

}
