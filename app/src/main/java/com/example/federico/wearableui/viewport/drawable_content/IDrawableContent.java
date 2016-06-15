package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;
import com.example.federico.wearableui.viewport.drawable_content.interaction_listener.EventListener;


/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link DrawableContent}
 */
public interface IDrawableContent {

    /**
     * Draws the DrawableContent onto the given canvas.
     * @param canvas the Canvas onto which the DrawableContent will drawn itself.
     */
    void drawOnCanvas(final Canvas canvas);

    /**
     * Draws the drawable content onto the given canvas, as if such canvas was a continuous canvas.
     * @param canvas the Canvas onto which the DrawableContent will drawn itself.
     */
    void drawOnContinuousCanvas(final Canvas canvas);

    /**
     * Checks if the {@link Point} is contained in the bounds of this DrawableContent. Be aware that hte point has to be
     * expressed in the {@link Viewport} coordinate system and not in the Android coordinate system.
     * @param point the point to check.
     * @return true if the Point is inside the bounds of this DrawableContent, false otherwise.
     */
    boolean isInBounds(final Point point);

    /**
     * Moves this DrawableContent over to the specified {@link Point}. Be aware that the point has to be
     * relative to the {@link Viewport} coordinate system.
     * @param point the Point where the DrawableContent has to be moved.
     */
    void moveTo(final Point point);

    /**
     * Programmatically fires an event involving the DrawableContent that calls it. If a {@link EventListener} is
     * set for this DrawableContent, it will be notified.
     * @return true if the event has been handled by a EventListener, false otherwise.
     */
    boolean fireEvent();

    /**
     * Returns the {@link Paint} used by this DrawableContent to draw itself.
     * @return the Paint used by this DrawableContent to draw itself.
     */
    Paint getPaint();

    /**
     * Returns the coordinates of this DrawableContent expressed in the {@link Viewport} coordinate system.
     * @return a {@link Point} representing the coordinates of this DrawableContent expressed in the {@link Viewport} coordinate system.
     */
    Point getViewportCoordinates();

    /**
     * Returns the {@link Viewport} that contains this DrawableContent.
     * @return the Viewport that contains this DrawableContent.
     */
    Viewport getContainer();

    /**
     * Returns the {@link com.example.federico.wearableui.viewport.drawable_content.DrawableContent.Visibility} of this DrawableContent.
     * @return the Visibility of this DrawableContent.
     */
    DrawableContent.Visibility getVisibility();

    /**
     * Sets the passed {@link EventListener} for this DrawableContent.
     * @param listener an EventListener for this DrawableContent.
     */
    void setEventListener(final EventListener listener);

    /**
     * Sets the color used by this DrawableContent to draw itself.
     * @param color an int representing a color.
     */
    void setColor(final int color);

    /**
     * Sets the alpha used by this DrawableContent to draw itself.
     * @param alpha an int expressing the alpha (from 0 to 255).
     */
    void setAlpha(final int alpha);

    /**
     * Sets the fill property of the {@link Paint} used by this DrawableContent to draw itself.
     * @param fill true for a filling Paint, false for a stroke style Paint.
     */
    void setFill(final boolean fill);

    /**
     * Sets the {@link com.example.federico.wearableui.viewport.drawable_content.DrawableContent.Visibility} of this DrawableContent.
     * @param visibility the Visibility of this DrawableContent.
     */
    void setVisibility(final DrawableContent.Visibility visibility);

}
