package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * Created by Federico on 28/04/2016.
 */
public class DrawableLine extends DrawableContent implements IDrawableLine {

    private int deltaX;
    private int deltaY;

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
