package com.example.myapplication.connection.socket.dto;

import java.util.UUID;

import ir.hfj.library.connection.socket.dto.BaseDto;

public final class ChatDeliverDto extends BaseDto
{
    public UUID chatId;
    public boolean isDeliver;
    public long intervalTime;
}
