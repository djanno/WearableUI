package com.example.federico.wearableui.viewport;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.drawable_content.IDrawableContent;

/**
 * @author Federico Giannoni
 */

/**
 * This class models a specific {@link Viewport} that presents a continuity property. This means that
 * this Viewport can be seen as a 2D blackboard that wraps around the user.
 */
public class ContinuousViewport extends Viewport implements IContinuousViewport {

    /**
     * The range of motion in degrees needed to scroll the Viewport vertically entirely.
     */
    private static final int Y_SCROLLING_ROM = 90;

    /**
     * The range of motion in degrees needed to scroll the Viewport horizontally entirely.
     */
    private static final int X_SCROLLING_ROM = 360;

    /**
     * How many pixels wider the visible part of this Viewport is compared to the screen dimensions.
     */
    private final int extraWidth;

    /**
     * How many pixels taller the Viewport is compared to the screen dimensions.
     */
    private final int extraHeight;

    /**
     * How many pixels wide the visible part of this Viewport is.
     */
    private final int width;

    /**
     * How many pixels tall the Viewport is.
     */
    private final int height;

    /**
     * Constructs a ContinuousViewport with the same dimensions as the device screen.
     * @param context the {@link Context}.
     */
    public ContinuousViewport(final Context context) {
        this(context, 0f, 0f);
    }

    /**
     * Constructs a ContinuousViewport with the passed extra dimensions.
     * @param context the {@link Context}.
     * @param extraSize a percentage expressing how much wider and taller the ContinuousViewport will be,
     *                  compared to the device screen.
     *                  For example, if 0.5 is passed, a ContinuousViewport that is 50% taller and wider than the
     *                  device screen will be created.
     */
    public ContinuousViewport(final Context context, final float extraSize) {
        this(context, extraSize, extraSize);
    }

    /**
     * Constructs a ContinuousViewport with the passed extra dimensions.
     * @param context the {@link Context}.
     * @param extraWidth a percentage expressing how much wider the visible part of the ContinuousViewport will be, compared to
     *                   the device screen. For example, if 0.5 is passed, the visible part of the ContinuousViewport
     *                   will be 50% wider than the device screen.
     * @param extraHeight a percentage expressing how much taller the ContinuousViewport will be, compared to the device screen.
     *                    For example if 0.5 is passed, the ContinuousViewport will be 50% taller than the device screen.
     */
    public ContinuousViewport(final Context context, final float extraWidth, final float extraHeight) {
        super(context, (2 + 3 * extraWidth), extraHeight);
        // These are not the actual extra dimensions of the Viewport, but they are the dimensions perceived by the user
        this.extraWidth = (int) (extraWidth * this.screen.x);
        this.extraHeight = (int) (extraHeight * this.screen.y);
        this.width = this.screen.x + this.extraWidth;
        this.height = this.screen.y + this.extraHeight;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        for(final IDrawableContent child : this.children) {
            // Draw only if the coordinates are acceptable. This is necessary to avoid rendering errors
            // in a ContinuousViewport, however there's no need to do this on a regular Viewport
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
            // Scroll the viewport by setting the margins
            this.setLayoutParams(this.params);
            // Scroll the cursor accordingly, so that it's always inside the field of view
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
