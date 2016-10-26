package com.example.myapplication.connection.socket.dto;


import java.util.List;

import ir.hfj.library.connection.socket.dto.BaseDto;

public class ContactResponsibleDto extends BaseDto
{
    public List<String> phoneNumbers;

    public static final class Result extends BaseDto.Result
    {

        public List<ContactDto> contactDtos;

        @Override
        public boolean isValid()
        {
            return super.isValid();
        }
    }
}

