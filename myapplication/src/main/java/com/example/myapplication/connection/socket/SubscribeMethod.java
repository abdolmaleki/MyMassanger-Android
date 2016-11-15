package com.example.myapplication.connection.socket;


public interface SubscribeMethod
{

    //subscribe



    String ChatReceive = "ChatReceive";
    String ChatDeliver = "ChatDeliver";
    String ChatReadReport = "ChatReadReport";
    String ChatTypingReportReceive = "ChatTypingReportReceive";


    //call back
    String GetContact = "GetContact";
    String AddChat = "AddChat";
    String SetChatReadReport = "SetChatReadReport";
    String GetProfileImage="GetProfileImage";



    //no call back
    String ChatTypingReport = "ChatTypingReport";


}
