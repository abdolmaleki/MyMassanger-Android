package com.example.myapplication.entity;

public class ChatTextContent extends ChatBaseContent
{
    public String text;

    public ChatTextContent()
    {
        super("Text");
    }

    public ChatTextContent(String text)
    {
        super("Text");
        this.text = text;
        this.extension = null;
    }
}
