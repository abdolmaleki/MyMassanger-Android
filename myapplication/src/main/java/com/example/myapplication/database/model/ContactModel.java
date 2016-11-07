package com.example.myapplication.database.model;


import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.UUID;

import ir.hfj.library.database.model.BaseModel;

@Table(name = ContactModel.__table, id = ContactModel.__id)
public class ContactModel extends BaseModel
{

    public static final String __table = "_contact";

    public static final String _firstName = "_firstName";
    @Column(name = "_firstName")
    public String firstName;

    public static final String _lastName = "_lastName";
    @Column(name = "_lastName")
    public String lastName;

    public static final String _imageUrl = "_imageUrl";
    @Column(name = "_imageUrl")
    public String imageUrl;

    public static final String _phoneNumber = "_phoneNumber";
    @Column(name = "_phoneNumber", notNull = true)
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
