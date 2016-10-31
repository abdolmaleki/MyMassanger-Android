package ir.hfj.library.handler;


import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.connection.socket.ConnectionEventHandler;
import ir.hfj.library.connection.socket.dto.BaseDto;
import ir.hfj.library.connection.socket.dto.HelloDto;
import ir.hfj.library.service.NetworkService;
import ir.hfj.library.util.Helper;


public abstract class NetworkServiceClientHandler<T extends Activity> extends Handler
{

    private Messenger mService = null;
    private boolean mIsBound = false;
    private Messenger mMessenger;
    private int mMessageFilter = -1;
    private String mConnectionToken = "";
    private boolean mIsReady = false;
    private final WeakReference<T> mActivity;

    private String mConnectionId = "";
    private int mConnectionState = -1;
    private boolean mIsAuthenticated = false;
    private String mAuthenticatedMessage;
    private int mAuthenticateIssue = NetworkService.AUT_LOADING;

    public NetworkServiceClientHandler(T activity, int messageFilter)
    {
        if (activity == null)
        {
            throw new RuntimeException("###### context not instanceof Activity");
        }

        if (!Helper.isFlag(messageFilter))
        {
            throw new RuntimeException("###### Value of " + messageFilter + " not a flag!");
        }

        mActivity = new WeakReference<>(activity);
        mMessageFilter = messageFilter;
        mMessenger = new Messenger(this);
    }

    private ServiceConnection mConnection = new ServiceConnection()
    {

        public void onServiceConnected(ComponentName className, IBinder service)
        {
            mService = new Messenger(service);
            //Attached
            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkServiceClientHandler > Attached");
            }

            try
            {
                Message msg = Message.obtain(null, NetworkService.MSG_SERVICE_SET_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                msg.arg1 = mMessageFilter;
                mService.send(msg);

            }
            catch (RemoteException e)
            {
                // In this case the service has crashed before we could even
            }

            ///connected
            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkServiceClientHandler > Connected");
            }

            NetworkServiceClientHandler.this.onServiceConnected();

        }

        public void onServiceDisconnected(ComponentName className)
        {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            //Disconnected
            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkServiceClientHandler > Disconnected");
            }


            NetworkServiceClientHandler.this.onServiceDisconnected();


        }
    };

    public final boolean doBindService()
    {

        startService();

        mIsBound = bindService(mConnection);

        if (AppConfig.DEBUG)
        {
            Log.v(AppConfig.LOG_TAG, "NetworkServiceClientHandler > Binding");
        }

        return mIsBound;
        //Binding
    }

    public final void editFilterMessage(int messageFilter)
    {
        try
        {
            Message msg = Message.obtain(null, NetworkService.MSG_SERVICE_SET_EDIT_CLIENT);
            msg.replyTo = mMessenger;
            msg.arg1 = mMessageFilter;
            mService.send(msg);
            mMessageFilter = messageFilter;
        }
        catch (RemoteException e)
        {
            // In this case the service has crashed before we could even
        }
    }

    public final void doUnbindService()
    {
        if (mIsBound)
        {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null)
            {
                try
                {

                    Message msg = Message.obtain(null, NetworkService.MSG_SERVICE_SET_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e)
                {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            getActivityCast().unbindService(mConnection);
            mIsBound = false;
            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkServiceClientHandler > Unbinding");
            }
        }
    }

    public final boolean isAuthenticated()
    {
        return mIsAuthenticated;
    }

    public final boolean isServerConnected()
    {
        return mConnectionState == ConnectionEventHandler.NET_Connected;
    }

    public final int getAuthenticateIssue()
    {
        return mAuthenticateIssue;
    }

    public final String getAuthenticateMessage()
    {
        return mAuthenticatedMessage;
    }


    public final boolean isReady()
    {
        return mIsReady;
    }
    public final boolean isBounded()
    {
        return mIsBound;
    }

    public final String getConnectionToken()
    {
        return mConnectionToken;
    }

    public final String getConnectionId()
    {
        return mConnectionId;
    }

    public final int getConnectionState()
    {
        return mConnectionState;
    }


    protected final Activity getActivityCast()
    {
        Activity activity = (Activity) mActivity.get();
        if (activity != null)
        {
            return activity;
        }

        throw new RuntimeException("###### Activity is null");
    }

    protected final T getActivity()
    {
        T activity = mActivity.get();
        if (activity != null)
        {
            return activity;
        }

        throw new RuntimeException("###### Activity is null");
    }

    // =========================================================================================================
    // Send message method for service
    // =========================================================================================================


    public final void send(Message msg) throws RemoteException
    {
        msg.replyTo = mMessenger;
        if (mService != null)
        {
            mService.send(msg);
        }
        else
        {
            throw new RemoteException();
        }
    }


    // =========================================================================================================
    // Send DTO method to server
    // =========================================================================================================

    protected final long sendCallback(BaseDto basedto, String method) throws RemoteException
    {

        String resultClassName = basedto.getClass().getName() + "$" + BaseDto.Result.class.getSimpleName();

        try
        {
            Class dtoResult = Class.forName(resultClassName);

            if (BaseDto.Result.class.isAssignableFrom(dtoResult))
            {

                Message msg = Message.obtain(null, NetworkService.MSG_SERVICE_SEND_MESSAGE_SERVER);
                Bundle bundle = new Bundle();
                bundle.putSerializable(NetworkService.MSG_KEY_DTO, basedto);
                bundle.putSerializable(NetworkService.MSG_KEY_DTO_RESULT_TYPE, dtoResult);
                bundle.putString(NetworkService.MSG_KEY_DTO_METHOD, method);
                msg.setData(bundle);

                send(msg);

                return basedto.autoId;
            }
            else
            {
                throw new RuntimeException("#######  Class [" + basedto.getClass().getName() + "] must inherit of " + BaseDto.Result.class.getName());
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("#######  Class not found : " + resultClassName);
        }


    }

    protected final long sendNoCallback(BaseDto basedto, String method) throws RemoteException
    {
        return sendCallback(basedto, null, method);
    }

    protected final long sendCallback(BaseDto basedto, Class<? extends BaseDto.Result> resultType, String method) throws RemoteException
    {

        Message msg = Message.obtain(null, NetworkService.MSG_SERVICE_SEND_MESSAGE_SERVER);
        Bundle bundle = new Bundle();
        bundle.putSerializable(NetworkService.MSG_KEY_DTO, basedto);
        bundle.putSerializable(NetworkService.MSG_KEY_DTO_RESULT_TYPE, resultType);
        bundle.putString(NetworkService.MSG_KEY_DTO_METHOD, method);
        msg.setData(bundle);

        send(msg);

        return basedto.autoId;

    }
    // =========================================================================================================
    // helper methods
    // =========================================================================================================

    @Override
    public final void handleMessage(Message msg)
    {
        if (AppConfig.DEBUG)
        {
            Log.v(AppConfig.LOG_TAG, "NetworkServiceClientHandler > handleMessage > msg:" + msg.what);
        }

        switch (msg.what)
        {
            case NetworkService.MSG_CLIENT_ALL_SERVER_MESSAGE:
            {
                Bundle bundle = msg.getData();

                bundle.setClassLoader(BaseDto.class.getClassLoader());
                BaseDto dto = (BaseDto) bundle.getSerializable(NetworkService.MSG_KEY_DTO);
                String subscribe = bundle.getString(NetworkService.MSG_KEY_DTO_METHOD, "");

                if (subscribe.equalsIgnoreCase("hello"))
                {
                    onHelloReceived((HelloDto) dto);
                }
                else
                {
                    driveHandleReceivedDTO(subscribe, dto);
                }

                break;
            }
            case NetworkService.MSG_CONNECTION_STATE:
            {
                Bundle bundle = msg.getData();
                this.mConnectionToken = bundle.getString(NetworkService.MSG_KEY_CONNECTION_TOKEN, "");
                this.mConnectionId = bundle.getString(NetworkService.MSG_KEY_CONNECTION_ID, "");
                this.mConnectionState = bundle.getInt(NetworkService.MSG_KEY_CONNECTION_STATE);
                onChangeConnectionState(this.mConnectionState);
                break;
            }
            case NetworkService.MSG_CLIENT_SEND_MESSAGE_SERVER_CALLBACK:
            {
                Bundle bundle = msg.getData();
                BaseDto.Result dto = (BaseDto.Result) bundle.getSerializable(NetworkService.MSG_KEY_DTO_CALLBACK);
                String method = bundle.getString(NetworkService.MSG_KEY_DTO_CALLBACK_METHOD, "");

                if (dto == null)
                {
                    onServiceError("Callback dto is null");
                }
                else
                {
                    if (method.equalsIgnoreCase("hello"))
                    {
                        onHelloCallback((HelloDto.Result) dto);
                    }
                    else
                    {
                        driveHandleCallbacksDTO(method, dto);
                    }
                }
                break;
            }
            case NetworkService.MSG_CLIENT_SEND_MESSAGE_SERVER_STATE:
            {
                Bundle bundle = msg.getData();
                BaseDto dto = (BaseDto) bundle.getSerializable(NetworkService.MSG_KEY_DTO_STATE_DTO);
                int value = bundle.getInt(NetworkService.MSG_KEY_DTO_STATE_VALUE);

                onMessageDtoChangeState(dto, value);

                break;
            }
            case NetworkService.MSG_CLIENT_EXCEPTION_ERROR:
            {
                onServiceError(msg.getData().getString(NetworkService.MSG_KEY_ERROR_MESSAGE));
                break;
            }
            case NetworkService.MSG_AUTHENTICATION_STATE:
            {

                Bundle bundle = msg.getData();

                mIsAuthenticated = bundle.getBoolean(NetworkService.MSG_KEY_AUTH_STATE);
                mAuthenticateIssue = bundle.getInt(NetworkService.MSG_KEY_AUTH_ISSUE);
                mAuthenticatedMessage = bundle.getString(NetworkService.MSG_KEY_AUTH_MESSAGE, "");

                onChangeAuthenticationState(mIsAuthenticated, mAuthenticatedMessage, mAuthenticateIssue);
                break;
            }
            case NetworkService.MSG_CLIENT_READY:
            {
                mIsReady = true;
                onReady();
                break;
            }
            default:
            {
                driveHandleMessage(msg);
            }
        }
    }

    public long invokeHello(HelloDto dto) throws RemoteException
    {
        return sendCallback(dto, "hello");
    }

    public void tryConnectToServer() throws RemoteException
    {
        Message msg = Message.obtain(null, NetworkService.MSG_SERVICE_TRYCONNECT);
        send(msg);
    }

    // =========================================================================================================
    // abstract methods
    // =========================================================================================================

    protected abstract void startService();

    protected abstract boolean bindService(ServiceConnection connection);

    protected abstract void onServiceConnected();

    protected abstract void onServiceDisconnected();

    protected abstract void onReady();

    protected void onServiceError(String message)
    {
        if (AppConfig.DEBUG)
        {
            Log.e(AppConfig.LOG_TAG, "onServiceError : " + message);
        }
    }

    protected void driveHandleMessage(Message msg)
    {
    }

    protected abstract void driveHandleReceivedDTO(String subscribe, BaseDto dto);

    protected abstract void driveHandleCallbacksDTO(String method, BaseDto.Result dto);

    protected abstract void onChangeAuthenticationState(boolean isAuthenticated, String message, int authenticateIssue);

    protected abstract void onChangeConnectionState(int state);

    protected abstract void onMessageDtoChangeState(BaseDto dto, int state);

    protected void onHelloReceived(HelloDto dto)
    {
        if (AppConfig.DEBUG)
        {
            Log.i(AppConfig.LOG_TAG, "onHelloReceived : " + dto.message);
        }
    }
    protected void onHelloCallback(HelloDto.Result dto)
    {
        if (AppConfig.DEBUG)
        {
            Log.i(AppConfig.LOG_TAG, "onHelloCallback : " + dto.message);
        }
    }

}
