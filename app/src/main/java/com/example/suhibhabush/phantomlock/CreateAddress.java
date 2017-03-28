package com.example.suhibhabush.phantomlock;

import java.net.*;
import java.net.UnknownHostException;
import android.os.AsyncTask;

/**
 * Created by brendanlucas on 2017-03-27.
 *

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
*/