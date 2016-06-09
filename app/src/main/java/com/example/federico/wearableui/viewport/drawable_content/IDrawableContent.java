package com.example.federico.wearableui.viewport.drawable_content;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.federico.wearableui.viewport.Viewport;
import com.example.federico.wearableui.viewport.drawable_content.interaction_listener.EventListener;


/**
 * Created by Federico on 14/04/2016.
 */

/**
 * Interface for {@link DrawableContent}
 */
public interface IDrawableContent {

    /* draws the drawable content onto the given canvas */
    void drawOnCanvas(final Canvas canvas);

    /* draws the drawable content onto the given canvas, as if such canvas was a continuous canvas */
    void drawOnContinuousCanvas(final Canvas canvas);

    /* checks if the point is contained in the bounds of the drawable content.
    *  Be aware that the point has to be relative to the viewport coordinates and not to the android
    *  coordinates */
    boolean isInBounds(final Point point);

    /* moves the drawable content over to the specified point.
    *  Be aware that the point has to be relative to the viewport coordinates and not to the android
    *  coordinates */
    void moveTo(final Point point);

    /* informs the drawable content that an event that involves it has been registered */
    boolean fireEvent();

    /* returns the paint the component uses to draw itself */
    Paint getPaint();

    /* returns the coordinates of the component inside the viewport (these are viewport based too) */
    Point getViewportCoordinates();

    /* returns the viewport that contains this component */
    Viewport getContainer();

    /* returns the visibility of this component */
    DrawableContent.Visibility getVisibility();

    /* sets the event listener for the drawable content */
    void setEventListener(final EventListener listener);

    /* sets the color used to draw this component */
    void setColor(final int color);

    /* set the alpha used to draw this component */
    void setAlpha(final int alpha);

    /* sets the fill property used to draw this component. True to fill, false otherwise */
    void setFill(final boolean fill);

    /* sets the visibility of the drawable content */
    void setVisibility(final DrawableContent.Visibility visibility);

}
