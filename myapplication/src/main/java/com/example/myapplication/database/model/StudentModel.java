package com.example.myapplication.database.model;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.example.myapplication.database.Db;

import java.util.List;
import java.util.UUID;

import ir.hfj.library.database.model.BaseModel;

@Table(name = StudentModel.__table, id = StudentModel.__id)
public class StudentModel extends BaseModel
{

    public static final String __table = "_student";

    public static final String _name = "_name";
    @Column(name = "_name")
    public String name;

    public static final String _family = "_family";
    @Column(name = "_family")
    public String family;

    public static final String _gender = "_gender";
    @Column(name = "_gender")
    public boolean gender;

    public static final String _imageUrl = "_imageUrl";
    @Column(name = "_imageUrl")
    public String imageUrl;

    public static final String _nationalCode = "_nationalCode";
    @Column(name = "_nationalCode")
    public String nationalCode;

    public static final String _code = "_code";
    @Column(name = "_code")
    public String code;

    public static final String _level = "_level";
    @Column(name = "_level")
    public String level;

    public StudentModel()
    {
        super();
    }

    public StudentModel(UUID guid)
    {
        super(guid);
    }

    public List<ContactModel> getTeachers()
    {
        return Db.Teacher.selectByStudentId(this.getId());
    }
}