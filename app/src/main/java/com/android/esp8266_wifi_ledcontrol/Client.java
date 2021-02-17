package com.android.esp8266_wifi_ledcontrol;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class Client extends Thread {

    private String address;
    private int PORT;

    public Client(String address , int PORT) {

        this.address = address;
        this.PORT = PORT;
    }

    public void run() {
        try {
            Socket socket = new Socket(address, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            Log.e("run: ", "called"+ response);

            socket.close();
        } catch (Exception ignored) {

        }
    }
}