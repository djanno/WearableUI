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
 * Created by Federico on 13/05/2016.
 */

/**
 * This class serves as a service to manage the connection with the device that provides commands and figer orientation
 * updates (these events include the update of the finger orientation and the gestures to lock/unlock the viewport and cursor).
 * This service manages the connection and communication with the finger source, by receiving its messages
 * and parsing them.
 */
public class MessageParserService extends Service {

    private static final String UUID = "a99acd49-93b9-4d5b-b0ba-bb2171a7a9fd";

    private Handler connectionHandler;

    private boolean serviceClosing;

    private void enqueueTask(final Runnable task, final int delayInMillis) {
        if(!this.serviceClosing) {
            this.connectionHandler.postDelayed(task, delayInMillis);
        }
    }

    private Message messageForIPMHandler(final IntraProcessMessage message) {
        return Message.obtain(IntraProcessMessageHandler.getInstance(), message.getMessageCode());
    }

    protected final void sendFingerOrientationUpdateMessage(final Quaternion orientationUpdate) {
        final Message toDispatch = this.messageForIPMHandler(IntraProcessMessage.FINGER_ORIENTATION_UPDATE);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(IntraProcessMessage.FINGER_ORIENTATION_UPDATE.getValueKey(), orientationUpdate);
        toDispatch.setData(bundle);
        toDispatch.sendToTarget();
    }

    protected final void sendCursorClickMessage() {
        this.messageForIPMHandler(IntraProcessMessage.CURSOR_CLICK).sendToTarget();
    }

    protected final void sendFingerCalibrationMessage(final Quaternion calibration) {
        final Message toDispatch = this.messageForIPMHandler(IntraProcessMessage.FINGER_CALIBRATION_RECEIVED);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(IntraProcessMessage.FINGER_CALIBRATION_RECEIVED.getValueKey(), calibration);
        toDispatch.setData(bundle);
        toDispatch.sendToTarget();
    }

    protected final void sendLockUnlockMessage() {
        this.messageForIPMHandler(IntraProcessMessage.LOCK_UNLOCK).sendToTarget();
    }

    protected final void sendResetCursorPositionMessage() {
        this.messageForIPMHandler(IntraProcessMessage.RESET_CURSOR_POSITION).sendToTarget();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //service started
        this.serviceClosing = false;
        //start the thread to handle bluetooth connection
        final HandlerThread connectionThread = new HandlerThread("ConnectionThread");
        connectionThread.start();
        //attach the connectionHandler to the thread
        this.connectionHandler = new Handler(connectionThread.getLooper());
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        //start accepting connections
        this.connectionHandler.post(new AcceptConnectionTask());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        //this service isn't meant to be bound, just started
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //sets the flag that indicates that the service is closing
        this.serviceClosing = true;
        //remove messages and callbacks and close the thread on which the handler operates
        this.connectionHandler.removeCallbacksAndMessages(null);
        this.connectionHandler.getLooper().quit();
    }

    /**
     * Accepts connection from a device that will act as the source for all finger events, including
     * orientation and messages
     */
    private class AcceptConnectionTask implements Runnable {

        private BluetoothServerSocket welcomeSocket;

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
                //if something went wrong, try again to accept connections
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

    private class ParseMessageTask implements Runnable {

        private BluetoothSocket client;
        private ObjectInputStream fromClient;
        private ObjectOutputStream toClient;

        private boolean connectionDropped;

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
                    MessageParserService.this.enqueueTask(new AcceptConnectionTask(), 2000);
                }
                else {
                    MessageParserService.this.enqueueTask(this, 0);
                }
            }
        }

    }

}
