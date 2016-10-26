package com.example.myapplication.mapper;

import com.example.myapplication.connection.socket.dto.ChatDto;
import com.example.myapplication.connection.socket.dto.ChatResponsibleDto;
import com.example.myapplication.database.Db;
import com.example.myapplication.database.model.ChatModel;
import com.example.myapplication.database.model.ContactModel;
import com.example.myapplication.dictionary.DataDictionary;
import com.example.myapplication.entity.ChatBaseContent;
import com.example.myapplication.entity.ChatFileContent;
import com.example.myapplication.entity.ChatImageContent;
import com.example.myapplication.entity.ChatTextContent;
import com.example.myapplication.entity.ChatVideoContent;
import com.example.myapplication.entity.ChatVoiceContent;
import com.example.myapplication.holder.ChatBaseContentHolder;
import com.example.myapplication.holder.ChatFileContentHolder;
import com.example.myapplication.holder.ChatHistoryHolder;
import com.example.myapplication.holder.ChatHolder;
import com.example.myapplication.holder.ChatImageContentHolder;
import com.example.myapplication.holder.ChatTextContentHolder;
import com.example.myapplication.holder.ChatVideoContentHolder;
import com.example.myapplication.holder.ChatVoiceContentHolder;

import java.util.List;
import java.util.UUID;

import ir.hfj.library.util.Helper;

public class ChatMapper
{

    public static ChatHolder convertModelToHolder(ChatModel model)
    {
        if (model != null)
        {
            ChatHolder holder = new ChatHolder();

            holder.chatId = model.getGuid();
            holder.date = Helper.getDate(model.date);
            holder.time = Helper.getTime(model.date);
            holder.contactUserId = model.contactUserId;
            holder.deliverDate = model.deliverDate;
            holder.readDate = model.readDate;
            holder.state = model.state;
            holder.contentType = model.contentType;
            holder.path = model.path;
            holder.needUpload = model.needUpload;
            holder.progress = -1;
            holder.content = convertModelContentToHolderContent(model.content);


            return holder;
        }
        return null;
    }

    public static ChatBaseContentHolder convertModelContentToHolderContent(ChatBaseContent content)
    {
        ChatBaseContentHolder holder = null;
        if (content instanceof ChatImageContent)
        {
            holder = new ChatImageContentHolder();
            ((ChatImageContentHolder) holder).comment = ((ChatImageContent) content).comment;


        }
        else if (content instanceof ChatTextContent)
        {
            holder = new ChatTextContentHolder();
            ((ChatTextContentHolder) holder).text = ((ChatTextContent) content).text;
        }

        else if (content instanceof ChatFileContent)
        {
            holder = new ChatFileContentHolder();
            ((ChatFileContentHolder) holder).name = ((ChatFileContent) content).name;
            ((ChatFileContentHolder) holder).size = ((ChatFileContent) content).size;
        }

        else if (content instanceof ChatVideoContent)
        {
            holder = new ChatVideoContentHolder();
        }

        else if (content instanceof ChatVoiceContent)
        {
            holder = new ChatVoiceContentHolder();
            ((ChatVoiceContentHolder) holder).duration = ((ChatVoiceContent) content).duration;
            ((ChatVoiceContentHolder) holder).size = ((ChatVoiceContent) content).size;
        }

        else
        {
            return null;
        }

        holder.fileToken = content.fileToken;
        holder.extension = content.extension;

        return holder;
    }


    public static DataDictionary<UUID, ChatHolder> convertModelToHolder(List<ChatModel> models)
    {
        DataDictionary<UUID, ChatHolder> holders = new DataDictionary<>();

        if (models != null)
        {
            for (ChatModel model : models)
            {
                holders.add(model.getGuid(), convertModelToHolder(model));
            }
        }

        return holders;
    }

    public static ChatModel convertDtoToModel(ChatDto dto, int state)
    {

        if (dto != null)
        {
            ChatModel model = new ChatModel(dto.chatId);
            model.content = dto.content;
            model.contentType = dto.contentType;
            model.state = state;
            if (dto.isOwner)
            {
                model.contactUserId = dto.receiverUserId;
            }
            else
            {
                model.contactUserId = dto.senderUserId;
            }
            model.deliverDate = null;
            model.readDate = null;
            model.path = "";
            model.needUpload = false;
            model.date = Helper.getDateTimeNow(dto.intervalTime);
            return model;
        }

        return null;
    }

    public static ChatModel convertDtoToModel(ChatResponsibleDto dto, int state)
    {

        if (dto != null)
        {
            ChatModel model = new ChatModel(dto.chatId);
            model.content = dto.content;
            model.contentType = dto.contentType;
            model.date = Helper.getDateTimeNow(0);
            model.contactUserId = dto.receiverUserId;
            model.deliverDate = null;
            model.readDate = null;
            model.state = state;
            model.path = dto.content.pathForUpload;
            model.needUpload = true;
            return model;
        }

        return null;
    }

    public static ChatResponsibleDto convertModelToDto(ChatModel model)
    {

        if (model != null)
        {
            ChatResponsibleDto dto = new ChatResponsibleDto(model.getGuid());
            dto.content = model.content;
            dto.receiverUserId = model.contactUserId;
            dto.contentType = model.contentType;
            if (model.needUpload)
            {
                dto.content.pathForUpload = model.path;
            }
            return dto;
        }

        return null;
    }

    public static ChatModel clone(ChatModel model, UUID guid)
    {
        if (model != null)
        {
            ChatModel newModel = new ChatModel(guid);
            newModel.content = model.content;
            newModel.contentType = model.contentType;
            newModel.date = model.date;
            newModel.contactUserId = model.contactUserId;
            newModel.deliverDate = model.deliverDate;
            newModel.readDate = model.readDate;
            newModel.path = model.path;
            newModel.needUpload = model.needUpload;
            newModel.state = model.state;
            return newModel;
        }

        return null;
    }

    public static ChatHistoryHolder getChatHistory(UUID contactGuid)
    {
        if (contactGuid != null)
        {
            ContactModel contactModel = Db.Teacher.selectByGuid(contactGuid);
            ChatModel lastChatModel = Db.Chat.selectHistory(contactGuid);

            if (lastChatModel != null)
            {
                ChatHistoryHolder holder = new ChatHistoryHolder();
                holder.description = lastChatModel.getSummary();
                holder.count = Db.Chat.selectUnreadHistoryCount(contactGuid);
                holder.name = contactModel.name;
                holder.time = Helper.getTime(lastChatModel.date);
                holder.contactGuid = contactModel.getGuid();
                holder.id = contactModel.getId();
                holder.img = contactModel.imageUrl;

                return holder;
            }
        }

        return null;
    }
}
