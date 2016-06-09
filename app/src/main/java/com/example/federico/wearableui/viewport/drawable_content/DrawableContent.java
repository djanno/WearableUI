package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.example.federico.wearableui.viewport.ContinuousViewport;
import com.example.federico.wearableui.viewport.Viewport;
import com.example.federico.wearableui.viewport.drawable_content.interaction_listener.EventListener;


/**
 * Created by Federico on 14/04/2016.
 */

/**
 * A DrawableContent is an element that can be added to a {@link com.example.federico.wearableui.viewport.Viewport}.
 * These elements know how to draw themselves inside the Viewport and they share the same coordinate system as the
 * Viewport. Keep in mind, that the coordinates specified for a DrawableContent are the coordinates of its bottom
 * left corner.
 */
public abstract class DrawableContent implements IDrawableContent {

    public enum Visibility {VISIBLE, HIDDEN, GONE}

    private Point viewportCoordinates;
    private EventListener listener;

    private final Viewport viewport;
    private final Paint paint;

    private Visibility visibility;

    /* this method maps the given viewport coordinates to the drawing coordinates relative to the given canvas */
    private Point toDrawingCoordinates(final Point viewportCoordinates, final Canvas canvas) {
        final Point drawingCoordinates = new Point();
        drawingCoordinates.x = viewportCoordinates.x + canvas.getWidth() / 2;
        drawingCoordinates.y = -viewportCoordinates.y + canvas.getHeight() / 2;
        return drawingCoordinates;
    }

    /* this method returns a rectangle representing the bounds of the drawable content. It's important to know
    *  that the coordinates of the vertices of the rectangle returned, are relative to the viewport coordinate
    *  system */
    private Rect getBounds() {
        final Point lowerBound = this.viewportCoordinates;
        //the upper bound has to be calculated, and it's done through an abstract method, since it depends on the
        //structure of the drawable content (i.e. a drawable content representing text will compute its upper bound
        //differently from a drawable content representing a rectangle)
        final Point upperBound = this.computeUpperBound();
        return new Rect(lowerBound.x, upperBound.y, upperBound.x, lowerBound.y);
    }

    /**
     * Helper function that tells us if the passed point is contained inside the passed hitbox
     * @param point
     * @param hitbox
     * @return true if the point is contained inside the hitbox
     */
    private boolean isPointInsideHitbox(final Point point, final Rect hitbox) {
        return (point.x >= hitbox.left && point.x <= hitbox.right && point.y >= hitbox.bottom
                && point.y <= hitbox.top);
    }

    /* this method contains the drawing logic of the component. The drawing coordinates that are passed as
     * argument are this time relative to the android coordinate system. This is because the canvas onto
     * which the drawing is done (also passed as argument) uses that coordinate system */
    abstract protected void draw(final Point drawingCoordinates, final Canvas canvas);

    /* this method computes the upper bound (top right corner) of the component */
    abstract protected Point computeUpperBound();

    /* constructor for a generic drawable content located in the given viewport coordinates and drawn
    *  using the given paint */
    public DrawableContent(final Point viewportCoordinates, final Viewport viewport, final Paint paint) {
        this.viewportCoordinates = viewportCoordinates;
        this.viewport = viewport;
        this.paint = paint;
        this.visibility = Visibility.VISIBLE;
    }

    @Override
    public final void drawOnCanvas(final Canvas canvas) {
        if(this.visibility.equals(Visibility.VISIBLE)) {
            this.draw(this.toDrawingCoordinates(this.viewportCoordinates, canvas), canvas);
        }
    }

    @Override
    public final void drawOnContinuousCanvas(final Canvas canvas) {
        if(this.visibility.equals(Visibility.VISIBLE)) {
            //to give continuity effect, each drawable content is drawn 3 times on the viewport
            //which means that each drawable content has to be drawn 3 times on the canvas
            final Point viewportCoordinates = new Point();
            viewportCoordinates.x = this.viewportCoordinates.x - canvas.getWidth() / 3;
            viewportCoordinates.y = this.viewportCoordinates.y;
            this.draw(this.toDrawingCoordinates(viewportCoordinates, canvas), canvas);
            viewportCoordinates.x = this.viewportCoordinates.x;
            this.draw(this.toDrawingCoordinates(viewportCoordinates, canvas), canvas);
            viewportCoordinates.x = this.viewportCoordinates.x + canvas.getWidth() / 3;
            this.draw(this.toDrawingCoordinates(viewportCoordinates, canvas), canvas);
        }
    }

    @Override
    public final boolean isInBounds(final Point point) {
        if(this.visibility.equals(Visibility.GONE)) {
            return false;
        }

        final Rect dHitbox = this.getBounds();
        if(this.viewport instanceof ContinuousViewport) {
            final Rect wHitbox = new Rect(dHitbox);
            if(this.computeUpperBound().x > this.viewport.getViewportWidth() / 2 && this.viewportCoordinates.x < this.viewport.getViewportWidth() / 2) {
                wHitbox.left -= this.viewport.getViewportWidth();
                wHitbox.right -= this.viewport.getViewportWidth();
            }
            else if(this.viewportCoordinates.x < -this.viewport.getViewportWidth() / 2 && this.computeUpperBound().x > -this.viewport.getViewportWidth() / 2) {
                wHitbox.left += this.viewport.getViewportWidth();
                wHitbox.right += this.viewport.getViewportWidth();
            }
            return((this.isPointInsideHitbox(point, dHitbox) || this.isPointInsideHitbox(point, wHitbox)));
        }
        return this.isPointInsideHitbox(point, dHitbox);
    }


    @Override
    public final void moveTo(final Point point) {
        this.viewportCoordinates.x = point.x;
        this.viewportCoordinates.y = point.y;
    }

    @Override
    public boolean fireEvent() {
        if(this.listener == null) {
            return false;
        }
        else {
            this.listener.onEventFired(this);
            return true;
        }
    }

    @Override
    public Paint getPaint() {
        return this.paint;
    }

    @Override
    public final Point getViewportCoordinates() {
        return this.viewportCoordinates;
    }

    @Override
    public final Viewport getContainer() {
        return this.viewport;
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void setEventListener(final EventListener listener) {
        this.listener = listener;
    }

    @Override
    public void setColor(final int color) {
        this.paint.setColor(color);
    }

    @Override
    public void setAlpha(final int alpha) {
        this.paint.setAlpha(alpha);
    }

    @Override
    public void setFill(final boolean fill) {
        this.paint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
    }

    @Override
    public void setVisibility(final Visibility visibility) {
        this.visibility = visibility;
    }

}
