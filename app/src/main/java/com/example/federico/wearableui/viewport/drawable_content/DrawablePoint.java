package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * Created by Federico on 28/04/2016.
 */
public class DrawablePoint extends DrawableContent {

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
