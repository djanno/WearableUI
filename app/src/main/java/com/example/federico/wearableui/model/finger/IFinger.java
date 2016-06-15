package com.example.federico.wearableui.model.finger;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * @author Federico Giannoni
 */

/**
 * Interface that defines the behaviour of the {@link Finger} class.
 */
public interface IFinger {

    /**
     * Updates the Finger orientation.
     * @param orientationUpdate a {@link Quaternion} that represents the new orientation of the Finger. This orientation isn't relative
     *                          to the user's coordinate system, but by calculating the difference between it and the calibration Quaternion
     *                          it's possible to obtain the Finger orientation based on the user's coordinate system.
     */
    void updateOrientation(final Quaternion orientationUpdate);

    /**
     * Sets the Finger calibration {@link Quaternion}.
     * @param calibration a Quaternion that represents the starting position of the Finger. This orientation isn't relative to the user's
     *                    coordinate system, but it's used as a starting point. By calculating the difference between the future orientations
     *                    provided and this one, it is possible to obtain the Finger orientation based on the user's coordinate system.
     */
    void calibrate(final Quaternion calibration);

    /**
     * Returns the orientation of the Finger, expressed in the user's coordinate system.
     * @return a {@link Quaternion} representing the Finger orientation.
     */
    Quaternion getOrientation();

    /**
     * Returns the starting position of the Finger, expressed in a coordinate system that is not relative to the user.
     * This is used to calculate the current orientation of the Finger relative to the user's coordinate system.
     * @return a {@link Quaternion} representing the Finger starting orientation.
     */
    Quaternion getCurrentCalibration();

    /**
     * Returns the pitch of the Finger based on the user's coordinate system.
     * @return the degrees of the pitch angle of the Finger, relative to the user's coordinate system.
     */
    float getFingerPitch();

    /**
     * Returns the yaw of the Finger based on the user's coordinate system.
     * @return the degrees of the yaw angle of the Finger, relative to the user's coordinate system.
     */
    float getFingerYaw();

}
