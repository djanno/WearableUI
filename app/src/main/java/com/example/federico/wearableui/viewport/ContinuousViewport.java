package com.example.federico.wearableui.viewport;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.drawable_content.IDrawableContent;

/**
 * Created by Federico on 04/05/2016.
 */
public class ContinuousViewport extends Viewport implements IContinuousViewport {

    private static final int Y_SCROLLING_ROM = 90;
    private static final int X_SCROLLING_ROM = 360;

    private final int extraWidth;
    private final int extraHeight;

    private final int width;
    private final int height;

    public ContinuousViewport(final Context context) {
        this(context, 0f, 0f);
    }

    public ContinuousViewport(final Context context, final float extraSize) {
        this(context, extraSize, extraSize);
    }

    public ContinuousViewport(final Context context, final float extraWidth, final float extraHeight) {
        super(context, (2 + 3 * extraWidth), extraHeight);
        //these are not the actual extra dimensions of the viewport, but they are the dimensions perceived by the user
        this.extraWidth = (int) (extraWidth * this.screen.x);
        this.extraHeight = (int) (extraHeight * this.screen.y);
        this.width = this.screen.x + this.extraWidth;
        this.height = this.screen.y + this.extraHeight;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        for(final IDrawableContent child : this.children) {
            //draw only if the coordinates are acceptable. This is necessary to avoid rendering errors
            //in a continuous viewport, however there's no need to do this on a regular viewport
            if(child.getViewportCoordinates().x >= -this.width / 2 && child.getViewportCoordinates().x < this.width / 2) {
                child.drawOnContinuousCanvas(canvas);
            }
        }
        this.cursor.drawOnCanvas(canvas);
    }

    @Override
    public int getExtraWidth() {
        return this.extraWidth;
    }

    @Override
    public int getExtraHeight() {
        return this.extraHeight;
    }

    @Override
    public int getViewportWidth() {
        return this.width;
    }

    @Override
    public int getViewportHeight() {
        return this.height;
    }

    @Override
    public void scrollAccordingly(float pitch, float yaw) {
        if(!this.isLocked()) {
            final int oldTopMargin = this.params.topMargin;
            final int oldLeftMargin = this.params.leftMargin;

            this.params.topMargin = (int) ((-this.extraHeight / 2) + (pitch * this.extraHeight / Y_SCROLLING_ROM));
            this.params.leftMargin = (int) ((-this.extraWidth / 2 - this.width) + (yaw * this.width / X_SCROLLING_ROM));
            //scroll the viewport by setting the margins
            this.setLayoutParams(this.params);
            //scroll the cursor accordingly, so that it's always inside the field of view
            final Point cursorCoordinates = this.cursor.getViewportCoordinates();
            cursorCoordinates.x -= (this.params.leftMargin - oldLeftMargin);
            cursorCoordinates.y += (this.params.topMargin - oldTopMargin);
            this.cursor.moveTo(cursorCoordinates);
        }
        this.invalidate();
    }

    @Override
    public int getActualExtraWidth() {
        return this.getActualWidth() - this.screen.x;
    }

    @Override
    public int getActualWidth() {
        return this.width * 3;
    }

}
