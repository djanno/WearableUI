package com.example.federico.wearableui.controller;

import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.viewport.IViewport;

/**
 * @author Federico Giannoni
 */

/**
 * Interface for the {@link ViewportActivity}.
 */
public interface IViewportActivity {

    /**
     * Returns the {@link com.example.federico.wearableui.viewport.Viewport} linked to the Activity.
     * @return the {@link com.example.federico.wearableui.viewport.Viewport}.
     */
    IViewport getViewport();

    /**
     * Returns whether or not the Activity is in foreground.
     * @return true if the Activity is in foreground, false otherwise.
     */
    boolean isInForeground();

    /**
     * Starts the {@link com.example.federico.wearableui.services.connection.message_parser.MessageParserService}.
     */
    void openBluetoothConnection();

    /**
     * Stops the {@link com.example.federico.wearableui.services.connection.message_parser.MessageParserService}.
     */
    void closeBluetoothConnection();

}
