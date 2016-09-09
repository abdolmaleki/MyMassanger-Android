package com.example.myapplication.connection.socket.dto;

import java.util.UUID;

import ir.hfj.library.connection.socket.dto.BaseDto;

public class ChatTypingReportDto extends BaseDto
{
    public UUID chatUserGuid;
    public boolean isTyping;
}
