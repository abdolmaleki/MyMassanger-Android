package com.example.myapplication.database.model.serializer;

import com.activeandroid.serializer.TypeSerializer;
import com.example.myapplication.entity.ChatContentType;

public final class ChatContentTypeDateSerializer extends TypeSerializer
{

    @Override
    public Class<?> getDeserializedType()
    {
        return ChatContentType.class;
    }

    @Override
    public Class<?> getSerializedType()
    {
        return Integer.class;
    }

    @Override
    public Integer serialize(Object data)
    {
        if (data instanceof ChatContentType)
        {
            ChatContentType base = (ChatContentType) data;
            return base.value;
        }
        throw new RuntimeException("##### ChatContentTypeDateSerializer serialize");
    }

    @Override
    public ChatContentType deserialize(Object data)
    {
        if (data instanceof Integer)
        {
            int v = (int) data;
            for (ChatContentType e : ChatContentType.values())
            {
                if (e.value == v)
                {
                    return e;
                }
            }
        }
        throw new RuntimeException("##### ChatContentTypeDateSerializer deserialize");
    }
}
