package com.example.myapplication.connection.socket.dto;


import java.util.List;
import java.util.UUID;

import ir.hfj.library.connection.socket.dto.BaseDto;

public class TeacherResponsibleDto extends BaseDto
{

    public UUID studentGuid;
    public int year;

    public static final class Result extends BaseDto.Result
    {

        public UUID studentGuid;
        public List<TeacherDto> teacherDtos;

        @Override
        public boolean isValid()
        {
            return super.isValid();
        }
    }
}

