package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * @author Federico Giannoni
 */

/**
 * This class models a DrawablePoint.
 */
public class DrawablePoint extends DrawableContent {

    /**
     * Constructor.
     * @param viewportCoordinates the {@link Point} representing the coordinate of the {@link Viewport} on which
     *                            the DrawablePoint will be placed.
     * @param viewport the Viewport that will contain the DrawablePoint.
     * @param paint the {@link Paint} that the DrawablePoint will use to draw itself.
     */
    public DrawablePoint(final Point viewportCoordinates, final Viewport viewport, final Paint paint) {
        super(viewportCoordinates, viewport, paint);
    }

    @Override
    protected void draw(final Point drawingCoordinates, final Canvas canvas) {
        final Point viewportCoordinates = this.getViewportCoordinates();
        canvas.drawPoint(viewportCoordinates.x, viewportCoordinates.y, this.getPaint());
    }

    @Override
    protected Point computeUpperBound() {
        return this.getViewportCoordinates();
    }
}
