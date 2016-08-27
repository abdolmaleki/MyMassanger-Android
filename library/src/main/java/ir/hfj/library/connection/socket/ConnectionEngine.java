package ir.hfj.library.connection.socket;

import android.content.Context;
import android.os.Messenger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ir.hfj.library.R;
import ir.hfj.library.application.AppConfig;
import ir.hfj.library.connection.socket.dto.BaseDto;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.MessageReceivedHandler;
import microsoft.aspnet.signalr.client.NullLogger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubException;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.transport.WebsocketTransport;

public final class ConnectionEngine
{

    private final Context mContext;
    private HubConnection mConnection;
    private HubProxy mHub;
    private int mConnectionState = ConnectionEventHandler.NET_Disconnected;
    private ConnectionEventHandler mEventHandler = null;

    public ConnectionEngine(Context context, String url, String hubName, Object[] subscribes)
    {
        mContext = context;

        Platform.loadPlatformComponent(new AndroidPlatformComponent());
        mConnection = new HubConnection(url, "", true, new NullLogger());
        mConnection.setGson(AppConfig.getGsonSetting());
        mHub = mConnection.createHubProxy(hubName);


        mConnection.disconnected(new Runnable()
        {
            @Override
            public void run()
            {
                setConnectionState(ConnectionEventHandler.NET_Disconnected);
            }
        });

        mConnection.connected(new Runnable()
        {
            @Override
            public void run()
            {
                setConnectionState(ConnectionEventHandler.NET_Connected);
            }
        });

        mConnection.error(new ErrorCallback()
        {
            @Override
            public void onError(Throwable error)
            {

                /*if (mEventHandler != null)
                {
                    mEventHandler.onConnectionError(error);
                }
                mConnection.disconnect();*/

                if (mEventHandler != null)
                {
                    mEventHandler.onConnectionError(error);
                }

                if (!(error instanceof HubException))
                {
                    mConnection.disconnect();
                }
                else
                {
                    String message = error.getMessage();
                    if (message != null && message.startsWith("#FA#EX#"))
                    {
                        mConnection.disconnect();
                    }
                }

            }
        });

        mConnection.received(new MessageReceivedHandler()
        {
            @Override
            public void onMessageReceived(final JsonElement json)
            {
                try
                {
                    JsonArray jsonArray = json.getAsJsonObject().getAsJsonArray("A");

                    if (jsonArray != null)
                    {
                        int size = jsonArray.size();
                        if (size > 1 && jsonArray.get(size - 1).isJsonObject())
                        {
                            JsonObject baseObj = jsonArray.get(size - 1).getAsJsonObject();
                            boolean isNeedDelivering = baseObj.get("nd").getAsBoolean();

                            if (isNeedDelivering)
                            {
                                mHub.invoke("DM", baseObj.get("Guid").getAsString());
                            }

                        }
                    }
                }
                catch (Exception ex)
                {
                    if (mEventHandler != null)
                    {
                        mEventHandler.onConnectionError(ex);
                    }
                }

                /*if (mEventHandler != null)
                {
                    mEventHandler.onMessageReceived(json);
                }*/
            }
        });



		/*
         * mConnection.stateChanged(new StateChangedCallback()
		 * {
		 * 
		 * @Override
		 * public void stateChanged(ConnectionState arg0, ConnectionState arg1)
		 * {
		 * 
		 * }
		 * });
		 */

        for (Object o : subscribes)
        {
            if (o != null)
            {
                mHub.subscribe(o);
            }
        }


    }

    // =========================================================================================================
    // Property method
    // =========================================================================================================

    public int getConnectionState()
    {
        return mConnectionState;
    }

    private void setConnectionState(int state)
    {
        this.mConnectionState = state;
        if (mEventHandler != null)
        {
            mEventHandler.onConnectionStateChanged(state);
        }
    }

    public void setOnEventListener(ConnectionEventHandler listener)
    {
        this.mEventHandler = listener;
    }

    public String getConnectionToken()
    {
        return (this.mConnectionState == ConnectionEventHandler.NET_Connected) ? mConnection.getConnectionToken() : "";
    }

    public String getConnectionID()
    {
        return (this.mConnectionState == ConnectionEventHandler.NET_Connected) ? mConnection.getConnectionId() : "";
    }

    // =========================================================================================================
    // Command method
    // =========================================================================================================

    public final void connect()
    {

        if (mConnectionState != ConnectionEventHandler.NET_Connected)
        {
            setConnectionState(ConnectionEventHandler.NET_Connecting);
        }

        WebsocketTransport websocketTransport = new WebsocketTransport(new NullLogger(), AppConfig.NETWORK_SR_TIME_OUT);

        /*new Logger()
        {

            @Override
            public void log(String s, LogLevel logLevel)
            {
                if (AppConfig.DEBUG)
                {
                    Log.v(AppConfig.LOG_TAG, "WS > loger > " + s);
                }
            }
        });*/

        mConnection.start(websocketTransport);
    }

    public final boolean isConnect()
    {
        return (mConnectionState == ConnectionEventHandler.NET_Connected);
    }

    public final void disconnect()
    {
        mConnection.disconnect();
    }

    public void invoke(final BaseDto dto, final Class<? extends BaseDto.Result> resultType, final String method, final Messenger sender)
    {


        //if (sender == null)
        //{
        //    return;
        //}

        try
        {

            if (mEventHandler != null)
            {
                mEventHandler.onConnectionMessageChangeState(sender, method, dto, ConnectionEventHandler.MSG_Sending);
            }


            if (resultType == null)
            {

                final SignalRFuture<Void> messageHandler = mHub.invoke(method, dto);//sending


                messageHandler.done(new Action<Void>()
                {
                    @Override
                    public void run(Void arg0) throws Exception
                    {
                        if (mEventHandler != null)
                        {
                            mEventHandler.onConnectionMessageChangeState(sender, method, dto, ConnectionEventHandler.MSG_Successful);
                        }
                    }
                }).onError(new ErrorCallback()
                {
                    @Override
                    public void onError(Throwable arg0)
                    {
                        if (mEventHandler != null)
                        {
                            mEventHandler.onConnectionMessageChangeState(sender, method, dto, ConnectionEventHandler.MSG_Failed);
                        }
                    }
                }).onCancelled(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        messageHandler.triggerError(new Exception("Connection closed"));
                    }
                });

                if (!isConnect())
                {
                    messageHandler.cancel();
                }

            }
            else
            {

                final SignalRFuture<BaseDto.Result> messageHandler = mHub.invoke(BaseDto.Result.class, resultType, method, dto);//sending


                messageHandler.done(new Action<BaseDto.Result>()
                {
                    @Override
                    public void run(BaseDto.Result resultDto) throws Exception
                    {
                        if (mEventHandler != null)
                        {
                            mEventHandler.onConnectionMessageChangeState(sender, method, dto, ConnectionEventHandler.MSG_Successful);
                            resultDto.autoIdResponse = dto.autoId;
                            resultDto.arg1Response = dto.arg1;
                            //resultDto.arg2Response = dto.arg2;
                            resultDto.request = dto;
                            mEventHandler.onConnectionMessageCallback(sender, method, resultDto);
                        }
                    }
                }).onError(new ErrorCallback()
                {
                    @Override
                    public void onError(Throwable e)
                    {

                        if (mEventHandler != null)
                        {
                            mEventHandler.onConnectionMessageChangeState(sender, method, dto, ConnectionEventHandler.MSG_Failed);

                            BaseDto.Result resultDTO = null;
                            try
                            {
                                resultDTO = resultType.newInstance();

                                resultDTO.isSuccessful = false;
                                resultDTO.isException = true;
                                resultDTO.autoIdResponse = dto.autoId;
                                resultDTO.arg1Response = dto.arg1;
                                //resultDTO.arg2Response = dto.arg2;
                                resultDTO.request = dto;
                                String message = e.getMessage();

                                if (e instanceof HubException && message != null && message.startsWith("#FA#"))
                                {
                                    resultDTO.baseMessage = message.substring(7);
                                }
                                else
                                {
                                    resultDTO.baseMessage = mContext.getString(R.string.samim_message_error_network);
                                }
                            }
                            catch (Exception ignored)
                            {

                            }

                            mEventHandler.onConnectionMessageCallback(sender, method, resultDTO);
                        }

                    }
                }).onCancelled(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        messageHandler.triggerError(new Exception("Connection closed"));
                    }
                });

                //for: if not connect, and send message, not raise onErrorMessage, only raise global onError.
                //with this code send error feedback to sender client with triggerError then raise onError message

                if (!isConnect())
                {
                    messageHandler.cancel();
                }


            }


        }
        catch (Exception ex)
        {
            if (mEventHandler != null)
            {
                mEventHandler.onConnectionMessageChangeState(sender, method, dto, ConnectionEventHandler.MSG_Exception);
            }
        }
    }


    public void invokeDirect(final BaseDto dto, final Class<? extends BaseDto.Result> resultType, final String method, final ConnectionMessageEventHandler listener)
    {

        try
        {

            if (listener != null)
            {
                listener.onConnectionMessageChangeState(null, method, dto, ConnectionEventHandler.MSG_Sending);
            }


            if (resultType == null)
            {

                final SignalRFuture<Void> messageHandler = mHub.invoke(method, dto);//sending


                messageHandler.done(new Action<Void>()
                {
                    @Override
                    public void run(Void arg0) throws Exception
                    {
                        if (listener != null)
                        {
                            listener.onConnectionMessageChangeState(null, method, dto, ConnectionEventHandler.MSG_Successful);
                        }
                    }
                }).onError(new ErrorCallback()
                {
                    @Override
                    public void onError(Throwable arg0)
                    {
                        if (listener != null)
                        {
                            listener.onConnectionMessageChangeState(null, method, dto, ConnectionEventHandler.MSG_Failed);
                        }
                    }
                }).onCancelled(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        messageHandler.triggerError(new Exception("Connection closed"));
                    }
                });

                if (!isConnect())
                {
                    messageHandler.cancel();
                }

            }
            else
            {

                final SignalRFuture<BaseDto.Result> messageHandler = mHub.invoke(BaseDto.Result.class, resultType, method, dto);//sending


                messageHandler.done(new Action<BaseDto.Result>()
                {
                    @Override
                    public void run(BaseDto.Result resultDto) throws Exception
                    {
                        if (listener != null)
                        {
                            resultDto.autoIdResponse = dto.autoId;
                            resultDto.arg1Response = dto.arg1;
                            //resultDto.arg2Response = dto.arg2;
                            resultDto.request = dto;

                            listener.onConnectionMessageChangeState(null, method, dto, ConnectionEventHandler.MSG_Successful);
                            listener.onConnectionMessageCallback(null, method, resultDto);
                        }
                    }
                }).onError(new ErrorCallback()
                {
                    @Override
                    public void onError(Throwable e)
                    {

                        if (listener != null)
                        {
                            listener.onConnectionMessageChangeState(null, method, dto, ConnectionEventHandler.MSG_Failed);

                            BaseDto.Result resultDto = null;
                            try
                            {
                                resultDto = resultType.newInstance();

                                resultDto.isSuccessful = false;
                                resultDto.isException = true;
                                resultDto.autoIdResponse = dto.autoId;
                                resultDto.arg1Response = dto.arg1;
                                //resultDto.arg2Response = dto.arg2;
                                resultDto.request = dto;

                                String message = e.getMessage();

                                if (e instanceof HubException && message != null && message.startsWith("#FA#"))
                                {
                                    resultDto.baseMessage = message.substring(7);
                                }
                                else
                                {
                                    resultDto.baseMessage = mContext.getString(R.string.samim_message_error_network);
                                }
                            }
                            catch (Exception ignored)
                            {

                            }

                            listener.onConnectionMessageCallback(null, method, resultDto);
                        }

                    }
                }).onCancelled(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        messageHandler.triggerError(new Exception("Connection closed"));
                    }
                });

                //for: if not connect, and send message, not raise onErrorMessage, only raise global onError.
                //with this code send error feedback to sender client with triggerError then raise onError message

                if (!isConnect())
                {
                    messageHandler.cancel();
                }


            }


        }
        catch (Exception ex)
        {
            if (listener != null)
            {
                listener.onConnectionMessageChangeState(null, method, dto, ConnectionEventHandler.MSG_Exception);
            }
        }
    }

}
