package com.example.myapplication.connection.socket.dto;

import java.util.List;
import java.util.UUID;

import ir.hfj.library.connection.socket.dto.BaseDto;

public class ChatReadReportResponsibleDto extends BaseDto
{

    ///Important! All [chatGuids] should have one owner
    public List<UUID> chatGuids;

    public static final class Result extends BaseDto.Result<ChatReadReportResponsibleDto>
    {
        @Override
        public boolean isValid()
        {
            return super.isValid() && request != null && request.chatGuids != null;
        }
    }
}
