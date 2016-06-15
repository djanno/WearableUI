package com.example.federico.wearableui.viewport.drawable_content;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.example.federico.wearableui.viewport.Viewport;

/**
 * @author Federico Giannoni
 */
public class DrawableBitmap extends DrawableContent implements IDrawableBitmap {

    /**
     * The bitmap that will be drawn.
     */
    private Bitmap bitmap;

    /**
     * Constructor.
     * @param viewportCoordinates the {@link Point} representing the coordinate of the {@link Viewport} on which
     *                            the bottom left point of the DrawableBitmap will be placed.
     * @param viewport the Viewport that will contain the DrawableBitmap.
     * @param paint the {@link Paint} that the DrawableBitmap will use to draw itself.
     * @param bitmap the {@link Bitmap} that will be drawn.
     */
    public DrawableBitmap(final Point viewportCoordinates, final Viewport viewport, final Paint paint,
                          final Bitmap bitmap) {
        super(viewportCoordinates, viewport, paint);
        this.bitmap = bitmap;
    }

    @Override
    protected void draw(final Point drawingCoordinates, final Canvas canvas) {
        canvas.drawBitmap(this.bitmap, drawingCoordinates.x, drawingCoordinates.y - this.bitmap.getHeight(),
                this.getPaint());
    }

    @Override
    protected Point computeUpperBound() {
        final Point lowerBound = this.getViewportCoordinates();
        return new Point(lowerBound.x + this.bitmap.getWidth(), lowerBound.y + this.bitmap.getHeight());
    }

    @Override
    public void setBitmap(final Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void setBitmapFromResFile(final int resId, final int width, final int height) {
        if(this.getContainer() == null) {
            return;
        }

        final Resources res = this.getContainer().getContext().getResources();
        final Bitmap bitmap = BitmapFactory.decodeResource(res, resId);
        this.setBitmap(Bitmap.createScaledBitmap(bitmap, width, height, true));
    }
}
