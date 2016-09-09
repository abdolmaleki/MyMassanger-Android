package com.example.myapplication.entity;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class ChatBaseContentDeserializer implements JsonDeserializer<ChatBaseContent>, JsonSerializer<ChatBaseContent>
{

    public ChatBaseContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        try
        {
            JsonObject je = json.getAsJsonObject();

            String className = je.get("C").getAsString();

            String typeStr = ChatBaseContent.class.getPackage().getName() + ".Chat" + className + "Content";

            Type type = Class.forName(typeStr);

            return context.deserialize(je, type);
        }
        catch (Exception ignored)
        {
        }

        throw new RuntimeException("##### Deserialize ChatBaseContentDeserializer");

    }

    @Override
    public JsonElement serialize(ChatBaseContent src, Type typeOfSrc, JsonSerializationContext context)
    {

        try
        {
            String className = src.c;

            String typeStr = ChatBaseContent.class.getPackage().getName() + ".Chat" + className + "Content";

            Type type = Class.forName(typeStr);

            return context.serialize(src, type);
        }
        catch (Exception ignored)
        {
        }

        throw new RuntimeException("##### Serialize ChatBaseContentDeserializer");

    }

}
