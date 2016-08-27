package ir.hfj.library.connection.socket;

public interface ConnectionEventHandler extends ConnectionMessageEventHandler
{


    public static final int NET_Connecting = 100;
    public static final int NET_Connected = 200;
    public static final int NET_Disconnected = 999;


    //====================================================================================


    public abstract void onConnectionError(final Throwable error);


    public abstract void onConnectionStateChanged(final int state);


}
