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
 * @author Federico Giannoni
 */

/**
 * This service serves as a template to start a connection towards the HMD, to supply finger orientation
 * updates and commands to it. The user will have to extend this service and call the send methods appropriately.
 * Keep in mind that this service can not only supply finger orientation updates, but also commands
 * updates and the service can only send one of these two types of message at a time.
 * This means that if the service is in the "SUPPLYING_ORIENTATION_UPDATES" state, it will filter away
 * all commands that try to be sent; similarly, if it's in the "SUPPLYING_COMMANDS" state, it will filter
 * away all orientation updates that try to be sent.
 *
 * The service will be initialized in its SUPPLYING_COMMANDS state, since a calibration command
 * is needed in order to start registering orientation updates, and will pass to the SUPPLYING_ORIENTATION_UPDATES
 * state as soon as such command is sent.
 *
 * To go back to the SUPPLYING_COMMANDS state, there's a specific method to be called.
 */
public abstract class ConnectionBridgeService extends Service {

    /**
     * Enumerator that defines the states the Service can be in.
     */
    private enum State { SUPPLYING_ORIENTATION_UPDATES, SUPPLYING_COMMANDS}

    /**
     * UUID used to connect to the HMD (that serves has a server).
     */
    private static final String UUID = "a99acd49-93b9-4d5b-b0ba-bb2171a7a9fd";

    /**
     * Defines whether the service is currently supplying orientation updates OR commands,
     * such as clicks or calibration commands.
     */
    private State state;

    /**
     * Socket towards the server.
     */
    protected BluetoothSocket socket;
    /**
     * ObjectInputStream to read the server replies. This is currently not used as the application protocol
     * does not include any feedback from the server.
     */
    protected ObjectInputStream fromServer;
    /**
     * ObjectOutputStream to write messages towards the server.
     */
    protected ObjectOutputStream toServer;

    /**
     * Handler that operates over the HandlerThread delegated to support the connection towards the server.
     */
    protected Handler connectionHandler;

    /**
     * Closes the buffers and socket, therefore terminating the connection.
     */
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

    /**
     * Sends a generic {@link Message} towards the server.
     * @param message the Message to be sent.
     */
    private void send(final IMessage message) {
        if(this.socket != null) {
            this.connectionHandler.post(new SendMessageTask(message));
        }
    }

    /**
     * Establishes a connection towards the server passed as argument.
     * @param server the device (server) to connect to.
     */
    protected void connect(final BluetoothDevice server) {
        this.connectionHandler.post(new ConnectionTask(server));
    }

    /**
     * Sends a lock-unlock {@link Message} towards the server, but only if the Service is in its SUPPLYING_COMMANDS state.
     */
    protected void sendLockUnlockMessage() {
        if(this.state.equals(State.SUPPLYING_COMMANDS)) {
            this.send(new Message(Content.LOCK_UNLOCK));
        }
    }

    /**
     * Sends a {@link com.example.federico.wearableui.model.finger.Finger} calibration {@link Message}
     * towards the server, but only if the Service is in its SUPPLYING_COMMANDS state. If a calibration
     * message is successfully sent, the Service passes to the SUPPLYING_ORIENTATION_UPDATES state.
     * @param calibration a {@link Quaternion} that will be set as the calibration Quaternion for the Finger.
     */
    protected void sendFingerCalibrationMessage(final Quaternion calibration) {
        if(this.state.equals(State.SUPPLYING_COMMANDS)) {
            this.send(new CalibrationMessage(calibration));
            this.state = State.SUPPLYING_ORIENTATION_UPDATES;
        }
    }

    /**
     * Sends a click {@link Message} towards the server, but only if the Service is in its SUPPLYING_COMMANDS state.
     */
    protected void sendCursorClickMessage() {
        if(this.state.equals(State.SUPPLYING_COMMANDS)) {
            this.send(new Message(Content.CLICK));
        }
    }

    /**
     * Sends a reset cursor position {@link Message} towards the server, but only if the Service is in its SUPPLYING_COMMANDS
     * state.
     */
    protected void sendResetCursorPositionMessage() {
        if(this.state.equals(State.SUPPLYING_COMMANDS)) {
            this.send(new Message(Content.RESET_CURSOR_POSITION));
        }
    }

    /**
     * Sends a {@link com.example.federico.wearableui.model.finger.Finger} orientation update {@link Message} towards the server,
     * but only if the Service is in its SUPPLYING_ORIENTATION_UPDATES state.
     * @param orientationUpdate a {@link Quaternion} representing the new orientation of the Finger, expressed in a coordinate system
     *                          that is not the user's coordinate system.
     */
    protected void sendOrientationChangedMessage(final Quaternion orientationUpdate) {
        if(this.state.equals(State.SUPPLYING_ORIENTATION_UPDATES)) {
            this.send(new OrientationChangedMessage(orientationUpdate));
        }
    }

    /**
     * Brings the Service to its SUPPLYING_COMMANDS state.
     */
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

    /**
     * Task used to connect to a device (server) via Bluetooth.
     */
    private class ConnectionTask implements Runnable {

        /**
         * The server to connect to.
         */
        private final BluetoothDevice server;

        /**
         * Constructor.
         * @param server the server to connect to.
         */
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

    /**
     * Task used to send a {@link Message} to a device (server) towards which a connection has already
     * been estabished.
     */
    private class SendMessageTask implements Runnable {

        /**
         * The message to be sent.
         */
        private final IMessage message;

        /**
         * Constructor.
         * @param message the message to be sent.
         */
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

    /**
     * The binder for the {@link ConnectionBridgeService}.
     */
    public class Binder extends android.os.Binder {

        /**
         * Asks the Service to connect to the device (server) passed as argument.
         * @param server the device to connect to.
         */
        public void askToConnectTo(final BluetoothDevice server) {
            connect(server);
        }

        /**
         * Asks the Service to send a lock-unlock {@link Message} towards the server.
         */
        public void askToSendLockUnlockMessage() {
            sendLockUnlockMessage();
        }

        /**
         * Asks the Service to send a {@link com.example.federico.wearableui.model.finger.Finger} calibration {@link Message}
         * towards the server.
         * @param calibration a {@link Quaternion} that will be used as the starting orientation for the Finger.
         *                    This orientation isn't realtive to the user's coordinate system.
         */
        public void askToSendFingerCalibrationMessage(final Quaternion calibration) {
            sendFingerCalibrationMessage(calibration);
        }

        /**
         * Asks the Service to send a click {@link Message} towards the server.
         */
        public void askToSendCursorClickMessage() {
            sendCursorClickMessage();
        }

        /**
         * Asks the Service to send a {@link com.example.federico.wearableui.model.finger.Finger} orientation update {@link Message}
         * towards the server.
         * @param orientationUpdate a {@link Quaternion} representing the new Finger orientation expressed in a coordinate system that
         *                          is different from the user's coordinate system.
         */
        public void askToSendOrientationChangedMessage(final Quaternion orientationUpdate) {
            sendOrientationChangedMessage(orientationUpdate);
        }

        /**
         * Asks the Service to transition over its SUPPLYING_COMMANDS state.
         */
        public void askToStopSupplyingOrientationUpdates() {
            stopSupplyingOrientationUpdates();
        }

        /**
         * Asks the Service to send a reset cursor position {@link Message} towards the server.
         */
        public void askToResetCursorPosition() {
            sendResetCursorPositionMessage();
        }

    }
}
