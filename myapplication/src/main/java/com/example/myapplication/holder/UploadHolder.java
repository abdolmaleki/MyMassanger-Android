package com.example.myapplication.holder;

import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.entity.MediaTransferState;

import java.io.Serializable;
import java.util.UUID;

public abstract class UploadHolder
{
    public static class Send implements Serializable
    {
        public UUID guid;
        public ChatContentType type;
        public String extension;
        public String title;
        public String path;
    }

    public static class Received implements Serializable
    {
        public UUID guid;
        public String title;
        public ChatContentType type;
        public String message;
        public MediaTransferState state;
        public int progress;
        public String fileToken;
        public String extension;
    }

}
