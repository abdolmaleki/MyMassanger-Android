package com.example.myapplication.fragment;

import com.example.myapplication.connection.socket.dto.ChatResponsibleDto;
import com.example.myapplication.connection.socket.dto.ChatTypingReportDto;

import java.util.UUID;

import ir.hfj.library.exception.MyMessangerException;


public interface IChatController
{
    void sendMessage(ChatResponsibleDto dto) throws MyMessangerException;

    void sendChatTypingReport(ChatTypingReportDto dto) throws MyMessangerException;

    void readChat(UUID chatUserGuid) throws MyMessangerException;

    //void setChatUserImage(String imageUrl) throws MyMessangerException;
}
