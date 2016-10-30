package com.example.myapplication.fragment;

import com.example.myapplication.connection.socket.dto.ChatResponsibleDto;
import com.example.myapplication.connection.socket.dto.ChatTypingReportDto;

import java.util.UUID;

import ir.hfj.library.exception.SamimException;


public interface IChatController
{
    void sendMessage(ChatResponsibleDto dto) throws SamimException;

    void sendChatTypingReport(ChatTypingReportDto dto) throws SamimException;

    void readChat(UUID chatUserGuid) throws SamimException;

    //void setChatUserImage(String imageUrl) throws SamimException;
}
