package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Bitmap;

/**
 * Created by Federico on 08/06/2016.
 */
public interface IDrawableBitmap extends IDrawableContent {

    void setBitmap(final Bitmap bitmap);

    void setBitmapFromResFile(final int resId, final int width, final int height);

}
