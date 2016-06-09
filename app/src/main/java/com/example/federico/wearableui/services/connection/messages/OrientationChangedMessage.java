package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.services.connection.messages.content.Content;

/**
 * Created by Federico on 18/05/2016.
 */
public class OrientationChangedMessage extends Message implements IOrientationChangedMessage {

    private final Quaternion rotation;

    public OrientationChangedMessage(final Quaternion rotation) {
        super(Content.NEW_ORIENTATION);
        this.rotation = rotation;
    }

    @Override
    public Quaternion getOrientationUpdate() {
        return this.rotation;
    }

}
