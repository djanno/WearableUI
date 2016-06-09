package com.example.federico.wearableui.viewport.drawable_content.cursor;

import android.graphics.Point;

import com.example.federico.wearableui.representation.EulerAngles;
import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.viewport.drawable_content.IDrawableContent;

/**
 * Created by Federico on 29/04/2016.
 */
public interface ICursor extends IDrawableContent {

    void click();

    void moveAccordingly(final float deltaPitch, final float deltaYaw);

}
