package com.example.myapplication.connection.socket.dto;


import java.util.UUID;
import ir.hfj.library.connection.socket.dto.BaseDto;

public class ContactDto extends BaseDto
{
    public String name;
    public String imageUrl;
    public UUID guid;
    public String phoneNumber;
}
