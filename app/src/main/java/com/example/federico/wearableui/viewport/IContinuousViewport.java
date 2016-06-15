package com.example.federico.wearableui.viewport;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link ContinuousViewport}.
 */
public interface IContinuousViewport {

    /**
     * Returns the actual extra width of the Viewport compared to the screen of the device.
     * @return the actual extra width in pixels compared to the screen of the device.
     */
    int getActualExtraWidth();

    /**
     * Returns the actual extra height of the Viewport compared to the screen of the device.
     * @return the actual extra height in pixels compared to the screen of the device.
     */
    int getActualWidth();

}
