package com.example.federico.wearableui.viewport.drawable_content;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link DrawableRectangle}.
 */
public interface IDrawableRectangle extends IDrawableContent {

    /**
     * Sets the width of the DrawableRectangle.
     * @param width the width to be set.
     */
    void setWidth(final int width);

    /**
     * Sets the height of the DrawableRectangle.
     * @param height the height to be set.
     */
    void setHeight(final int height);

}
