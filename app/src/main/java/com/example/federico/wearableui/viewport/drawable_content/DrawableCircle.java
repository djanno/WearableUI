package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * Created by Federico on 09/06/2016.
 */
public class DrawableCircle extends DrawableContent implements IDrawableCircle {

    private int radius;

    private Point getCenter() {
        final Point viewportCoordinates = this.getViewportCoordinates();
        return new Point(viewportCoordinates.x + this.radius, viewportCoordinates.y + this.radius);
    }

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
