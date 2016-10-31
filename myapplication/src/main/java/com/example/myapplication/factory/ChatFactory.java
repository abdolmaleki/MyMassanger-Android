package com.example.myapplication.factory;

import android.app.Activity;

import com.example.myapplication.R;
import com.example.myapplication.connection.socket.dto.ChatResponsibleDto;
import com.example.myapplication.database.Db;
import com.example.myapplication.database.model.ChatModel;
import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.entity.ChatFileContent;
import com.example.myapplication.entity.ChatImageContent;
import com.example.myapplication.entity.ChatTextContent;
import com.example.myapplication.entity.ChatVideoContent;
import com.example.myapplication.entity.ChatVoiceContent;
import com.example.myapplication.mapper.ChatMapper;

import java.io.File;
import java.util.UUID;

import ir.hfj.library.util.Helper;


public class ChatFactory
{

    public static void Text(Activity activity, UUID userId, String text, EventListener listener)
    {
        try
        {

            if (text == null || text.isEmpty())
            {
                listener.onInvalidation(activity.getString(R.string.messanger_message_error_chat_empty));
            }
            else
            {

                //-----------------------
                ChatResponsibleDto dto = new ChatResponsibleDto(UUID.randomUUID());
                dto.content = new ChatTextContent(text);
                dto.contentType = ChatContentType.Text;
                dto.receiverUserId = userId;

                //-----------------------
                ChatModel model = ChatMapper.convertDtoToModel(dto, ChatModel.SENDER_STATE_SENDING);
                if (Db.Chat.insert(model))
                {
                    listener.onReadyForSend(dto, model);
                }
                else
                {
                    listener.onErrorDb(activity.getString(R.string.messanger_chat_message_error_db_error));
                }
            }


        }
        catch (Exception ex)
        {
            listener.onException(ex);
        }

    }

    public static void Image(Activity activity, UUID userId, String path, String comment, EventListener listener)
    {
        try
        {

            if (path.isEmpty())
            {
                listener.onInvalidation(activity.getString(R.string.messanger_error_image_empty));
            }
            else
            {

                //-----------------------
                ChatResponsibleDto dto = new ChatResponsibleDto(UUID.randomUUID());
                dto.content = new ChatImageContent(path, Helper.getExtension(path));
                dto.contentType = ChatContentType.Image;
                dto.receiverUserId = userId;
                //-----------------------
                ChatModel model = ChatMapper.convertDtoToModel(dto, ChatModel.SENDER_STATE_SENDING);
                if (Db.Chat.insert(model))
                {
                    listener.onReadyForSend(dto, model);
                }
                else
                {
                    listener.onErrorDb(activity.getString(R.string.messanger_chat_message_error_db_error));
                }
            }


        }
        catch (Exception ex)
        {
            listener.onException(ex);
        }

    }

    public static void File(Activity activity, UUID userId, String path, EventListener listener)
    {
        try
        {

            if (path.isEmpty())
            {
                listener.onInvalidation(activity.getString(R.string.messanger_message_error_file_empty));
            }
            else
            {
                //-----------------------
                ChatResponsibleDto dto = new ChatResponsibleDto(UUID.randomUUID());
                ChatFileContent chatFileContent = new ChatFileContent(path, Helper.getExtension(path));

                File file = new File(path);
                chatFileContent.name = file.getName();
                dto.content = chatFileContent;
                dto.contentType = ChatContentType.File;
                dto.receiverUserId = userId;

                //-----------------------
                ChatModel model = ChatMapper.convertDtoToModel(dto, ChatModel.SENDER_STATE_SENDING);
                if (Db.Chat.insert(model))
                {
                    listener.onReadyForSend(dto, model);
                }
                else
                {
                    listener.onErrorDb(activity.getString(R.string.messanger_chat_message_error_db_error));
                }
            }

        }
        catch (Exception ex)
        {
            listener.onException(ex);
        }
    }

    public static void Video(Activity activity, UUID userId, String path, EventListener listener)
    {
        try
        {

            if (path.isEmpty())
            {
                listener.onInvalidation(activity.getString(R.string.messanger_message_error_video_empty));
            }
            else
            {

                //-----------------------
                ChatResponsibleDto dto = new ChatResponsibleDto(UUID.randomUUID());
                dto.content = new ChatVideoContent(path, Helper.getExtension(path));
                dto.contentType = ChatContentType.Video;
                dto.receiverUserId = userId;
                //-----------------------
                ChatModel model = ChatMapper.convertDtoToModel(dto, ChatModel.SENDER_STATE_SENDING);
                if (Db.Chat.insert(model))
                {
                    listener.onReadyForSend(dto, model);
                }
                else
                {
                    listener.onErrorDb(activity.getString(R.string.messanger_chat_message_error_db_error));
                }
            }


        }
        catch (Exception ex)
        {
            listener.onException(ex);
        }

    }

    public static void Voice(Activity activity, UUID userId, String path, EventListener listener)
    {
        try
        {

            if (path.isEmpty())
            {
                listener.onInvalidation(activity.getString(R.string.messanger_message_error_voice_empty));
            }
            else
            {

                //-----------------------
                ChatResponsibleDto dto = new ChatResponsibleDto(UUID.randomUUID());
                dto.content = new ChatVoiceContent(path, Helper.getExtension(path));
                dto.contentType = ChatContentType.Voice;
                dto.receiverUserId = userId;
                //-----------------------
                ChatModel model = ChatMapper.convertDtoToModel(dto, ChatModel.SENDER_STATE_SENDING);
                if (Db.Chat.insert(model))
                {
                    listener.onReadyForSend(dto, model);
                }
                else
                {
                    listener.onErrorDb(activity.getString(R.string.messanger_chat_message_error_db_error));
                }
            }


        }
        catch (Exception ex)
        {
            listener.onException(ex);
        }

    }

    public interface EventListener
    {

        void onInvalidation(String message);

        void onErrorDb(String message);

        void onException(Exception ex);

        void onReadyForSend(ChatResponsibleDto dto, ChatModel model);

    }


}
