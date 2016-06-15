package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * @author Federico Giannoni
 */

/**
 * This class models a DrawableRectangle.
 */
public class DrawableRectangle extends DrawableContent implements IDrawableRectangle {

    /**
     * The rectangle width in pixels.
     */
    private int width;
    /**
     * The rectangle height in pixels.
     */
    private int height;

    /**
     * Constructor.
     * @param viewportCoordinates the {@link Point} representing the coordinate of the {@link Viewport} in
     *                            which the bottom left point of the DrawableRectangle will be placed.
     * @param viewport the Viewport that will contain the DrawableRectangle.
     * @param paint the {@link Paint} that the DrawableRectangle will use to draw itself.
     * @param width an int representing the length in pixels of the DrawableRectangle.
     * @param height an int representing the height in pixels of the DrawableRectangle.
     */
    public DrawableRectangle(final Point viewportCoordinates, final Viewport viewport, final Paint paint,
                             final int width, final int height) {
        super(viewportCoordinates, viewport, paint);
        this.width = width;
        this.height = height;
    }

    @Override
    protected void draw(final Point drawingCoordinates, final Canvas canvas) {
        canvas.drawRect(drawingCoordinates.x, drawingCoordinates.y - this.height, drawingCoordinates.x + this.width,
                drawingCoordinates.y, this.getPaint());
    }

    @Override
    protected Point computeUpperBound() {
        final Point viewportCoordinates = this.getViewportCoordinates();
        return new Point(viewportCoordinates.x + this.width, viewportCoordinates.y + this.height);
    }

    @Override
    public void setWidth(final int width) {
        this.width = width;
    }

    @Override
    public void setHeight(final int height) {
        this.height = height;
    }
}
