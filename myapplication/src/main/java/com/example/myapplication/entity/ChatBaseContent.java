package com.example.myapplication.entity;


import java.io.Serializable;
import java.util.UUID;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.util.ExcludeGson;

public abstract class ChatBaseContent implements Serializable
{
    //class name(type)
    public final String c;
    public String fileToken;
    public String extension;

    //replay message
    public UUID replyChatId;

    @ExcludeGson
    public String pathForUpload;



    protected ChatBaseContent(String className)
    {
        this.c = className;
        this.pathForUpload = null;
        this.extension = null;
    }

    public String toJson()
    {
        return AppConfig.getGsonSetting().toJson(this);
    }

    public static ChatBaseContent fromJson(String json)
    {
        return AppConfig.getGsonSetting().fromJson(json, ChatBaseContent.class);
    }

}
