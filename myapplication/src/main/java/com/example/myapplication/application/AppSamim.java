package com.example.myapplication.application;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.serializer.BigDecimalSerializer;
import com.activeandroid.serializer.CalendarSerializer;
import com.activeandroid.serializer.FileSerializer;
import com.activeandroid.serializer.SqlDateSerializer;
import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.serializer.UUIDSerializer;
import com.activeandroid.serializer.UtilDateSerializer;
import com.example.myapplication.database.model.ChatModel;
import com.example.myapplication.database.model.ContactModel;
import com.example.myapplication.database.model.serializer.ChatContentDateSerializer;
import com.example.myapplication.database.model.serializer.ChatContentTypeDateSerializer;
import com.example.myapplication.entity.ChatBaseContent;
import com.example.myapplication.entity.ChatBaseContentDeserializer;
import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.entity.ChatContentTypeDeserializer;
import com.example.myapplication.service.MyMessangerService;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;
import java.util.Date;

import ir.hfj.library.application.App;
import ir.hfj.library.application.AppConfig;
import ir.hfj.library.database.DbBase;
import ir.hfj.library.database.model.UserSettingModel;
import ir.hfj.library.receiver.SamimAction;
import ir.hfj.library.util.ExclusionGsonStrategies;
import microsoft.aspnet.signalr.client.DateSerializer;

public class AppSamim extends App
{

    public AppSamim()
    {
        //=====================================================================================================
        AppConfig.LOG_TAG = "MyMessanger";
        AppConfig.DEBUG = true;
        AppConfig.ROLE = 1;


//        AppConfig.NETWORK_HOST_WS = "http://samim.aligorji.ir:8080/api";
//        AppConfig.NETWORK_HOST_SR = "http://samim.aligorji.ir:8080/";
//        AppConfig.NETWORK_HOST_WEB = "http://samim.aligorji.ir:8080/";

        AppConfig.NETWORK_HOST_WS = "http://192.168.13.33:5009/api";
        AppConfig.NETWORK_HOST_SR = "http://192.168.13.33:5009/";
        AppConfig.NETWORK_HOST_WEB = "http://192.168.13.33:5009/";

        AppConfig.NETWORK_MESSANGER_HUB = "MyMessangerHub";
        AppConfig.NETWORK_HOST_TRY_CONNECT_DURATION = 1000 * 3;
        AppConfig.NETWORK_HOST_PING = AppConfig.NETWORK_HOST_SR + "TestConnection/Ping.html";
         AppConfig.NETWORK_HOST_TRY_CONNECT_EMERGENCY_DURATION = 1000 * 10;//1000 * 60 * 30;

        AppConfig.AUTO_REFRESH_INTERVAL = 30;//minute

        AppConfig.setGsonSetting(new GsonBuilder()
                                         .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                                         .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                                         .registerTypeAdapter(Date.class, new DateSerializer())
                                         .registerTypeAdapter(ChatBaseContent.class, new ChatBaseContentDeserializer())
                                         .registerTypeAdapter(ChatContentType.class, new ChatContentTypeDeserializer())
                                         .setExclusionStrategies(new ExclusionGsonStrategies())
                                         .create());
        //======================================================================================================
    }

    @Override
    public void onCreate()
    {
        SamimAction.ACTIVATION_EXPIRED = "ir.hfj.samim.parent.activation.expired";
        SamimAction.ACTIVATION_SUCCESSFUL = "ir.hfj.samim.parent.activation.successful";
        SamimAction.ACTIVATION_NULL = "ir.hfj.samim.parent.activation.null";
        super.onCreate();
        MyMessangerService.start(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SamimAction.ACTIVATION_EXPIRED);
        intentFilter.addAction(SamimAction.ACTIVATION_SUCCESSFUL);
        registerReceiver(mActivationReceiver, intentFilter);
    }

    @Override
    protected void ActiveAndroidInitialize()
    {
        Configuration.Builder config = new Configuration.Builder(this);
        //
        config.setDatabaseName("messanger.db");
                config.setDatabaseVersion(1);
        //
        config.addModelClasses(
                UserSettingModel.class,
                ChatModel.class,
                ContactModel.class
        );

        config.addTypeSerializers(
                ChatContentDateSerializer.class,
                ChatContentTypeDateSerializer.class,
                UUIDSerializer.class,
                BigDecimalSerializer.class,
                CalendarSerializer.class,
                SqlDateSerializer.class,
                UtilDateSerializer.class,
                FileSerializer.class,
                TypeSerializer.class);

        ActiveAndroid.initialize(config.create());
    }

    public static AppSamim getInstance(Activity activity)
    {
        return ((AppSamim) activity.getApplication());
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        unregisterReceiver(mActivationReceiver);
    }

    BroadcastReceiver mActivationReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (AppConfig.DEBUG)
            {
                Log.i(AppConfig.LOG_TAG, "App Take BroadcastReceiver: " + intent.getAction());
            }
            if (intent.getAction().equalsIgnoreCase(SamimAction.ACTIVATION_SUCCESSFUL))
            {
                mUserSetting = DbBase.UserSetting.select();

            }
            else if (intent.getAction().equalsIgnoreCase(SamimAction.ACTIVATION_EXPIRED))
            {
                if (mUserSetting == null)
                {
                    mUserSetting = DbBase.UserSetting.select();
                }
                if (mUserSetting != null)
                {
                    mUserSetting.expired = true;
                }

            }

        }

    };
}