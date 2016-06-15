package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * @author Federico Giannoni
 */

/**
 * This class models a DrawableText.
 */
public class DrawableText extends DrawableContent implements IDrawableText {

    /**
     * The content that will displayed when this DrawableText draws itself.
     */
    private String content;

    /**
     * Constructor.
     * @param viewportCoordinates the {@link Point} representing the coordinate of the {@link Viewport} in which the
     *                            bottom left point of the DrawableText will be placed.
     * @param viewport the {@link Viewport} that will contain the DrawableText.
     * @param paint the {@link Paint} that the DrawableText will use to draw itself.
     * @param content the String of text that will be drawn.
     */
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
