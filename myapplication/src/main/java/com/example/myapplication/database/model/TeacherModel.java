package com.example.myapplication.database.model;


import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.UUID;

import ir.hfj.library.database.model.BaseModel;

@Table(name = TeacherModel.__table, id = TeacherModel.__id)
public class TeacherModel extends BaseModel
{

    public static final String __table = "_teacher";

    public static final String _name = "_name";
    @Column(name = "_name")
    public String name;

    public static final String _imageUrl = "_imageUrl";
    @Column(name = "_imageUrl")
    public String imageUrl;

    public static final String _lessonName = "_lessonName";
    @Column(name = "_lessonName")
    public String lessonName;

    public static final String _studentId = "_studentId";
    @Column(name = "_studentId")
    public long studentId;

    public TeacherModel(UUID guid)
    {
        super(guid);
    }
    public TeacherModel()
    {
        super();
    }

}
