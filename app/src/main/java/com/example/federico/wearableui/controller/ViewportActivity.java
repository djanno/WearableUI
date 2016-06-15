package com.example.federico.wearableui.controller;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.federico.wearableui.intraprocess_messaging.IPMHCallbackInterface;
import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessageHandler;
import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.services.imu_handling.ImuHandlerService;
import com.example.federico.wearableui.services.imu_handling.SensorFusionService;
import com.example.federico.wearableui.viewport.IViewport;
import com.example.federico.wearableui.viewport.Viewport;
import com.example.federico.wearableui.viewport.drawable_content.DrawableContent;
import com.example.federico.wearableui.model.finger.Finger;
import com.example.federico.wearableui.services.connection.message_parser.MessageParserService;
import com.example.federico.wearableui.model.finger.IFinger;
import com.example.federico.wearableui.model.gaze.Gaze;
import com.example.federico.wearableui.model.gaze.IGaze;

/**
 * This class defines the Activity that serves as the main controller for the system. All of its calls
 * are executed on the Main Thread. This Activity implements {@link IPMHCallbackInterface} making it the delegate
 * for all the callbacks used to handle the {@link com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessage}s
 * received by the {@link IntraProcessMessageHandler}.
 */
public abstract class ViewportActivity extends IPMHCallbackInterface implements IViewportActivity {

    /**
     * ID for Intent request.
     */
    private static final int BECOME_DISCOVERABLE_REQUEST_ID = 0;
    /**
     * ID for Intent request.
     */
    private static final int BLUETOOTH_ENABLE_REQUEST_ID = 1;
    /**
     * ID for Intent request.
     */
    private static final int REQUEST_COARSE_PERMISSION = 2;

    /**
     * Service Connection for the Service that handles the Imu of the device.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mBinder = (ImuHandlerService.Binder) service;
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mBinder = null;
        }
    };

    /**
     * Sensor Service binder.
     */
    private ImuHandlerService.Binder mBinder;

    /**
     * The Viewport of the Activity. This is also the ContentView of the Activity.
     */
    private Viewport viewport;

    /**
     * A secondary Viewport that is set and used to display logs, such as calibration status update logs.
     */
    private Viewport monitor;

    /**
     * The Finger.
     */
    private IFinger finger;

    /**
     * The Gaze.
     */
    private IGaze gaze;

    /**
     * A flag indicating whether or not the Activity is in foreground.
     */
    private boolean isInForeground;

    /**
     * Asks for permissions.
     */
    private void checkForPermissions() {
        int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_PERMISSION);
        }
    }

    /**
     * Makes the device visible to all other bluetooth devices for an unlimited amount of time.
     */
    private void makeDeviceVisible() {
        final Intent becomeDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        becomeDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        this.startActivityForResult(becomeDiscoverable, BECOME_DISCOVERABLE_REQUEST_ID);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getSupportActionBar() != null && this.getSupportActionBar().isShowing()) {
            this.getSupportActionBar().hide();
        }

        IntraProcessMessageHandler.init(this);

        this.finger = Finger.getInstance();
        this.gaze = Gaze.getInstance();

        this.checkForPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.isInForeground = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unbindService(this.mServiceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.isInForeground = true;
        IntraProcessMessageHandler.getInstance().removeCallbacksAndMessages(null);
        final Intent bindSensorService = new Intent(this, SensorFusionService.class);
        this.bindService(bindSensorService, this.mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_ENABLE_REQUEST_ID && resultCode == Activity.RESULT_OK) {
            this.makeDeviceVisible();
        } else if (requestCode == BECOME_DISCOVERABLE_REQUEST_ID && resultCode == Activity.RESULT_FIRST_USER) {
            //start connection service to allow the finger orientation source to connect to you
            final Intent startMessageParserService = new Intent(this, MessageParserService.class);
            this.startService(startMessageParserService);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String permissions[], @NonNull final int[] grantResults) {
        if (requestCode == REQUEST_COARSE_PERMISSION && grantResults.length == 2 && (grantResults[0] != PackageManager.PERMISSION_GRANTED
                || grantResults[1] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "You can't proceed without giving permissions", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    @Override
    public void setContentView(final int layoutResID) {
        throw new RuntimeException("You can only use setContentView(View view) to set the content view" +
                " of a ViewportActivity.");
    }

    @Override
    public void setContentView(final View view) {
        if (!(view instanceof Viewport)) {
            throw new IllegalArgumentException("A ViewportActivity can only have a Viewport set as its" +
                    "content view.");
        }
        // Setting fullscreen mode
        final Window window = this.getWindow();
        final WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.flags |= 0x80000000;
        window.setAttributes(windowParams);
        // Setting the viewport and linking it to the gaze
        this.viewport = (Viewport) view;
        // If the calibration phase is over, we can set the current viewport instantly to be displayed
        // otherwise it will be done when the calibration phase is over
        if (this.mBinder != null && this.mBinder.askHasCalibrationPhaseFinished()) {
            super.setContentView(this.viewport);
        }
    }

    @Override
    public void setContentView(final View view, final ViewGroup.LayoutParams params) {
        throw new RuntimeException("You can only use setContentView(View view) to set the content view" +
                " of a ViewportActivity.");
    }

    @Override
    protected void redrawViewport() {
        // The viewport is redrawn based on where the wearer's gaze is oriented
        this.viewport.scrollAccordingly(this.gaze.getGazePitch(), this.gaze.getGazeYaw());
    }

    @Override
    protected void resetCursorPosition() {
        this.viewport.getCursor().moveAccordingly(0, 0);
    }

    @Override
    protected void redrawCursor() {
        // The cursor is redrawn based on where the wearer's finger is oriented
        this.viewport.getCursor().moveAccordingly(this.finger.getFingerPitch(), this.finger.getFingerYaw());
    }

    @Override
    protected void onGazeCalibrationStarted() {
        this.monitor = new Viewport(this);
        super.setContentView(this.monitor);
        final DrawableContent calibratingLog = this.monitor.drawText(new Point(-this.monitor.getViewportWidth() / 2 + 25,
                this.monitor.getViewportHeight() / 2 - 25), "calibrating...", 15, Color.WHITE, 255, true);

        this.monitor.drawText(new Point(calibratingLog.getViewportCoordinates().x, calibratingLog.getViewportCoordinates().y - 25),
                "move your head around...", 15, Color.WHITE, 255, true);

        this.monitor.invalidate();
    }

    @Override
    protected void onGazeCalibrationWillFinish() {
        final DrawableContent doneLog = this.monitor.drawText(new Point(-this.monitor.getViewportWidth() / 2 + 200,
                this.monitor.getViewportHeight() / 2 - 50), "  done", 15, Color.GREEN, 255, true);

        this.monitor.drawText(new Point(-this.monitor.getViewportWidth() / 2 + 25, doneLog.getViewportCoordinates().y - 25),
                "fix at a point...", 15, Color.WHITE, 255, true);

        this.monitor.invalidate();
    }

    @Override
    protected void onGazeCalibrationFinished(final Quaternion calibration) {
        this.monitor.drawText(new Point(-this.monitor.getViewportWidth() / 2 + 200, this.monitor.getViewportHeight() / 2 - 75),
                "  done", 15, Color.GREEN, 255, true);

        this.monitor.drawText(new Point(-this.monitor.getViewportWidth() / 2 + 200, this.monitor.getViewportHeight() / 2 - 25),
                "  done", 15, Color.GREEN, 255, true);

        this.monitor.invalidate();

        this.gaze.calibrate(calibration);

        if (this.viewport != null) {
            super.setContentView(this.viewport);
            this.monitor = null;
        }
    }

    @Override
    protected void onGazeOrientationUpdate(final Quaternion orientationUpdate) {
        this.gaze.updateGazeOrientation(orientationUpdate);
    }

    @Override
    protected void onFingerOrientationUpdate(final Quaternion orientationUpdate) {
        this.finger.updateOrientation(orientationUpdate);
    }

    @Override
    protected void onFingerCalibrationReceived(final Quaternion calibration) {
        this.finger.calibrate(calibration);
    }

    @Override
    protected void onCursorClickCommandReceived() {
        this.viewport.getCursor().click();
    }

    @Override
    protected void onLockUnlockCommandReceived() {
        if (this.viewport.isLocked()) {
            this.viewport.unlock();
        } else {
            this.viewport.lock();
        }
    }

    @Override
    public IViewport getViewport() {
        return this.viewport;
    }

    @Override
    public boolean isInForeground() {
        return this.isInForeground;
    }

    @Override
    public void openBluetoothConnection() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(this.getApplicationContext(), "No bluetooth adapter was found on the device.", Toast.LENGTH_SHORT).show();
            this.finish();
        } else if (!adapter.isEnabled()) {
            this.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_ENABLE_REQUEST_ID);
        } else {
            this.makeDeviceVisible();
        }
    }

    @Override
    public void closeBluetoothConnection() {
        // Close connection and parsing Service
        final Intent stopMessageParserService = new Intent(this, MessageParserService.class);
        this.stopService(stopMessageParserService);
    }
}
