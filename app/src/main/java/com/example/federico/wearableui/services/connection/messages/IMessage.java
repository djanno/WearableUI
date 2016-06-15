package com.example.federico.wearableui.services.connection.messages;

import com.example.federico.wearableui.services.connection.messages.content.Content;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines a generic {@link Message}.
 */
public interface IMessage {

    /**
     * Returns the {@link Message} content.
     * @return the Message content.
     */
    Content getContent();

}
