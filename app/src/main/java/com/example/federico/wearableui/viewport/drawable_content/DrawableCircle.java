package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * @author Federico Giannoni
 */

/**
 * This class models a DrawableCircle.
 */
public class DrawableCircle extends DrawableContent implements IDrawableCircle {

    /**
     * The length in pixels of the radius.
     */
    private int radius;

    /**
     * Returns the center of the DrawableCircle.
     * @return a {@link Point} representing the coordinate of the {@link Viewport} on which the center of the
     * DrawableCircle is located.
     */
    private Point getCenter() {
        final Point viewportCoordinates = this.getViewportCoordinates();
        return new Point(viewportCoordinates.x + this.radius, viewportCoordinates.y + this.radius);
    }

    /**
     * Constructor.
     * @param viewportCoordinates the {@link Point} representing the coordinate of the {@link Viewport} on which the
     *                            bottom left point of the DrawableCircle will be placed.
     * @param viewport the Viewport that will contain the DrawableCircle.
     * @param paint the {@link Paint} that the DrawableCircle will use to draw itself.
     * @param radius the length of the radius in pixels.
     */
    public DrawableCircle(final Point viewportCoordinates, final Viewport viewport, final Paint paint,
                          final int radius) {
        super(viewportCoordinates, viewport, paint);
        this.radius = radius;
    }

    @Override
    protected void draw(final Point drawingCoordinates, final Canvas canvas) {
        canvas.drawCircle(drawingCoordinates.x + this.radius, drawingCoordinates.y + this.radius,
                this.radius, this.getPaint());
    }

    @Override
    protected Point computeUpperBound() {
        final Point center = this.getCenter();
        return new Point(center.x + this.radius, center.y + this.radius);
    }

    @Override
    public void setRadius(final int radius) {
        this.radius = radius;
    }
}
