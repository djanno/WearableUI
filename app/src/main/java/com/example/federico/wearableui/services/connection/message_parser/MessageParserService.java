package com.example.federico.wearableui.services.connection.message_parser;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessage;
import com.example.federico.wearableui.intraprocess_messaging.IntraProcessMessageHandler;
import com.example.federico.wearableui.representation.Quaternion;
import com.example.federico.wearableui.services.connection.messages.ICalibrationMessage;
import com.example.federico.wearableui.services.connection.messages.IMessage;
import com.example.federico.wearableui.services.connection.messages.IOrientationChangedMessage;
import com.example.federico.wearableui.services.connection.messages.content.Content;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Federico Giannoni
 */

/**
 * This Service manages the connection with the device that provides commands (such as clicks, {@link com.example.federico.wearableui.model.finger.Finger}
 * calibrations and lock-unlock of the {@link com.example.federico.wearableui.viewport.Viewport}) and Finger
 * orientation updates. This service manages the connection and communication with such device, by receiving its messages
 * and parsing them.
 */
public class MessageParserService extends Service {

    /**
     * UUID used to establish a Bluetooth communication.
     */
    private static final String UUID = "a99acd49-93b9-4d5b-b0ba-bb2171a7a9fd";

    /**
     * Handler that operates over a HandlerThread delegated to host the connection code.
     */
    private Handler connectionHandler;

    /**
     * A flag that indicates whether or not the Service has already been unbound and it's, therefore, about
     * to be destroyed.
     */
    private boolean serviceClosing;

    /**
     * Enqueues the passed task in the Message Loop with the specified delay.
     * @param task a {@link Runnable} to be run.
     * @param delayInMillis time interval (in milliseconds) between the call to this method and the execution of the task provided to it.
     */
    private void enqueueTask(final Runnable task, final int delayInMillis) {
        if(!this.serviceClosing) {
            this.connectionHandler.postDelayed(task, delayInMillis);
        }
    }

    /**
     * Prepares a {@link Message} for the {@link IntraProcessMessageHandler} from a {@link IntraProcessMessage}.
     * @param message the {@link com.example.federico.wearableui.services.connection.messages.Message} to be sent to the IntraProcessMessageHandler.
     * @return the {@link Message} that will be sent to the IntraProcessMessageHandler.
     */
    private Message messageForIPMHandler(final IntraProcessMessage message) {
        return Message.obtain(IntraProcessMessageHandler.getInstance(), message.getMessageCode());
    }

    /**
     * Sends a {@link com.example.federico.wearableui.model.finger.Finger} orientation update {@link Message}
     * to the {@link IntraProcessMessageHandler}.
     * @param orientationUpdate the {@link Quaternion} representing the new Finger orientation expressed in a coordinate system that is
     *                          different from the user's coordinate system.
     */
    protected final void sendFingerOrientationUpdateMessage(final Quaternion orientationUpdate) {
        final Message toDispatch = this.messageForIPMHandler(IntraProcessMessage.FINGER_ORIENTATION_UPDATE);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(IntraProcessMessage.FINGER_ORIENTATION_UPDATE.getValueKey(), orientationUpdate);
        toDispatch.setData(bundle);
        toDispatch.sendToTarget();
    }

    /**
     * Sends a click {@link Message} to the {@link IntraProcessMessageHandler}.
     */
    protected final void sendCursorClickMessage() {
        this.messageForIPMHandler(IntraProcessMessage.CURSOR_CLICK).sendToTarget();
    }

    /**
     * Sends a {@link com.example.federico.wearableui.model.finger.Finger} calibration {@link Message}
     * to the {@link IntraProcessMessageHandler}.
     * @param calibration the {@link Quaternion} representing the Finger orientation that will be used as starting position.
     *                    This orientation is expressed in a coordinate system that is different from the user's coordinate system.
     */
    protected final void sendFingerCalibrationMessage(final Quaternion calibration) {
        final Message toDispatch = this.messageForIPMHandler(IntraProcessMessage.FINGER_CALIBRATION_RECEIVED);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(IntraProcessMessage.FINGER_CALIBRATION_RECEIVED.getValueKey(), calibration);
        toDispatch.setData(bundle);
        toDispatch.sendToTarget();
    }

    /**
     * Sends a lock-unlock {@link Message} to the {@link IntraProcessMessageHandler}.
     */
    protected final void sendLockUnlockMessage() {
        this.messageForIPMHandler(IntraProcessMessage.LOCK_UNLOCK).sendToTarget();
    }

    /**
     * Sends a reset cursor position {@link Message} to the {@link IntraProcessMessageHandler}.
     */
    protected final void sendResetCursorPositionMessage() {
        this.messageForIPMHandler(IntraProcessMessage.RESET_CURSOR_POSITION).sendToTarget();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Service started
        this.serviceClosing = false;
        // Start the thread to handle bluetooth connection
        final HandlerThread connectionThread = new HandlerThread("ConnectionThread");
        connectionThread.start();
        // Attach the connectionHandler to the thread
        this.connectionHandler = new Handler(connectionThread.getLooper());
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        // Start accepting connections.
        this.connectionHandler.post(new AcceptConnectionTask());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        // This service isn't meant to be bound, just started
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Sets the flag that indicates that the service is closing
        this.serviceClosing = true;
        // Remove messages and callbacks and close the thread on which the handler operates
        this.connectionHandler.removeCallbacksAndMessages(null);
        this.connectionHandler.getLooper().quit();
    }

    /**
     * Accepts connection from a device that will provide commands as well as {@link com.example.federico.wearableui.model.finger.Finger}
     * orientation updates.
     */
    private class AcceptConnectionTask implements Runnable {

        /**
         * The socket used to accept connections.
         */
        private BluetoothServerSocket welcomeSocket;

        /**
         * Constructor.
         */
        public AcceptConnectionTask() {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            try {
                this.welcomeSocket = adapter.listenUsingRfcommWithServiceRecord(adapter.getName(), java.util.UUID.fromString(UUID));
            }
            catch(final IOException e) {
                //try again in 2 seconds
                MessageParserService.this.enqueueTask(new AcceptConnectionTask(), 2000);
            }
        }

        @Override
        public void run() {
            try {
                Log.i("MessageParserTask", "Waiting for a device to connect.");
                final BluetoothSocket socket = this.welcomeSocket.accept();
                Log.i("MessageParserTask", "Connected to: " + socket.getRemoteDevice().getName() + ", " + socket.getRemoteDevice().getAddress());
                MessageParserService.this.enqueueTask(new ParseMessageTask(socket), 0);
            }
            catch(final IOException e) {
                // If something went wrong, try again to accept connections
                MessageParserService.this.enqueueTask(new AcceptConnectionTask(), 0);
            }
            finally {
                try {
                    this.welcomeSocket.close();
                }
                catch(final IOException e) { /**/ }
            }
        }

    }

    /**
     * The task used to read and parse a specific {@link com.example.federico.wearableui.services.connection.messages.Message}.
     */
    private class ParseMessageTask implements Runnable {

        /**
         * Socket towards the client.
         */
        private BluetoothSocket client;
        /**
         * ObjectInputStream to receive the client messages.
         */
        private ObjectInputStream fromClient;
        /**
         * ObjectOutputStream to send messages to the client. Currently this is not used as the application protocol
         * doesn't include any messages that the server can send.
         */
        private ObjectOutputStream toClient;

        /**
         * A flag indicating if the connection has dropped.
         */
        private boolean connectionDropped;

        /**
         * Closes the buffers and the socket, therefore terminating the connection.
         */
        private void closeConnection() {
            try {
                if(this.fromClient != null) {
                    this.fromClient.close();
                    this.fromClient = null;
                }
                if(this.toClient != null) {
                    this.toClient.close();
                    this.toClient = null;
                }
                if(this.client != null) {
                    this.client.close();
                    this.client = null;
                }
            }
            catch(final IOException e) { /**/ }
        }

        /**
         * Constructor.
         * @param client the socket towards the client.
         */
        public ParseMessageTask(final BluetoothSocket client) {
            this.client = client;
            try {
                this.toClient = new ObjectOutputStream(this.client.getOutputStream());
                this.toClient.flush(); //flush the header
                this.fromClient = new ObjectInputStream(this.client.getInputStream());
                this.connectionDropped = false;
            }
            catch(final IOException e) {
                this.closeConnection();
                MessageParserService.this.enqueueTask(new AcceptConnectionTask(), 2000);
            }
        }

        @Override
        public void run() {
            try {
                final IMessage received = (IMessage) this.fromClient.readObject();
                if(received.getContent().equals(Content.NEW_ORIENTATION)) {
                    final Quaternion q = ((IOrientationChangedMessage) received).getOrientationUpdate();
                    MessageParserService.this.sendFingerOrientationUpdateMessage(q);
                }
                else if(received.getContent().equals(Content.CLICK)) {
                    MessageParserService.this.sendCursorClickMessage();
                }
                else if(received.getContent().equals(Content.LOCK_UNLOCK)) {
                    MessageParserService.this.sendLockUnlockMessage();
                }
                else if(received.getContent().equals(Content.RESET_CURSOR_POSITION)) {
                    MessageParserService.this.sendResetCursorPositionMessage();
                }
                else if(received.getContent().equals(Content.CALIBRATION)) {
                    final Quaternion q = ((ICalibrationMessage) received).getCalibration();
                    MessageParserService.this.sendFingerCalibrationMessage(q);
                }
            }
            catch(final IOException io) {
                Log.d("IOException", " " + io.getMessage());
                io.printStackTrace();
                this.connectionDropped = true;
            }
            catch(final ClassNotFoundException cnf) {
                Log.d("ClassNotFoundException", " " + cnf.getMessage());
                cnf.printStackTrace();
                this.connectionDropped = true;
            }
            finally {
                if(serviceClosing || this.connectionDropped) {
                    this.closeConnection();
                    // If something went wrong, try again to accept connections.
                    MessageParserService.this.enqueueTask(new AcceptConnectionTask(), 2000);
                }
                else {
                    MessageParserService.this.enqueueTask(this, 0);
                }
            }
        }

    }

}
