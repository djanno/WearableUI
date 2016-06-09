package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * Created by Federico on 15/04/2016.
 */
public class DrawableRectangle extends DrawableContent implements IDrawableRectangle {

    private int width;
    private int height;

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
