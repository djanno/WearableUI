package com.example.federico.wearableui.model.gaze;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * Created by Federico on 29/04/2016.
 */
public interface IGaze {

    /**
     * This method updates the Gaze based on the new orientation provided. The orientation provided
     * to this method has to be relative to Rotation Vector Coordinate System.
     * @param orientation the orientation that will be used to compute the new Gaze orientation.
     */
    void updateGazeOrientation(final Quaternion orientation);

    /**
     * This method sets the passed quaternion as a calibration tool, that will be used to translate
     * the orientation relative to the Rotation Vector Coordinate System that are provided, into the
     * orientation relative to the device (local) coordinate system.
     * @param calibration the quaternion that will be used as the zero position. The orientation relative
     *                    to the device coordinate system will be computed by calculating the delta between
     *                    this quaternion and the others supplied with updateGazeOrientation.
     */
    void calibrate(final Quaternion calibration);

    /**
     * This method returns the orientation of the Gaze expressed in the coordinate system of the rotation
     * vector.
     * @return
     */
    Quaternion getOrientationRelativeToRVCS();

    Quaternion getCurrentCalibration();

    float getGazePitch();

    float getGazeYaw();

}
