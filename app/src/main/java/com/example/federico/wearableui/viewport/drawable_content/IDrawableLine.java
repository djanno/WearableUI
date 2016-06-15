package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Point;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link DrawableLine}.
 */
public interface IDrawableLine extends IDrawableContent {

    /**
     * Sets the end point of the DrawableLine.
     * @param endPoint a {@link Point} expressed in the {@link com.example.federico.wearableui.viewport.Viewport} coordinate system
     *                 that represents the final point of the line.
     */
    void setEndPoint(final Point endPoint);

}
