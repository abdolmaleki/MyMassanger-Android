package com.example.myapplication.entity;

public class ChatFileContent extends ChatBaseContent
{

    public String name;
    public String size;

    public ChatFileContent()
    {
        super("File");
    }
    public ChatFileContent(String path, String extension)
    {
        super("File");
        this.pathForUpload = path;
        this.extension = extension;
    }
}
