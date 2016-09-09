package com.example.myapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.myapplication.service.SamimService;

public class BootReceiver extends BroadcastReceiver
{


    @Override
    public void onReceive(Context context, Intent intent)
    {
        SamimService.start(context);
    }
}
