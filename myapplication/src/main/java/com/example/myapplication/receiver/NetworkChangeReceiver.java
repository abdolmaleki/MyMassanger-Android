package com.example.myapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.myapplication.service.SamimService;

import ir.hfj.library.application.AppConfig;

public class NetworkChangeReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {

        if (AppConfig.DEBUG)
        {
            Log.i(AppConfig.LOG_TAG, "NetworkChangeReceiver");
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null)
        {
            if (info.isConnected())
            {
                //start service
                SamimService.start(context);
            }

        }


    }
}