package com.example.myapplication.database.model;

import com.activeandroid.annotation.Column;

import java.util.Date;
import java.util.UUID;

import ir.hfj.library.database.model.BaseModel;

public abstract class BaseContentModel extends BaseModel
{

    public static final String _studentId = "_studentId";
    @Column(name = "_studentId")
    public long studentId;

    public static final String _termId = "_termId";
    @Column(name = "_termId")
    public UUID termId;

    public static final String _publishDate = "_publishDate";
    @Column(name = "_publishDate")
    public Date publishDate;

    public BaseContentModel()
    {

    }

    public BaseContentModel(UUID guid)
    {
        super(guid);
    }
}
