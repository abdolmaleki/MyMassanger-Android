package ir.hfj.library.service;


import android.content.Intent;

import ir.hfj.library.connection.socket.dto.BaseDto;
import ir.hfj.library.connection.socket.dto.UpdateAppDto;

public interface INetworkService
{

    void setServiceNotification(Intent intent, int flags, int startId);
    Object getSubscribe();
    void refreshNotification();
    boolean onMessageDtoCallback(String method, BaseDto.Result result);
    boolean onMessageDtoChangeState(BaseDto dto, int state);
    void onUpdateAppNotification(UpdateAppDto dto);
}
