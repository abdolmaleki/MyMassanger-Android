package com.example.myapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.R;

import ir.hfj.library.activity.ActivationActivity;
import ir.hfj.library.application.App;

public class MainActivity extends Activity
{


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activationIfNeeded();
    }

    private void activationIfNeeded()
    {
        if (App.getInstance(this).isExpired())
        {
            Intent myIntent = new Intent(this, ActivationActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myIntent.putExtra(ActivationActivity.KEY_CAUSE, App.getInstance(this).isUserSetting() ? ActivationActivity.PARAM_CAUSE_EXPIRE : ActivationActivity.PARAM_CAUSE_ACTIVE);
            startActivity(myIntent);
        }
    }
}
