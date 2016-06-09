package com.example.federico.wearableui.model.finger;

import com.example.federico.wearableui.representation.Quaternion;

/**
 * Created by Federico on 29/04/2016.
 */
public interface IFinger {

    void updateOrientation(final Quaternion orientationUpdate);

    void calibrate(final Quaternion calibration);

    Quaternion getOrientation();

    Quaternion getCurrentCalibration();

    float getFingerPitch();

    float getFingerYaw();

}
