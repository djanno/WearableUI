package com.example.federico.wearableui.model.gaze;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of the {@link Gaze}.
 */
public interface IGaze {

    /**
     * Updates the Gaze orientation.
     * @param orientation a {@link Quaternion} that represents the new orientation of the Gaze expressed in the World Coordinate
     *                    System. By calculating the difference between this orientation and the one that has been set as calibration,
     *                    it is possible to obtain the Gaze orientation relative to the device coordinate system.
     */
    void updateGazeOrientation(final Quaternion orientation);

    /**
     * Sets the Gaze calibration {@link Quaternion}.
     * @param calibration a Quaternion that represents the starting position of the Gaze, expressed in the World Coordinate System.
     *                    By calculating the difference between the future orientations provided (expressed in the WCS) and this one,
     *                    it is possible to obtain the Gaze orientation relative to the device coordinate system.
     */
    void calibrate(final Quaternion calibration);

    /**
     * This method returns the orientation of the Gaze expressed according to the World Coordinate System.
     * @return a {@link Quaternion} representing the orientation of the Gaze according to the World Coordinate System.
     */
    Quaternion getOrientationRelativeToWCS();

    /**
     * This method returns the starting position of the Gaze, which is also the starting position of the device.
     * @return a {@link Quaternion} representing the starting orientation of the Gaze according to the World Coordinate System.
     */
    Quaternion getCurrentCalibration();

    /**
     * Returns the pitch angle of the Gaze relative to the device coordinate system.
     * @return an angle in degrees representing the pitch of the Gaze relative to the device coordinate system.
     */
    float getGazePitch();

    /**
     * Returns the yaw angle of the Gaze relative to the device coordinate system.
     * @return an angle in degrees representing the yaw of the Gaze relative to the device coordinate system.
     */
    float getGazeYaw();

}
