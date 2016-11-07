package com.example.myapplication.connection.socket.dto;

import com.example.myapplication.entity.ChatBaseContent;
import com.example.myapplication.entity.ChatContentType;

import java.util.UUID;

import ir.hfj.library.connection.socket.dto.BaseDto;


public class ChatResponsibleDto extends BaseDto
{

    public ChatBaseContent content;
    public ChatContentType contentType;
    public UUID chatId;
    public UUID receiverUserId;
    public String phoneNumber;

    public ChatResponsibleDto(UUID chatId)
    {
        this.chatId = chatId;
    }

    public static final class Result extends BaseDto.Result<ChatResponsibleDto>
    {

        public UUID chatId;
        public boolean isNewId;

    }

}
