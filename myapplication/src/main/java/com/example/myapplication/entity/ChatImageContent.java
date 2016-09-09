package com.example.myapplication.entity;


public class ChatImageContent extends ChatBaseContent
{
    public String comment;

    public ChatImageContent()
    {
        super("Image");
    }

    public ChatImageContent(String path, String extension)
    {
        super("Image");
        this.pathForUpload = path;
        this.extension = extension;
    }

}
