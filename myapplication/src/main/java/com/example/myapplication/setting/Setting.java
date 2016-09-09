package com.example.myapplication.setting;

import android.app.Activity;
import android.content.SharedPreferences;
import com.example.myapplication.application.Constant;

import java.util.Date;


public class Setting
{

    public static void SaveRefreshDateTime(Activity activity, String key, Date date)
    {
        SharedPreferences.Editor editor = activity.getSharedPreferences(Constant.Preference.REFRESH_TIME, Activity.MODE_PRIVATE).edit();
        editor.putLong(key, date.getTime());
        editor.apply();
    }

    public static Date LoadRefreshDateTime(Activity activity, String key)
    {
        SharedPreferences prefs = activity.getSharedPreferences(Constant.Preference.REFRESH_TIME, Activity.MODE_PRIVATE);
        long mili = prefs.getLong(key, -1);
        if (mili < 0)
        {
            return null;
        }
        return new Date(mili);
    }

}
