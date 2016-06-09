package com.example.federico.wearableui.viewport;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;

import com.example.federico.wearableui.representation.EulerAngles;
import com.example.federico.wearableui.viewport.drawable_content.DrawableBitmap;
import com.example.federico.wearableui.viewport.drawable_content.DrawableContent;
import com.example.federico.wearableui.viewport.drawable_content.DrawableLine;
import com.example.federico.wearableui.viewport.drawable_content.DrawablePoint;
import com.example.federico.wearableui.viewport.drawable_content.DrawableRectangle;
import com.example.federico.wearableui.viewport.drawable_content.DrawableText;
import com.example.federico.wearableui.viewport.drawable_content.cursor.ICursor;

/**
 * Created by Federico on 14/04/2016.
 */

/**
 * Interface for {@link Viewport}
 */
public interface IViewport {

    /* returns the portion of the viewport that is currently visible to the user */
    Viewport.FieldOfView getFOV();

    /* returns the cursor of the viewport */
    ICursor getCursor();

    /* returns how wider the viewport is compared to the device screen */
    int getExtraWidth();

    /* returns how taller the viewport is compared to the device screen */
    int getExtraHeight();

    /* returns the width of the viewport */
    int getViewportWidth();

    /* returns the height of the viewport */
    int getViewportHeight();

    /* returns whether or not the viewport is locked */
    boolean isLocked();

    /* draws the given text on the viewport with the given parameters */
    DrawableText drawText(final Point where, final String text, final int size, final int color,
                          final int alpha, final boolean fill);

    /* draws a rectangle on the viewport with the given parameters */
    DrawableRectangle drawRectangle(final Point where, final int width, final int height, final int color,
                                    final int alpha, final boolean fill);

    /* draws a line on the viewport with the given parameters */
    DrawableLine drawLine(final Point from, final Point to, final int color, final int alpha,
                          final boolean fill);

    /* draws a point on the viewport with the given parameters */
    DrawablePoint drawPoint(final Point point, final int color, final int alpha, final boolean fill);

    /* draws the image associated with the given resId on the viewport, using the given parameters */
    DrawableBitmap drawImage(final int resId, final Point where, final int width, final int height);

    /* adds the given content to the viewport*/
    void addContent(final DrawableContent toAdd);

    /* removes the given content from the viewport */
    boolean removeContent(final DrawableContent toRemove);

    /* scrolls the viewport based on the angles passed as argument. Such angles should represent
    * the orientation of the user's gaze */
    void scrollAccordingly(final float pitch, final float yaw);

    /* locks the viewport so that it can not be scrolled */
    void lock();

    /* unlocks the viewport so that it can be scrolled again */
    void unlock();

}
