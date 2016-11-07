package com.example.myapplication.database.model;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.example.myapplication.entity.ChatBaseContent;
import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.entity.ChatImageContent;
import com.example.myapplication.entity.ChatTextContent;

import java.util.Date;
import java.util.UUID;

import ir.hfj.library.database.model.BaseModel;


@Table(name = ChatModel.__table, id = ChatModel.__id)
public class ChatModel extends BaseModel
{

    public static final int SENDER_STATE = 0;

    public static final int SENDER_STATE_SENDING = SENDER_STATE + 0;
    public static final int SENDER_STATE_SENDED = SENDER_STATE + 1;
    public static final int SENDER_STATE_DELIVER = SENDER_STATE + 2;
    public static final int SENDER_STATE_SEEN = SENDER_STATE + 3;

    public static final int RECEIVER_STATE = 100;

    public static final int RECEIVER_STATE_RECEIVED = RECEIVER_STATE + 0;
    public static final int RECEIVER_STATE_READ = RECEIVER_STATE + 1;
    public static final int RECEIVER_STATE_REPORT_READ = RECEIVER_STATE + 2;

    public static final String __table = "_chat";

    public static final String _content = "_content";
    @Column(name = _content, notNull = true)
    public ChatBaseContent content;

    public static final String _contentType = "_contentType";
    @Column(name = _contentType, notNull = true)
    public ChatContentType contentType;

    public static final String _date = "_date";
    @Column(name = _date, notNull = true)
    public Date date;

    public static final String _contactUserId = "_contactUserId";
    @Column(name = _contactUserId)
    public UUID contactUserId;

    public static final String _contactPhoneNumber = "_contactPhoneNumber";
    @Column(name = _contactPhoneNumber)
    public String contactPhoneNumber;

    public static final String _deliverDate = "_deliverDate";
    @Column(name = _deliverDate)
    public Date deliverDate;

    public static final String _readDate = "_readDate";
    @Column(name = _readDate)
    public Date readDate;

    public static final String _path = "_path";
    @Column(name = _path)
    public String path;

    public static final String _needUpload = "_needUpload";
    @Column(name = _needUpload)
    public boolean needUpload;

    public static final String _state = "_state";
    @Column(name = _state)
    public int state;

    public boolean isMyMessage()
    {
        return RECEIVER_STATE < 100;
    }

    public ChatModel()
    {
        super();
    }

    public ChatModel(UUID guid)
    {
        super(guid);
    }

    public String getSummary()
    {
        if (content instanceof ChatTextContent)
        {
            return ((ChatTextContent) content).text;
        }
        else if (content instanceof ChatImageContent)
        {
            return ((ChatImageContent) content).comment;
        }
        else
        {
            return content.c;
        }
    }

    @Override
    public String toString()
    {
        return (content != null) ? content.c : super.toString();
    }


}
