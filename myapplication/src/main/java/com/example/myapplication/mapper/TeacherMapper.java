package com.example.myapplication.mapper;

import com.example.myapplication.connection.socket.dto.TeacherDto;
import com.example.myapplication.database.model.TeacherModel;
import com.example.myapplication.holder.ChatUserHolder;

import java.util.ArrayList;
import java.util.List;


public final class TeacherMapper
{

    public static TeacherModel ConvertDtoToModel(TeacherDto dto, long studentId)
    {
        TeacherModel model = new TeacherModel(dto.guid);
        model.imageUrl = dto.imageUrl;
        model.name = dto.name;
        model.lessonName = dto.lessonName;
        model.studentId = studentId;

        return model;
    }

    public static ChatUserHolder ConvertModelToHolder(TeacherModel model)
    {
        ChatUserHolder holder = new ChatUserHolder();
        holder.name = model.name;
        holder.imageUrl = model.imageUrl;
        holder.guid = model.getGuid();

        return holder;
    }

    public static List<ChatUserHolder> ConvertModelToHolder(List<TeacherModel> models)
    {
        List<ChatUserHolder> holders = new ArrayList<>();

        for (TeacherModel model : models)
        {
            holders.add(ConvertModelToHolder(model));
        }

        return holders;
    }
}
