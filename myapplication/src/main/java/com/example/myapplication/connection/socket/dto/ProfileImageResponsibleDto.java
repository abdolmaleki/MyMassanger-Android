package com.example.myapplication.connection.socket.dto;

import ir.hfj.library.connection.socket.dto.BaseDto;

public class ProfileImageResponsibleDto extends BaseDto
{

    public static final class Result extends BaseDto.Result
    {

        public String profileImage;

        @Override
        public boolean isValid()
        {
            return super.isValid();
        }
    }
}
