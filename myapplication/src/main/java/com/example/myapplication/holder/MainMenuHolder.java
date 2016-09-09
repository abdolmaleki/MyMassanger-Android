package com.example.myapplication.holder;


import android.app.Activity;

public class MainMenuHolder
{

    public int icon;
    public int label;
    public Class<? extends Activity> activity;

    public MainMenuHolder(int icon, int label, Class<? extends Activity> activity)
    {
        this.icon = icon;
        this.label = label;
        this.activity = activity;
    }

}
