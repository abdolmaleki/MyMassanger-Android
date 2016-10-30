package com.example.myapplication.fragment;

import com.example.myapplication.holder.ChatHolder;

import java.util.UUID;

public interface IChatFragment
{
    UUID getContactGuid();

    void addMessage(ChatHolder holder);

    void replaceMessage(UUID chatId, ChatHolder holder);

    void updateMessage(ChatHolder holder);

    void setContactTypingState(boolean isTyping, UUID contactGuid);

}
