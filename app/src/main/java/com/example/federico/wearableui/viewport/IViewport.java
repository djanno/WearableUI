package com.example.federico.wearableui.viewport;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;

import com.example.federico.wearableui.representation.EulerAngles;
import com.example.federico.wearableui.viewport.drawable_content.DrawableBitmap;
import com.example.federico.wearableui.viewport.drawable_content.DrawableCircle;
import com.example.federico.wearableui.viewport.drawable_content.DrawableContent;
import com.example.federico.wearableui.viewport.drawable_content.DrawableLine;
import com.example.federico.wearableui.viewport.drawable_content.DrawablePoint;
import com.example.federico.wearableui.viewport.drawable_content.DrawableRectangle;
import com.example.federico.wearableui.viewport.drawable_content.DrawableText;
import com.example.federico.wearableui.viewport.drawable_content.cursor.ICursor;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link Viewport}.
 */
public interface IViewport {

    /**
     * Returns the portion of the Viewport that is currently visible to the user.
     * @return the {@link com.example.federico.wearableui.viewport.Viewport.FieldOfView}.
     */
    Viewport.FieldOfView getFOV();

    /**
     * Returns the {@link com.example.federico.wearableui.viewport.drawable_content.cursor.Cursor} for this
     * Viewport.
     * @return the Cursor for this Viewport.
     */
    ICursor getCursor();

    /**
     * Returns how much wider the Viewport is compared to the device's screen.
     * @return how many pixels wider the Viewport is compared to the device's screen.
     */
    int getExtraWidth();

    /**
     * Returns how much taller the Viewport is compared to the device's screen.
     * @return how many pixels taller the Viewport is compared to the device's screen.
     */
    int getExtraHeight();

    /**
     * Returns the width of the Viewport.
     * @return the Viewport width in pixels.
     */
    int getViewportWidth();

    /**
     * Returns the height of the Viewport.
     * @return the Viewport height in pixels.
     */
    int getViewportHeight();

    /**
     * Returns wether or not the Viewport is locked.
     * @return true if the Viewport is locked, false otherwise.
     */
    boolean isLocked();

    /**
     * Draws the passed text on the Viewport with the specified parameters.
     * @param where the bottom left {@link Point} where the text has to be drawn (relative to the Viewport coordinates).
     * @param text a String of text.
     * @param size the size of the text to be drawn.
     * @param color an int representing a color with whom the text will be drawn.
     * @param alpha an alpha value for the text (from 0 to 255).
     * @param fill true if the text should be drawn with a fill style, false if it should be drawn with a stroke style.
     * @return a {@link DrawableText} object representing what was drawn.
     */
    DrawableText drawText(final Point where, final String text, final int size, final int color,
                          final int alpha, final boolean fill);

    /**
     * Draws a rectangle on the Viewport based on the passed parameters.
     * @param where the bottom left {@link Point} where the rectangle should be drawn (relative to the Viewport coordinates).
     * @param width the width in pixels of the rectangle.
     * @param height the height in pixels of the rectangle.
     * @param color an int representing the color with whom the rectangle will be drawn.
     * @param alpha an alpha value for the rectangle (from 0 to 255).
     * @param fill true if the rectangle should be filled, false otherwise.
     * @return a {@link DrawableRectangle} object representing what was drawn.
     */
    DrawableRectangle drawRectangle(final Point where, final int width, final int height, final int color,
                                    final int alpha, final boolean fill);

    /**
     * Draws a circle on the Viewport based on the passed parameters.
     * @param where the bottom left {@link Point} where the circle should be drawn (relative to the Viewport coordinates).
     * @param radius a length in pixels for the radius.
     * @param color an int representing the color with whom the circle will be drawn.
     * @param alpha an alpha value for the circle (from 0 to 255).
     * @param fill true if the circle should be filled, false otherwise.
     * @return a {@link DrawableCircle} object representing what was drawn.
     */
    DrawableCircle drawCircle(final Point where, final int radius, final int color, final int alpha,
                              final boolean fill);

    /**
     * Draws a line on the Viewport based on the passed parameters.
     * @param from the starting {@link Point} of the line (relative to the Viewport coordinates).
     * @param to the end Point of the line (relative to the Viewport coordinates).
     * @param color an int representing the color with whom the line will be drawn.
     * @param alpha an alpha value for the line (from 0 to 255).
     * @param fill true for a fill style, false for a stroke style.
     * @return a {@link DrawableLine} object representing what was drawn.
     */
    DrawableLine drawLine(final Point from, final Point to, final int color, final int alpha,
                          final boolean fill);

    /**
     * Draws a point on the Viewport based on the passed parameters.
     * @param point a {@link Point} representing where the point should be drawn (relative to the Viewport coordinates).
     * @param color an int representing a color with whom the point will be drawn.
     * @param alpha an alpha value for the point (from 0 to 255).
     * @param fill true for a fill style, false for a stroke style.
     * @return a {@link DrawablePoint} object representing what was drawn.
     */
    DrawablePoint drawPoint(final Point point, final int color, final int alpha, final boolean fill);

    /**
     * Draws an image based on the specified parameters.
     * @param resId the id for a drawable resource.
     * @param where the bottom left {@link Point} where the image should be drawn (relative to the Viewport coordinates).
     * @param width the width in pixels.
     * @param height the height in pixels.
     * @return a {@link DrawableBitmap} object representing what was drawn.
     */
    DrawableBitmap drawImage(final int resId, final Point where, final int width, final int height);

    /**
     * Adds the passed {@link DrawableContent} to the Viewport.
     * @param toAdd the DrawableContent to be added.
     */
    void addContent(final DrawableContent toAdd);

    /**
     * Remove the passed {@link DrawableContent} from the Viewport.
     * @param toRemove the DrawableContent to be removed.
     * @return true if the DrawableContent was removed, false otherwise.
     */
    boolean removeContent(final DrawableContent toRemove);

    /**
     * Scrolls the Viewport by modifying the value of its margins based upon the angles passed as argument.
     * A call to this method also refreshes the Viewport by forcing it to redraw itself.
     * @param pitch the pitch angle in degrees of the {@link com.example.federico.wearableui.model.gaze.Gaze} from its
     *              calibration orientation. This will determine of how many pixels the topMargin of the Viewport will change
     *              (in other words, this will determine the magnitude of the scrolling along the y axis).
     * @param yaw the yaw angle in degrees of the {@link com.example.federico.wearableui.model.gaze.Gaze} from its
     *            calibration orientation. This will determine of how many pixels the leftMargin of the Viewport will change
     *            (in other words, this will determine the magnitude of the scrolling along the x axis).
     */
    void scrollAccordingly(final float pitch, final float yaw);

    /**
     * Locks the Viewport so that it can no longer be scrolled.
     */
    void lock();

    /**
     * Unlocks the Viewport making it scrollable.
     */
    void unlock();

}
