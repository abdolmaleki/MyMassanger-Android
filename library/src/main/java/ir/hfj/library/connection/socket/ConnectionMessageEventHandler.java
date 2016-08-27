package ir.hfj.library.connection.socket;

import android.os.Messenger;

import ir.hfj.library.connection.socket.dto.BaseDto;


public interface ConnectionMessageEventHandler
{

    public static final int MSG_Sending = 100;
    public static final int MSG_Successful = 200;
    public static final int MSG_Failed = 399;
    public static final int MSG_Exception = 499;

    //====================================================================================


    public abstract void onConnectionMessageChangeState(final Messenger sender, String method, final BaseDto dto, final int state);

    public abstract void onConnectionMessageCallback(final Messenger sender, String method, final BaseDto.Result resultDto);

}
