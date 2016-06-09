package com.example.federico.wearableui.services.connection.connection_template;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.services.connection.messages.CalibrationMessage;
import com.example.federico.wearableui.services.connection.messages.Message;
import com.example.federico.wearableui.services.connection.messages.IMessage;
import com.example.federico.wearableui.services.connection.messages.OrientationChangedMessage;
import com.example.federico.wearableui.services.connection.messages.content.Content;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Federico on 19/05/2016.
 */

/**
 * This service serves as a template to start a connection towards the HMD; to supply finger orientation updates to it.
 * The user will have to extend this service and call the send methods appropriately.
 * Keep in mind that this service can not only supply finger orientation updates, but also commands
 * updates and the service can only send one of these two types of message at a time.
 * This means that if the service is in the "SUPPLYING_ORIENTATION_UPDATES" state, it will not send
 * any command; similarly, if it's in the "SUPPLYING_COMMANDS" state, it will not send
 * any orientation update.
 *
 * The service will be initialized in its SUPPLYING_COMMANDS state, since a calibration command
 * is needed in order to start registering orientation updates, and will pass to the SUPPLYING_ORIE-
 * NTATION_UPDATES state as soon as such command is sent.
 *
 * To go back to the SUPPLYING_COMMANDS state, there's a specific method to be call.
 */
public abstract class ConnectionBridgeService extends Service {

    private enum State { SUPPLYING_ORIENTATION_UPDATES, SUPPLYING_COMMANDS}

    private static final String UUID = "a99acd49-93b9-4d5b-b0ba-bb2171a7a9fd";

    /**
     * Defines whether the service is currently supplying orientation updates OR commands,
     * such as clicks or calibration commands.
     */
    private State state;

    protected BluetoothSocket socket;
    protected ObjectInputStream fromServer;
    protected ObjectOutputStream toServer;

    protected Handler connectionHandler;

    private void closeConnection() {
        try {
            if(this.fromServer != null) {
                this.fromServer.close();
                this.fromServer = null;
            }
            if(this.toServer != null) {
                this.toServer.close();
                this.toServer = null;
            }
            if(this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
        }
        catch(final IOException e) { /**/ }
    }

    private void send(final IMessage message) {
        if(this.socket != null) {
            this.connectionHandler.post(new SendMessageTask(message));
        }
    }

    protected void connect(final BluetoothDevice server) {
        this.connectionHandler.post(new ConnectionTask(server));
    }

    protected void sendLockUnlockMessage() {
        if(this.state.equals(State.SUPPLYING_COMMANDS)) {
            this.send(new Message(Content.LOCK_UNLOCK));
        }
    }

    protected void sendFingerCalibrationMessage(final Quaternion calibration) {
        if(this.state.equals(State.SUPPLYING_COMMANDS)) {
            this.send(new CalibrationMessage(calibration));
            this.state = State.SUPPLYING_ORIENTATION_UPDATES;
        }
    }

    protected void sendCursorClickMessage() {
        if(this.state.equals(State.SUPPLYING_COMMANDS)) {
            this.send(new Message(Content.CLICK));
        }
    }

    protected void sendResetCursorPositionMessage() {
        if(this.state.equals(State.SUPPLYING_COMMANDS)) {
            this.send(new Message(Content.RESET_CURSOR_POSITION));
        }
    }

    protected void sendOrientationChangedMessage(final Quaternion rotation) {
        if(this.state.equals(State.SUPPLYING_ORIENTATION_UPDATES)) {
            this.send(new OrientationChangedMessage(rotation));
        }
    }

    protected void stopSupplyingOrientationUpdates() {
        this.state = State.SUPPLYING_COMMANDS;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.state = State.SUPPLYING_COMMANDS;
        //create connection thread
        final HandlerThread connectionThread = new HandlerThread("ConnectionThread");
        connectionThread.start();
        //link handler to thread
        this.connectionHandler = new Handler(connectionThread.getLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.closeConnection();
        this.connectionHandler.removeCallbacksAndMessages(null);
        this.connectionHandler.getLooper().quit();
    }

    private class ConnectionTask implements Runnable {

        private final BluetoothDevice server;

        public ConnectionTask(final BluetoothDevice server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                socket = this.server.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
                socket.connect();
                toServer = new ObjectOutputStream(socket.getOutputStream());
                toServer.flush(); //flush the header
                fromServer = new ObjectInputStream(socket.getInputStream());
            }
            catch(final IOException e) {
                closeConnection();
            }
        }

    }

    private class SendMessageTask implements Runnable {

        private final IMessage message;

        public SendMessageTask(final IMessage message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                toServer.writeObject(this.message);
                toServer.flush();
            }
            catch(final IOException e) {
                closeConnection();
            }
        }

    }

    public class Binder extends android.os.Binder {

        public void askToConnectTo(final BluetoothDevice server) {
            connect(server);
        }

        public void askToSendLockUnlockMessage() {
            sendLockUnlockMessage();
        }

        public void askToSendFingerCalibrationMessage(final Quaternion calibration) {
            sendFingerCalibrationMessage(calibration);
        }

        public void askToSendCursorClickMessage() {
            sendCursorClickMessage();
        }

        public void askToSendOrientationChangedMessage(final Quaternion orientationUpdate) {
            sendOrientationChangedMessage(orientationUpdate);
        }

        public void askToStopSupplyingOrientationUpdates() {
            stopSupplyingOrientationUpdates();
        }

        public void askToResetCursorPosition() {
            sendResetCursorPositionMessage();
        }

    }
}
