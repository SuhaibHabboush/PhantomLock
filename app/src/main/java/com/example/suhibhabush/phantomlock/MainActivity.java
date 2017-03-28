package com.example.suhibhabush.phantomlock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.*;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.os.AsyncTask;

public class MainActivity extends AppCompatActivity
{
    //TODO: Add image request, add multiple doors
    private String hostName = "local";
    private InetAddress hostAddress;
    //TODO: external ip address of the pi or internal if we doing it thru the local network
    private static final int portnumber = 1400;
    private static final String debugString = "debug";
    public byte housenumber = 0x00;
    public byte doornumber = 0x01;
    public final byte UNLOCK = (byte) 0xFF;
    public final byte LOCK = 0x00;
    public final byte PASS_MSG = 0;
    public final byte IMG_MSG = 1;
    public final byte D_STAT_MSG = 2;
    public final byte LK_MSG = 3;
    public final byte GET_DOR = (byte) 0xFF;
    public DatagramSocket socket;
    private TextView tvDoorStatus;
    public final static String eventString = "Door doornum was ";
    public List<String> eventArrayList;
    public boolean currentDoorState;
    public ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        CreateAddress addressGetter = new CreateAddress();
        try {
            hostAddress = addressGetter.execute(hostName).get();
        } catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Finished previous hickup");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentDoorState = true;
        eventArrayList = new ArrayList<String>();

        tvDoorStatus = (TextView) findViewById(R.id.tvDoorStatus);
        ListView listView = (ListView) findViewById(R.id.lvRecAct);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, eventArrayList);
        listView.setAdapter(adapter);

        try
        {
            //Connecting
            eventArrayList.add(0, (getCurrentTimeStamp() + "App Launched."));
            Log.i(debugString, "Attempting to connect  to server");
            socket = new DatagramSocket();
            Log.i(debugString, "Connection established");

            //initialize text view with doorstatus
            updateDoorStatus(requestDoorStatus());

            //start thread that keeps updating door status
            ControlThread doorStatusUpdater = new ControlThread(this);
            doorStatusUpdater.start();
        }
        catch (IOException e)
        {
            Log.e(debugString, e.getMessage());
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }



        Button btnLock = (Button) findViewById(R.id.btnLock);

        final DatagramSocket finalSocket = socket;
        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] sendMsg = new byte[100];
                byte[] recMsg = new byte[100];
                sendMsg[0] = housenumber;
                sendMsg[1] = doornumber;
                sendMsg[2] = LK_MSG;

                DatagramPacket sendPacket;
                DatagramPacket receivePacket;
                receivePacket = new DatagramPacket(recMsg, recMsg.length);
                try {
                    sendPacket = new DatagramPacket(sendMsg, sendMsg.length, hostAddress, portnumber);
                    finalSocket.send(sendPacket);
                    finalSocket.receive(receivePacket);
                    byte[] receiveMsg = receivePacket.getData();
                    currentDoorState = (receiveMsg[3]==UNLOCK);
                    updateDoorStatus(currentDoorState);
                    int doorNum = doornumber;
                    eventArrayList.add(0, (getCurrentTimeStamp() + eventString.replace("doornum", Integer.toString(doorNum)))+ ((currentDoorState) ? "unlocked." : "locked."));
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                    int doorNum = doornumber;
                    eventArrayList.add(0, (getCurrentTimeStamp() + eventString.replace("doornum", Integer.toString(doorNum)))+ ((currentDoorState) ? "unlocked." : "locked."));
                    adapter.notifyDataSetChanged();
                }

            }
        });


    }

    private boolean requestDoorStatus() {
        byte[] sendMsg = new byte[100];
        byte[] recMsg = new byte[100];
        sendMsg[0] = housenumber;
        sendMsg[1] = doornumber;
        sendMsg[2] = GET_DOR;

        DatagramPacket sendPacket;
        DatagramPacket receivePacket;
        receivePacket = new DatagramPacket(recMsg, recMsg.length);

        try {
            sendPacket = new DatagramPacket(sendMsg, sendMsg.length, hostAddress, portnumber);
            socket.send(sendPacket);
            socket.receive(receivePacket);
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] receiveMsg = receivePacket.getData();
        return(receiveMsg[3]==UNLOCK);
    }

    private void updateDoorStatus(boolean isUnlocked){
        if(isUnlocked){
            tvDoorStatus.setText("Unlocked");
        }else{
            tvDoorStatus.setText("Locked");
        }
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return ("["+strDate+"] ");
    }


    private class ControlThread extends Thread {
        private DatagramSocket socket;
        private MainActivity mainActivity;
        public boolean statusObtainedByThread;
        public ControlThread (MainActivity mainActivity)
        {
            this.mainActivity = mainActivity;
            this.socket = mainActivity.socket;
        }

        public void run() {
            byte[] sendMsg = new byte[100];
            byte[] recMsg = new byte[100];
            sendMsg[0] = mainActivity.housenumber;
            sendMsg[1] = mainActivity.doornumber;
            sendMsg[2] = mainActivity.GET_DOR;

            DatagramPacket sendPacket;
            DatagramPacket receivePacket;
            receivePacket = new DatagramPacket(recMsg, recMsg.length);
            try {
                while(true) {
                    sendPacket = new DatagramPacket(sendMsg, sendMsg.length, hostAddress, portnumber);
                    socket.send(sendPacket);
                    socket.receive(receivePacket);
                    //TODO: Add short delay to remove asynchronicity
                    final byte[] receiveMsg = receivePacket.getData();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusObtainedByThread = (receiveMsg[3] == UNLOCK);
                            if (statusObtainedByThread != mainActivity.currentDoorState){
                                mainActivity.currentDoorState = statusObtainedByThread;
                                int doorNum = mainActivity.doornumber;
                                mainActivity.eventArrayList.add(0, (getCurrentTimeStamp() + eventString.replace("doornum", Integer.toString(doorNum)))+ ((statusObtainedByThread) ? "unlocked." : "locked."));
                                mainActivity.adapter.notifyDataSetChanged();
                            }
                            mainActivity.updateDoorStatus(statusObtainedByThread);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * Created by brendanlucas on 2017-03-27.
     */
    private class CreateAddress extends AsyncTask<String, Void, InetAddress>{

        private Exception e;

        @Override
        protected InetAddress doInBackground(String... params) {
            try{
                if (params[0].equals("local")) {
                    return InetAddress.getLocalHost();
                }else return InetAddress.getByName(params[0]);
            } catch(UnknownHostException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

