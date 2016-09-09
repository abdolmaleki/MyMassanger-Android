package com.example.myapplication.holder;

import com.example.myapplication.entity.MediaTransferState;
import com.example.myapplication.factory.FileManager;

public abstract class ChatBaseContentHolder
{
    public MediaTransferState state;
    public String fileToken;
    public String extension;
    public int progress;

    public String getThumbnailUrl()
    {
        return FileManager.getThumbnailUrl(fileToken);
    }

}
