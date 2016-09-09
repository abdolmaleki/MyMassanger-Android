package com.example.myapplication.entity;

public class ChatVoiceContent extends ChatBaseContent
{

    public String duration;
    public String size;

    public ChatVoiceContent()
    {
        super("Voice");
    }

    public ChatVoiceContent(String path, String extension)
    {
        super("Voice");
        this.pathForUpload = path;
        this.extension = extension;
    }
}
