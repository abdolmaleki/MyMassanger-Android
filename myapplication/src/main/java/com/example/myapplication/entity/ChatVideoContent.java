package com.example.myapplication.entity;


public class ChatVideoContent extends ChatBaseContent
{

    public ChatVideoContent()
    {
        super("Video");
    }

    public ChatVideoContent(String path, String extension)
    {
        super("Video");
        this.pathForUpload = path;
        this.extension = extension;
    }
}
