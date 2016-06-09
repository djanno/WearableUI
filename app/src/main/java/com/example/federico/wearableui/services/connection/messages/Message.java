package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.services.connection.messages.content.Content;

import java.io.Serializable;

/**
 * Created by Federico on 18/05/2016.
 */
public class Message implements IMessage, Serializable {

    protected static final long serialVersionUID = 110L;

    private final Content content;

    public Message(final Content content) {
        this.content = content;
    }

    @Override
    public Content getContent() {
        return this.content;
    }

}
