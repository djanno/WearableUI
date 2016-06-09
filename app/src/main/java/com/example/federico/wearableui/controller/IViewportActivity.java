package com.example.federico.wearableui.controller;

import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.viewport.IViewport;

/**
 * Created by Federico on 25/04/2016.
 */
public interface IViewportActivity {

    IViewport getViewport();

    boolean isInForeground();

    void openBluetoothConnection();

    void closeBluetoothConnection();

}
