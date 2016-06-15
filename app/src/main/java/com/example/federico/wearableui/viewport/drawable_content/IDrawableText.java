package com.example.federico.wearableui.viewport.drawable_content;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link DrawableText}.
 */
public interface IDrawableText extends IDrawableContent {

    /**
     * Returns the content of the DrawableText.
     * @return a String representing the content of the DrawableText.
     */
    String getContent();

    /**
     * Sets the content of the DrawableText.
     * @param content a String representing the content of the DrawableText.
     */
    void setContent(final String content);

}
