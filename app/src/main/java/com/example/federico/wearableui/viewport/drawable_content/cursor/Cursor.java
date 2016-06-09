package com.example.federico.wearableui.viewport.drawable_content.cursor;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.example.federico.wearableui.viewport.ContinuousViewport;
import com.example.federico.wearableui.viewport.IContinuousViewport;
import com.example.federico.wearableui.viewport.Viewport;
import com.example.federico.wearableui.viewport.drawable_content.DrawableContent;

/**
 * Created by Federico on 29/04/2016.
 */
public class Cursor extends DrawableContent implements ICursor {

    private static final int Y_SCROLLING_ROM = 40;
    private static final int X_SCROLLING_ROM = 40;

    private static final int CROSSHAIR_LENGTH = 10;
    private static final int CROSSHAIR_HEIGHT = 10;

    private final Point screen;

    private Point getCenter() {
        final Point viewportCoordinates = this.getViewportCoordinates();
        return new Point(viewportCoordinates.x + CROSSHAIR_LENGTH / 2, viewportCoordinates.y + CROSSHAIR_HEIGHT / 2);
    }

    public Cursor(final Viewport viewport, final Paint paint, final Point screen) {
        super(new Point(0, 0), viewport, paint);
        this.screen = screen;
    }

    @Override
    protected void draw(final Point drawingCoordinates, final Canvas canvas) {
        final Paint paint = this.getPaint();

        canvas.drawLine(drawingCoordinates.x, drawingCoordinates.y - CROSSHAIR_HEIGHT / 2,
                drawingCoordinates.x + CROSSHAIR_LENGTH, drawingCoordinates.y - CROSSHAIR_HEIGHT / 2,
                paint);

        canvas.drawLine(drawingCoordinates.x + CROSSHAIR_LENGTH / 2, drawingCoordinates.y,
                drawingCoordinates.x + CROSSHAIR_LENGTH / 2, drawingCoordinates.y - CROSSHAIR_HEIGHT,
                paint);
    }

    @Override
    protected Point computeUpperBound() {
        final Point viewportCoordinates = this.getViewportCoordinates();
        return new Point(viewportCoordinates.x + CROSSHAIR_LENGTH, viewportCoordinates.y + CROSSHAIR_HEIGHT);
    }

    @Override
    public void click() {
        final Point remappedCenter = this.getCenter();
        final Viewport viewport = this.getContainer();
        //check for the continuous viewport
        remappedCenter.x += (viewport instanceof ContinuousViewport ?
                ((IContinuousViewport) viewport).getActualWidth() / 2 : viewport.getViewportWidth() / 2);
        remappedCenter.y = -remappedCenter.y + viewport.getViewportHeight() / 2;
        //dispatch the click event in the center of the crosshair
        final MotionEvent toDispatch = MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis() + 20, MotionEvent.ACTION_DOWN, remappedCenter.x, remappedCenter.y, KeyEvent.META_SHIFT_ON);

        this.getContainer().dispatchTouchEvent(toDispatch);
    }

    @Override
    public void moveAccordingly(final float deltaPitch, final float deltaYaw) {
        //before moving the cursor, we check if the pitch and yaw are valid we consider them valid if
        //they are angles contained in the Range Of Motion we expressed through the constants Y_SCROLLING_ROM
        //and X_SCROLLING_ROM - this check avoids to move the cursor outside the FOV, so that the user can always see it
        if((deltaPitch >= -Y_SCROLLING_ROM / 2 && deltaPitch <= Y_SCROLLING_ROM / 2) && (deltaYaw >= -X_SCROLLING_ROM / 2 && deltaYaw <= X_SCROLLING_ROM / 2)) {
            final Point zero = this.getContainer().getFOV().getCenter();
            final int y = (int) (zero.y + (deltaPitch * (this.screen.y / Y_SCROLLING_ROM)));
            final int x = (int) (zero.x + (deltaYaw * (this.screen.x / X_SCROLLING_ROM)));
            this.moveTo(new Point(x, y));
        }
    }

}
