package com.example.myapplication.database.model.serializer;

import com.activeandroid.serializer.TypeSerializer;
import com.example.myapplication.entity.ChatBaseContent;

public final class ChatContentDateSerializer extends TypeSerializer
{

    @Override
    public Class<?> getDeserializedType()
    {
        return ChatBaseContent.class;
    }

    @Override
    public Class<?> getSerializedType()
    {
        return String.class;
    }

    @Override
    public String serialize(Object data)
    {
        if (data instanceof ChatBaseContent)
        {
            ChatBaseContent base = (ChatBaseContent) data;
            return base.toJson();
        }
        throw new RuntimeException("##### ChatContentDateSerializer serialize");
    }

    @Override
    public ChatBaseContent deserialize(Object data)
    {
        if (data instanceof String)
        {
            return ChatBaseContent.fromJson((String) data);
        }
        throw new RuntimeException("##### ChatContentDateSerializer deserialize");
    }
}
