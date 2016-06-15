package com.example.federico.wearableui.viewport.drawable_content;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link DrawableCircle}.
 */
public interface IDrawableCircle extends IDrawableContent {

    /**
     * Sets the radius of the DrawableCircle.
     * @param radius the length of the radius expressed in pixels.
     */
    void setRadius(final int radius);

}
