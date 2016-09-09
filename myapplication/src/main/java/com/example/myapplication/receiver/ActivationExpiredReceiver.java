package com.example.myapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.database.Db;

import ir.hfj.library.activity.ActivationActivity;
import ir.hfj.library.application.AppConfig;
import ir.hfj.library.receiver.SamimAction;

public class ActivationExpiredReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(SamimAction.ACTIVATION_EXPIRED))
        {

            if (AppConfig.DEBUG)
            {
                Log.i(AppConfig.LOG_TAG, "ActivationExpiredReceiver : " + intent.getAction());
            }

            Db.UserSetting.updateUserSettingExpire(true);

            Intent myIntent = new Intent(context, ActivationActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myIntent.putExtra(ActivationActivity.KEY_CAUSE, ActivationActivity.PARAM_CAUSE_EXPIRE);
            context.startActivity(myIntent);

        }

    }
}
