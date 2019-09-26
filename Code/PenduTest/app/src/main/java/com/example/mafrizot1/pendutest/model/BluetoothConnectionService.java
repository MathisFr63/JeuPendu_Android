package com.example.mafrizot1.pendutest.model;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.mafrizot1.pendutest.MainActivity;
import com.example.mafrizot1.pendutest.MultiActivity;
import com.example.mafrizot1.pendutest.StartActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.UUID;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by Mathis on 2/1/2018.
 */

public class BluetoothConnectionService implements Serializable {
    private static final String TAG = "BluetoothConnectionServ";

    private static final String appName = "MYAPP";

    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    private Context context;

    private AcceptThread insecureAcceptThread;

    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private BluetoothDevice bluetoothDevice;
    private UUID deviceUUID;
    private ProgressDialog progressDialog;

    public BluetoothConnectionService(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

//             Create a new listening server socket

            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            } catch (IOException e) {

            }

            serverSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

//            This is a blocking call and will only return on a
//            successful connection or an exception.
            Log.d(TAG, "run: RFCOM server socket start.....");

            try {
                socket = serverSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

//            !! In the third part !!
            if (socket != null) {
                connected(socket, bluetoothDevice);
            }
            Log.i(TAG, "END acceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread");
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed." + e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            bluetoothDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN connectThread");

//            Get a BluetoothSocket for a connection with the
//            given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + MY_UUID_INSECURE);
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecrureRfcommSocket " + e.getMessage());
            }

            socket = tmp;

//            Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

//            Make a connection to the BluetoothSocket

            try {
//                This is a blocking call and will only return on a
//                successful connection or an exception
                socket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
//                Close the socket
                try {
                    socket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "connectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: could not connect tot UUID: " + MY_UUID_INSECURE);
            }

//            !! In the third part !!
            connected(socket, bluetoothDevice);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of socket in ConnectThread failed. " + e.getMessage());
            }
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

//        Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (insecureAcceptThread == null) {
            insecureAcceptThread = new AcceptThread();
            insecureAcceptThread.start();
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection
     * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     */
    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started.");

//        initprogress dialog
        progressDialog = ProgressDialog.show(context, "Connecting Bluetooth", "Please Wait...", true);

        connectThread = new ConnectThread(device, uuid);
        connectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

//            Dismiss the progressdialog when connection is established
            try {
                progressDialog.dismiss();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes; // bytes returned from read()

//            Keep listening to the InputStream until an exception occurs
            while (true) {
//                Read from the InputStream
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    if (incomingMessage != null) {
                        Intent intent = new Intent(context, MultiActivity.class).putExtra("EXTRA_TEXT", incomingMessage.toUpperCase());
                        startActivity(context, intent, null);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "read: Error reading inputStream. " + e.getMessage());
                    break;
                }
            }
        }

        //        Call this from the main activity to send date to the remote device
        public void write(byte[] bytes, BluetoothConnectionService bcs) {
            String text = new String(bytes, Charset.defaultCharset().defaultCharset());
            Log.d(TAG, "write: Writing to outputStream: " + text);
            try {
                outputStream.write(bytes);
                Intent intent = new Intent(context, MultiActivity.class).putExtra("EXTRA_TEXT", text.toUpperCase());

//                Il faut envoyer le BluetoothConnectionService pour que depuis l'activité Multi, on puisse récupérer
//                le bluetooth et ensuite envoyer des messages et réinitialiser le jeu des 2 joueurs en même temps.
//                Intent intent = new Intent(context, MultiActivity.class).putExtra("EXTRA_TEXT", text.toUpperCase()).putExtra("EXTRA_BCS", bcs);
                startActivity(context, intent, null);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to outputStream. " + e.getMessage());
            }
        }

        //        Call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

    }

    private void connected(BluetoothSocket socket, BluetoothDevice bluetoothDevice) {
        Log.d(TAG, "connected: Starting.");

//        Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronised manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
//        Synchronise a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
//        Perform the write
        connectedThread.write(out, this);
    }
}
