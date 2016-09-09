package com.example.myapplication.fragment;

import com.example.myapplication.holder.ChatHistoryHolder;

import java.util.UUID;

public interface IChatHistoryFragment
{

    void updateChatHistory(UUID contactGuid);
    void setCurrentContact(UUID contactGuid);
    ChatHistoryHolder getChatHistoryByGuid(UUID contactGuid);
}
