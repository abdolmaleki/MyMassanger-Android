package com.example.myapplication.connection.socket.dto;

import java.util.List;
import java.util.UUID;

import ir.hfj.library.connection.socket.dto.BaseDto;

public final class ChatReadReportDto extends BaseDto
{
    public List<UUID> chatGuids;
    public boolean isOwner;
    public long intervalTime;
}
