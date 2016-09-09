package com.example.myapplication.connection.restapi.jto;

import com.example.myapplication.entity.ChatContentType;

import ir.hfj.library.connection.restapi.jto.PostBackJto;
import ir.hfj.library.connection.restapi.jto.PostJto;

public abstract class FileJto
{

    public final static class Post extends PostJto
    {

        public String path;
        public ChatContentType contentType;
        public String extension;

        public Post(String p, ChatContentType type, String ext)
        {
            path = p;
            contentType = type;
            extension = ext;
        }

    }

    public final static class PostBack extends PostBackJto
    {
        public String token;
    }

}
