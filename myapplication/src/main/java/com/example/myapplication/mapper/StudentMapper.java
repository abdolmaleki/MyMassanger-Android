package com.example.myapplication.mapper;

import com.example.myapplication.database.model.StudentModel;
import com.example.myapplication.holder.StudentHolder;

import java.util.ArrayList;
import java.util.List;


public final class StudentMapper
{

    public static StudentHolder convertModelToHolder(StudentModel model)
    {

        if (model != null)
        {
            StudentHolder holder = new StudentHolder();
            holder.id = model.getId();
            holder.guid = model.getGuid();
            holder.name = model.name + " " + model.family;
            holder.imageUrl = model.imageUrl;
            return holder;
        }
        return null;
    }

    public static List<StudentHolder> convertModelToHolder(List<StudentModel> models)
    {
        List<StudentHolder> studentsHolder = new ArrayList<>();
        if (models != null)
        {
            for (StudentModel model : models)
            {
                studentsHolder.add(convertModelToHolder(model));
            }
        }
        return studentsHolder;

    }

}
