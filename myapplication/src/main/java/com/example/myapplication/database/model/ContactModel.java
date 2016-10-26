package com.example.myapplication.database.model;


import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.UUID;

import ir.hfj.library.database.model.BaseModel;

@Table(name = ContactModel.__table, id = ContactModel.__id)
public class ContactModel extends BaseModel
{

    public static final String __table = "_contact";

    public static final String _name = "_name";
    @Column(name = "_name")
    public String name;

    public static final String _imageUrl = "_imageUrl";
    @Column(name = "_imageUrl")
    public String imageUrl;

    public static final String _phoneNumber = "_phoneNumber";
    @Column(name = "_phoneNumber")
    public String phoneNumber;

    public ContactModel(UUID guid)
    {
        super(guid);
    }
    public ContactModel()
    {
        super();
    }

}
