package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * @author Federico Giannoni
 */

/**
 * This class models a DrawableLine.
 */
public class DrawableLine extends DrawableContent implements IDrawableLine {

    /**
     * The line width in pixels.
     */
    private int deltaX;
    /**
     * The line height in pixels.
     */
    private int deltaY;

    /**
     * Constructor.
     * @param from the {@link Point} representing the coordinate of the {@link Viewport} on which the starting point
     *             of the DrawableLine will be placed.
     * @param to the Point representing the coordinate of the Viewport on which the end point of the DrawableLine will be placed.
     * @param viewport the Viewport that will contain the DrawableLine.
     * @param paint the {@link Paint} that the DrawableLine will use to draw itself.
     */
    public DrawableLine(final Point from, final Point to, final Viewport viewport, final Paint paint) {
        super(from, viewport, paint);
        this.setEndPoint(to);
    }

    @Override
    protected void draw(final Point drawingCoordinates, final Canvas canvas) {
        canvas.drawLine(drawingCoordinates.x, drawingCoordinates.y, drawingCoordinates.x + this.deltaX,
                drawingCoordinates.y - this.deltaY, this.getPaint());
    }

    @Override
    protected Point computeUpperBound() {
        final Point viewportCoordinates = this.getViewportCoordinates();
        return new Point(viewportCoordinates.x + this.deltaX, viewportCoordinates.y + this.deltaY);
    }

    @Override
    public void setEndPoint(final Point endPoint) {
        final Point viewportCoordinates = this.getViewportCoordinates();
        this.deltaX = endPoint.x - viewportCoordinates.x;
        this.deltaY = endPoint.y - viewportCoordinates.y;
    }
}
