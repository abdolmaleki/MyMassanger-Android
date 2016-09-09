package com.example.myapplication.connection.socket.dto;

import java.util.Date;
import java.util.UUID;

import ir.hfj.library.connection.socket.dto.BaseDto;

public abstract class BaseContentDto extends BaseDto
{
    public UUID guid;
    public UUID studentGuid;
    public Date publishDate;
    public UUID termGuid;
}
