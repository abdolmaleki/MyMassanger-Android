package com.example.myapplication.connection.socket.dto;

import com.example.myapplication.entity.ChatBaseContent;
import com.example.myapplication.entity.ChatContentType;

import java.util.UUID;

import ir.hfj.library.connection.socket.dto.BaseDto;


public final class ChatDto extends BaseDto
{
    public UUID chatId;
    public ChatBaseContent content;
    public ChatContentType contentType;
    public UUID senderUserId;
    public UUID receiverUserId;
    public long intervalTime;
    public boolean isOwner;
    public String phoneNumber;

}
