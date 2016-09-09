package com.example.myapplication.handler;

import android.app.Activity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;

import com.example.myapplication.connection.socket.SubscribeMethod;
import com.example.myapplication.connection.socket.dto.ChatDeliverDto;
import com.example.myapplication.connection.socket.dto.ChatDto;
import com.example.myapplication.connection.socket.dto.ChatReadReportDto;
import com.example.myapplication.connection.socket.dto.ChatReadReportResponsibleDto;
import com.example.myapplication.connection.socket.dto.ChatResponsibleDto;
import com.example.myapplication.connection.socket.dto.ChatTypingReportDto;
import com.example.myapplication.connection.socket.dto.TeacherResponsibleDto;
import com.example.myapplication.holder.DownloadHolder;
import com.example.myapplication.holder.UploadHolder;
import com.example.myapplication.service.SamimService;

import java.util.UUID;

import ir.hfj.library.connection.socket.dto.BaseDto;
import ir.hfj.library.handler.NetworkServiceClientHandler;


public abstract class SamimClientHandler<T extends Activity> extends NetworkServiceClientHandler<T>
{

    public SamimClientHandler(T activity)
    {
        super(activity, -1);
    }

    public SamimClientHandler(T activity, int messageFilter)
    {
        super(activity, messageFilter);
    }

    @Override
    protected final void startService()
    {
        SamimService.start(getActivityCast());
    }

    @Override
    protected final boolean bindService(ServiceConnection connection)
    {
        Activity activity = getActivityCast();
        return activity.bindService(new Intent(activity, SamimService.class), connection, 0);
    }

    @Override
    protected final void onServiceError(String message)
    {
    }

    @Override
    protected final void driveHandleMessage(Message msg)
    {
        switch (msg.what)
        {
            case SamimService.MSG_CLIENT_DOWNLOAD_STATE:
            {
                Bundle bundle = msg.getData();
                DownloadHolder.Received downloadHolder = (DownloadHolder.Received) bundle.getSerializable(SamimService.MSG_KEY_DOWNLOAD);
                if (downloadHolder != null)
                {
                    onDownloadChangeState(downloadHolder);
                }
                break;
            }
            case SamimService.MSG_CLIENT_UPLOAD_STATE:
            {
                Bundle bundle = msg.getData();
                UploadHolder.Received uploadHolder = (UploadHolder.Received) bundle.getSerializable(SamimService.MSG_KEY_UPLOAD);
                if (uploadHolder != null)
                {
                    onUploadChangeState(uploadHolder);
                }
                break;
            }
        }
    }


    @Override
    protected final void driveHandleReceivedDTO(String subscribe, BaseDto dto)
    {
         if (subscribe.equalsIgnoreCase(SubscribeMethod.ChatReceive))
        {
            onChatReceived((ChatDto) dto);
        }
        else if (subscribe.equalsIgnoreCase(SubscribeMethod.ChatDeliver))
        {
            onChatDeliverReceived((ChatDeliverDto) dto);
        }
        else if (subscribe.equalsIgnoreCase(SubscribeMethod.ChatReadReport))
        {
            onChatReadReportReceived((ChatReadReportDto) dto);
        }
        else if (subscribe.equalsIgnoreCase(SubscribeMethod.ChatTypingReportReceive))
        {
            onChatTypingReportReceive((ChatTypingReportDto) dto);
        }


    }

    protected void onChatReceived(ChatDto dto)
    {
    }
    protected void onChatDeliverReceived(ChatDeliverDto dto)
    {
    }

    protected void onChatReadReportReceived(ChatReadReportDto dto)
    {
    }
    protected void onChatTypingReportReceive(ChatTypingReportDto dto)
    {
    }



    @Override
    protected final void driveHandleCallbacksDTO(String method, BaseDto.Result dto)
    {

        if (method.equalsIgnoreCase(SubscribeMethod.AddChat))
        {
            onChatCallback((ChatResponsibleDto.Result) dto);
        }
        else if (method.equalsIgnoreCase(SubscribeMethod.SetChatReadReport))
        {
            onChatReadReportCallback((ChatReadReportResponsibleDto.Result) dto);
        }
        else if (method.equalsIgnoreCase(SubscribeMethod.GetTeacher))
        {
            onTeacherCallBack((TeacherResponsibleDto.Result) dto);
        }


        //if (method.equalsIgnoreCase("hello"))
        //{
        //    onHelloCallback((HelloDto.Result) dto);
        //}
        //else
        //{
        //    driveHandleCallbacksDTO(method, dto);
        //}
    }

    protected void onTeacherCallBack(TeacherResponsibleDto.Result dto)
    {
    }
    protected void onChatCallback(ChatResponsibleDto.Result dto)
    {
    }
    protected void onChatReadReportCallback(ChatReadReportResponsibleDto.Result dto)
    {
    }

    //---------------------

    protected void onUploadProgress(UUID guid, int progress)
    {

    }

    protected void onUploadComplete(UUID guid, String token)
    {

    }

    protected void onUploadError(UUID guid, String message)
    {

    }

    protected void onDownloadChangeState(DownloadHolder.Received downloadHolder)
    {

    }
    protected void onUploadChangeState(UploadHolder.Received uploadHolder)
    {

    }


    //---------------------

    public long invokeChat(ChatResponsibleDto dto) throws RemoteException
    {
        return sendCallback(dto, SubscribeMethod.AddChat);
    }

    public long invokeChatReportRead(ChatReadReportResponsibleDto dto) throws RemoteException
    {
        return sendCallback(dto, SubscribeMethod.SetChatReadReport);
    }

    public long invokeTeacher(TeacherResponsibleDto dto) throws RemoteException
    {
        return sendCallback(dto, SubscribeMethod.GetTeacher);
    }

    public long invokeChatTypingReport(ChatTypingReportDto dto) throws RemoteException
    {
        return sendNoCallback(dto, SubscribeMethod.ChatTypingReport);
    }

    public void sendDownload(DownloadHolder.Send sendHolder) throws RemoteException
    {
        Message msg = Message.obtain(null, SamimService.MSG_SERVICE_DOWNLOAD);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SamimService.MSG_KEY_DOWNLOAD, sendHolder);
        msg.setData(bundle);

        send(msg);
    }

    public void cancelDownload(UUID guid) throws RemoteException
    {
        Message msg = Message.obtain(null, SamimService.MSG_SERVICE_DOWNLOAD_CANCEL);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SamimService.MSG_KEY_DOWNLOAD_GUID, guid);
        msg.setData(bundle);

        send(msg);
    }

    public void cancelUpload(UUID guid) throws RemoteException
    {
        Message msg = Message.obtain(null, SamimService.MSG_SERVICE_UPLOAD_CANCEL);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SamimService.MSG_KEY_UPLOAD_GUID, guid);
        msg.setData(bundle);

        send(msg);
    }

    public enum Action
    {
        Add, Edit, Delete
    }

}
