package com.example.federico.wearableui.viewport.drawable_content.cursor;

import android.graphics.Point;

import com.example.federico.wearableui.representation.EulerAngles;
import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.viewport.drawable_content.IDrawableContent;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of a {@link Cursor}.
 */
public interface ICursor extends IDrawableContent {

    /**
     * Dispatches a {@link android.view.MotionEvent} (click) in the center of the Cursor.
     */
    void click();

    /**
     * Moves the Cursor according to the angles passed as parameters.
     * @param deltaPitch an angle in degrees that represents the pitch of the {@link com.example.federico.wearableui.model.finger.Finger}
     *                   from its calibration orientation. This angle will determine of how many pixels the Cursor will move along the Y axis.
     * @param deltaYaw an angle in degrees that represents the yaw of the {@link com.example.federico.wearableui.model.finger.Finger}
     *                 from its calibration orientation. This angle will determine of how many pixels the Cursor will move along the X axis.
     */
    void moveAccordingly(final float deltaPitch, final float deltaYaw);

}
