package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.services.connection.messages.content.Content;

import java.io.Serializable;

/**
 * @author Federico Giannoni
 */

/**
 * This class models a basic Message of the application protocol for the Bluetooth communication.
 */
public class Message implements IMessage, Serializable {

    /**
     * Serial Version UID.
     */
    protected static final long serialVersionUID = 110L;

    /**
     * Message content.
     */
    private final Content content;

    /**
     * Constructor.
     * @param content the content of the Message.
     */
    public Message(final Content content) {
        this.content = content;
    }

    @Override
    public Content getContent() {
        return this.content;
    }

}
