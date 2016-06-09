package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * Created by Federico on 14/04/2016.
 */
public class DrawableText extends DrawableContent implements IDrawableText {

    private String content;

    public DrawableText(final Point viewportCoordinates, final Viewport viewport, final Paint paint,
                        final String content) {
        super(viewportCoordinates, viewport, paint);
        this.content = content;
    }

    @Override
    protected void draw(final Point drawingCoordinates, final Canvas canvas) {
        canvas.drawText(this.content, drawingCoordinates.x, drawingCoordinates.y, this.getPaint());
    }

    @Override
    protected Point computeUpperBound() {
        final Point upperBound = new Point();
        final float size = this.getPaint().getTextSize();
        upperBound.y = (int) (this.getViewportCoordinates().y + size);
        upperBound.x = (int) (this.getViewportCoordinates().x + this.getPaint().measureText(this.content));
        return upperBound;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public void setContent(final String content) {
        this.content = content;
    }
}
