package com.example.suhibhabush.phantomlock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity
{

    private static final String hostname = "localHost";
    private static final int portnumber = 1400;
    private DatagramPacket packet;
    private static final String debugString = "debug";
    private static final byte housenumber = 0x00;
    private static final byte doornumber = 0x01;
    final byte UNLOCK = (byte) 0xFF;
    final byte LOCK = 0x00;
    final byte PASS_MSG = 0;
    final byte IMG_MSG = 1;
    final byte D_STAT_MSG = 2;
    final byte LK_MSG = 3;
    final byte GET_DOR = (byte) 0xFF;
    final DatagramSocket socket = null;
    //private ArrayList<ControlThread> activeRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DatagramSocket socket = null;

        try
        {
            //Connecting
            Log.i(debugString, "Attempting to connect  to server");
            socket = new DatagramSocket(portnumber, InetAddress.getByName(hostname));
            Log.i(debugString, "Connection established");


            boolean doorOpen = requestDoorStatus();
            if(doorOpen){
                //TODO: change locked textview to unlocked, else leave locked
            }



            //Receive Message from Server


    }
        catch (IOException e)
        {
            Log.e(debugString, e.getMessage());
        }

        Button btnLock = (Button) findViewById(R.id.btnLock);

        btnLock.setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View v) {
                byte[] sendMsg = new byte[100];
                sendMsg[0] = housenumber;
                sendMsg[1] = doornumber;
                sendMsg[2] = GET_DOR;
                //sendMsg[3] = ;
                try {
                    packet = new DatagramPacket(sendMsg, sendMsg.length, InetAddress.getByName(hostname), portnumber);
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
















    }

    private boolean requestDoorStatus() {
        byte[] sendMsg = new byte[100];
        sendMsg[0] = housenumber;
        sendMsg[1] = doornumber;
        sendMsg[2] = LK_MSG;
        //sendMsg[3] = ;
        DatagramPacket receivePacket;
        byte[] msg = new byte[100];
        receivePacket = new DatagramPacket(msg, msg.length);
        try {
            packet = new DatagramPacket(sendMsg, sendMsg.length, InetAddress.getByName(hostname), portnumber);
            socket.send(packet);
            socket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO: decode it and return code status


        return false;
    }
}
