package com.android.esp8266_wifi_ledcontrol;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private String serverMessage;
    public static String buttonPushed;

//    private final String HOST_ADDRESS = "192.168.4.1";
//    private final int HOST_PORT = 80;
    public static final String SERVERIP = "192.168.4.1"; //your Photon (formerly computer) IP address

    public static final int SERVERPORT = 80;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedWriter out1;
    OutputStreamWriter out2;
    OutputStream out3;
    BufferedReader in;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            Log.d("TCP Client", "Message: " + message);
            out.flush();
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVERPORT);

            try {
                //send the message to the server

                out3 = socket.getOutputStream();
                out2 = new OutputStreamWriter(socket.getOutputStream());
                out1 = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                Log.e("TCP Client", "C: out3 = " + out3);
                Log.e("TCP Client", "C: out2 = " + out2);
                Log.e("TCP Client", "C: out1 = " + out1);
                Log.e("TCP Client", "C: out0 = " + out);

                sendMessage(buttonPushed); //this was the key
                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");

                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.e("TCP Client", "C: received = " + in);
                Log.e("TCP Client", "C: run = " + mRun);
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    Log.e("TCP Client", "C: I got to the while loop!");

                    serverMessage = in.readLine().toString();

                    Log.e("TCP Client", "C: serverMessage = " + serverMessage);
                    new TCPClient(mMessageListener);
                    stopClient();
                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    } else {
                        serverMessage = null;
                    }
                }

                Log.e("TCP Client", "C: run = " + mRun);

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                Log.d("TCP Client", "Socket closed.");
                serverMessage = null;
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}