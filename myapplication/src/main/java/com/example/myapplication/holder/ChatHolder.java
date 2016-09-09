package com.example.myapplication.holder;

import com.example.myapplication.database.model.ChatModel;
import com.example.myapplication.entity.ChatContentType;

import java.util.Date;
import java.util.UUID;


public class ChatHolder
{

    public Date deliverDate;
    public Date readDate;
    public int state;
    public ChatContentType contentType;
    public String alertMessage;
    public UUID chatId;
    public String date;
    public String time;
    public UUID contactUserId;
    public String path;
    public ChatBaseContentHolder content;
    public int progress = -1;

    public boolean needUpload;


    public boolean isMyMessage()
    {
        return state < ChatModel.RECEIVER_STATE;
    }

}
