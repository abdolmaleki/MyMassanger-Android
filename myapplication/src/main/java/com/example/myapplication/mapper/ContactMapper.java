package com.example.myapplication.mapper;

import com.example.myapplication.connection.socket.dto.ContactDto;
import com.example.myapplication.database.model.ContactModel;
import com.example.myapplication.holder.ChatUserHolder;

import java.util.ArrayList;
import java.util.List;


public final class ContactMapper
{

    public static ContactModel ConvertDtoToModel(ContactDto dto)
    {
        ContactModel model = new ContactModel(dto.guid);
        model.firstName = dto.firstName;
        model.lastName = dto.lastName;
        model.phoneNumber = dto.imageUrl;
        model.imageUrl = dto.imageUrl;

        return model;
    }

    public static ChatUserHolder ConvertModelToHolder(ContactModel model)
    {
        ChatUserHolder holder = new ChatUserHolder();
        holder.name = model.firstName + " " + model.lastName;
        holder.imageUrl = model.imageUrl;
        holder.guid = model.getGuid();

        return holder;
    }

    public static List<ChatUserHolder> ConvertModelToHolder(List<ContactModel> models)
    {
        List<ChatUserHolder> holders = new ArrayList<>();

        for (ContactModel model : models)
        {
            holders.add(ConvertModelToHolder(model));
        }

        return holders;
    }
}
