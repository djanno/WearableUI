package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Bitmap;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link DrawableBitmap}.
 */
public interface IDrawableBitmap extends IDrawableContent {

    /**
     * Sets the bitmap.
     * @param bitmap the bitmap to be set.
     */
    void setBitmap(final Bitmap bitmap);

    /**
     * Sets the bitmap from a given resource id. This id has to be relative to a drawable resource.
     * @param resId the id of the drawable resource.
     * @param width the width of the bitmap.
     * @param height the height of the bitmap.
     */
    void setBitmapFromResFile(final int resId, final int width, final int height);

}
