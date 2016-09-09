package com.example.myapplication.entity;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class ChatContentTypeDeserializer implements JsonDeserializer<ChatContentType>, JsonSerializer<ChatContentType>
{


    public ChatContentType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        try
        {
            int v = json.getAsInt();
            for (ChatContentType e : ChatContentType.values())
            {
                if (e.value == v)
                {
                    return e;
                }
            }

        }
        catch (Exception ignored)
        {
        }

        throw new RuntimeException("##### Deserialize ChatBaseContentDeserializer");

    }

    @Override
    public JsonElement serialize(ChatContentType src, Type typeOfSrc, JsonSerializationContext context)
    {

        try
        {
            return context.serialize(src.value, Integer.class);
        }
        catch (Exception ignored)
        {
        }

        throw new RuntimeException("##### Serialize ChatBaseContentDeserializer");

    }


}