package com.example.federico.wearableui.viewport.drawable_content.interaction_listener;


import com.example.federico.wearableui.viewport.drawable_content.DrawableContent;

/**
 * {@author Federico Giannoni}
 */

/**
 * Interface that defines the behaviour of a generic EventListener for a {@link DrawableContent}.
 */
public interface EventListener {

    /**
     * Callback called each time an event involving the {@link DrawableContent} to which this listener
     * is associated is fired.
     * @param content the DrawableContent involved in the event fired.
     */
    void onEventFired(final DrawableContent content);

}
