package com.example.myapplication.entity;


public enum ChatContentType
{

    Text("Text", 1),
    Image("Image", 2),
    Voice("Voice", 3),
    Video("Video", 4),
    File("File", 5);


    public final String name;
    public final int value;

    ChatContentType(String n, int v)
    {
        name = n;
        value = v;
    }

    @Override
    public String toString()
    {
        return name;
    }

}
