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
 * Created by Federico on 28/04/2016.
 */
public class DrawableBitmap extends DrawableContent implements IDrawableBitmap {

    private Bitmap bitmap;

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
