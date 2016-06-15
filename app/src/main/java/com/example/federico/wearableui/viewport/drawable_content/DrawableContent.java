package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.example.federico.wearableui.viewport.ContinuousViewport;
import com.example.federico.wearableui.viewport.Viewport;
import com.example.federico.wearableui.viewport.drawable_content.interaction_listener.EventListener;


/**
 * @author Federico Giannoni
 */

/**
 * A DrawableContent is an element that can be added to a {@link com.example.federico.wearableui.viewport.Viewport}.
 * These elements know how to draw themselves inside the Viewport and they share the same coordinate system as the
 * Viewport. Keep in mind, that the coordinates specified for a DrawableContent are the coordinates of its bottom
 * left corner.
 */
public abstract class DrawableContent implements IDrawableContent {

    /**
     * Enumerator for the visibility state of a DrawableContent.
     */
    public enum Visibility {VISIBLE, HIDDEN, GONE}

    /**
     * Coordinates of the bottom left corner of the DrawableContent.
     */
    private Point viewportCoordinates;

    /**
     * EventListener for the DrawableContent.
     */
    private EventListener listener;

    /**
     * Viewport in which the DrawableContent is contained.
     */
    private final Viewport viewport;

    /**
     * Paint used by the DrawableContent to draw itself.
     */
    private final Paint paint;

    /**
     * Visibility of the DrawableContent
     */
    private Visibility visibility;

    /**
     * This method maps the passed coordinates relative to the {@link Viewport} coordinate system to the drawing coordinates
     * relative to the Android coordinate system.
     * @param viewportCoordinates a {@link Point} representing a coordinate relative to the Viewport coordinate system.
     * @param canvas the {@link Canvas} on which the drawing coordinates are based.
     * @return a {@link Point} representing the coordinate on which the DrawableContent will draw itself. This coordinate is the
     * bottom left coordinate of the DrawableContent and its relative to the passed Canvas, which uses the Android coordinate system.
     */
    private Point toDrawingCoordinates(final Point viewportCoordinates, final Canvas canvas) {
        final Point drawingCoordinates = new Point();
        drawingCoordinates.x = viewportCoordinates.x + canvas.getWidth() / 2;
        drawingCoordinates.y = -viewportCoordinates.y + canvas.getHeight() / 2;
        return drawingCoordinates;
    }

    /**
     * Returns a {@link Rect} representing the bounds of the DrawableContent. It's important to know that the coordinates
     * of the vertices of the returned rectangle are relative to the {@link Viewport} coordinate system.
     * @return a Rectangle representing the bounds of the DrawableContent, expressed in the Viewport coordinate system.
     */
    private Rect getBounds() {
        final Point lowerBound = this.viewportCoordinates;
        // The upper bound has to be calculated, and it's done through an abstract method, since it depends on the
        // structure of the drawable content (i.e. a drawable content representing text will compute its upper bound
        // differently from a drawable content representing a rectangle)
        final Point upperBound = this.computeUpperBound();
        return new Rect(lowerBound.x, upperBound.y, upperBound.x, lowerBound.y);
    }

    /**
     * Helper function that tells us if the passed {@link Point} is contained inside the passed hitbox.
     * Both the Point and the hitbox share the {@link Viewport} coordinate system.
     * @param point the Point to be checked.
     * @param hitbox a {@link Rect} representing the hitbox in which the presence of the Point will be checked.
     * @return true if the point is contained inside the hitbox, false otherwise.
     */
    private boolean isPointInsideHitbox(final Point point, final Rect hitbox) {
        return (point.x >= hitbox.left && point.x <= hitbox.right && point.y >= hitbox.bottom
                && point.y <= hitbox.top);
    }

    /**
     * Abstract method used by the DrawableContent to draw itself.
     * @param drawingCoordinates a {@link Point} representing the bottom left coordinate of the component from
     *                           which it will draw itself. This coordinate is relative to the Android coordinate system,
     *                           since it has to be used to draw the element on a {@link Canvas}.
     * @param canvas the Canvas onto which the element will draw itself.
     */
    abstract protected void draw(final Point drawingCoordinates, final Canvas canvas);

    /**
     * Abstract method used to compute the top right {@link Point} of the DrawableContent. The coordinates of
     * the point that are returned, are relative to the {@link Viewport} coordinate system.
     * @return a point representing the coordinate of the top right corner of the DrawableContent, expressed in the
     * Viewport coordinate system.
     */
    abstract protected Point computeUpperBound();

    /**
     * Constructor.
     * @param viewportCoordinates a {@link Point} representing the coordinate of the bottom left point of
     *                            the DrawableContent inside the {@link Viewport}.
     * @param viewport the Viewport that will contain the DrawableContent.
     * @param paint the {@link Paint} used by the DrawableContent to draw itself.
     */
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
            // To give continuity effect, each drawable content is drawn 3 times on the viewport
            // which means that each drawable content has to be drawn 3 times on the canvas
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
            // Wrap around of bounds in case the Viewport is a ContinuousViewport
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
