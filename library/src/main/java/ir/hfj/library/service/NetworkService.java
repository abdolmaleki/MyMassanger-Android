package ir.hfj.library.service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ir.hfj.library.R;
import ir.hfj.library.application.AppConfig;
import ir.hfj.library.connection.restapi.RestApi;
import ir.hfj.library.connection.restapi.jto.AuthenticationJto;
import ir.hfj.library.connection.restapi.jto.PostBackJto;
import ir.hfj.library.connection.socket.ConnectionEngine;
import ir.hfj.library.connection.socket.ConnectionEventHandler;
import ir.hfj.library.connection.socket.dto.BaseDto;
import ir.hfj.library.connection.socket.dto.HelloDto;
import ir.hfj.library.connection.socket.dto.UpdateAppDto;
import ir.hfj.library.database.DbBase;
import ir.hfj.library.database.model.UserSettingModel;
import ir.hfj.library.exception.SamimException;
import ir.hfj.library.receiver.SamimAction;
import ir.hfj.library.util.Helper;


public abstract class NetworkService extends Service implements INetworkService, ConnectionEventHandler, INotificationManageable
{


    //===========================================================================
    public static final int FLAG_HELLO = 1;
    //===========================================================================
    public static final int MSG_CLIENT_EXCEPTION_ERROR = 60;
    public static final String MSG_KEY_ERROR_MESSAGE = "error.message";
    //===========================================================================


    //===========================================================================
    public static final int MSG_AUTHENTICATION_STATE = 70;
    public static final String MSG_KEY_AUTH_MESSAGE = "auth.message";
    public static final String MSG_KEY_AUTH_STATE = "auth.state";
    public static final String MSG_KEY_AUTH_ISSUE = "auth.issue";
    //===========================================================================
    public static final int AUT_LOADING = 1;
    public static final int AUT_NOT_ACTIVE = 2;
    public static final int AUT_EXPIRED = 3;
    public static final int AUT_OK = 4;
    public static final int AUT_ERROR = 5;
    public static final int AUT_VERSION_NOT_SUPPORT = 6;
    public static final int AUT_BLOCKED = 7;
    //===========================================================================
    //public static final int LOGOUT_SERVER_REQUEST = 1;
    //public static final int LOGOUT_EXCEPTION = 2;
    //public static final int LOGOUT_UNAUTHORIZED = 3;
    //public static final int LOGOUT_CLIENT_REQUEST = 4;
    public static final int LOGOUT_VERSION_NOT_SUPPORT = 5;
    //public static final int LOGOUT_USER_IS_DISABLE = 6;
    //===========================================================================

    public static final int MSG_CLIENT_ALL_SERVER_MESSAGE = 50;

    //===========================================================================
    public static final int MSG_SERVICE_SEND_MESSAGE_SERVER = 51;
    public static final String MSG_KEY_DTO = "dto.object";
    public static final String MSG_KEY_DTO_RESULT_TYPE = "dto.callback";
    public static final String MSG_KEY_DTO_METHOD = "dto.method";
    //===========================================================================

    //===========================================================================
    public static final int MSG_CLIENT_SEND_MESSAGE_SERVER_CALLBACK = 52;
    public static final String MSG_KEY_DTO_CALLBACK = "dto.callback";
    public static final String MSG_KEY_DTO_CALLBACK_METHOD = "dto.callback.method";
    //===========================================================================

    //===========================================================================
    public static final int MSG_CLIENT_SEND_MESSAGE_SERVER_STATE = 53;
    public static final String MSG_KEY_DTO_STATE_DTO = "dto.state.guid";
    public static final String MSG_KEY_DTO_STATE_VALUE = "dto.state.value";
    public static final String MSG_KEY_DTO_STATE_METHOD = "dto.state.method";
    //===========================================================================

    //===========================================================================
    public static final int MSG_CONNECTION_STATE = 40;
    public static final String MSG_KEY_CONNECTION_TOKEN = "connection.token";
    public static final String MSG_KEY_CONNECTION_STATE = "connection.state";
    public static final String MSG_KEY_CONNECTION_ID = "connection.id";
    //===========================================================================


    public static final int MSG_SERVICE_SET_REGISTER_CLIENT = 10;
    public static final int MSG_SERVICE_SET_UNREGISTER_CLIENT = 11;
    public static final int MSG_SERVICE_SET_EDIT_CLIENT = 12;
    public static final int MSG_CLIENT_READY = 13;
    public static final int MSG_SERVICE_TRYCONNECT = 14;

    //====================================================================================
    //====================================================================================

    protected static boolean mIsAlive = false;


    private boolean mIsAuthenticatedX = false;
    private String mAuthenticatedMessage;
    private int mAuthenticateIssue = AUT_LOADING;
    protected ConnectionEngine mConnectionEngine;
    private NotificationManager mNotificationManager;
    private UserSettingModel mUserSetting = null;
    private List<ClientMessenger> mClients = new ArrayList<>();


    // =========================================================================================================
    // Service Overrided method
    // =========================================================================================================


    @Override
    public void onCreate()
    {
        if (!mIsAlive)
        {

            mAuthenticatedMessage = getString(R.string.samim_service_message_logining);
            mAuthenticateIssue = AUT_LOADING;

            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            registerReceiver(mBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SamimAction.ACTIVATION_EXPIRED);
            intentFilter.addAction(SamimAction.ACTIVATION_SUCCESSFUL);
            registerReceiver(mActivationReceiver, intentFilter);

            mAsyncPing.start();

            mConnectionEngine = new ConnectionEngine(this, AppConfig.NETWORK_HOST_SR + "/signalr", AppConfig.NETWORK_Samim_HUB, getSubscribes());
            mConnectionEngine.setOnEventListener(this);
            mConnectionEngine.connect();

            if (AppConfig.DEBUG)
            {
                Log.i(AppConfig.LOG_TAG, "NetworkService > onCreate");
            }

        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        if (!mIsAlive)
        {
            mIsAlive = true;
            //if intent==1 stopForeground
            setServiceNotification(intent, flags, startId);
        }
        return 0;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return true;// allow rebind
    }

    @Override
    public void onRebind(Intent intent)
    {
    }

    @Override
    public void onDestroy()
    {

        mIsAlive = false;

        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mActivationReceiver);

        mAsyncPing.interrupt();

        if (AppConfig.DEBUG)
        {
            Log.i(AppConfig.LOG_TAG, "NetworkService > onDestroy");
        }

    }

    // =========================================================================================================
    // Static method
    // =========================================================================================================

    protected synchronized static void start(Context context, Class<?> cls)
    {
        if (!mIsAlive)//always is false; because another process
        {
            context = context.getApplicationContext();
            Intent serviceIntent = new Intent(context, cls);
            context.startService(serviceIntent);
        }
    }

    // =========================================================================================================
    // Client service message receiver
    // =========================================================================================================


    @SuppressLint("HandlerLeak")
    private class NetworkServiceHandler extends Handler
    {

        @Override
        public void handleMessage(Message msg)
        {

            try
            {

                if (AppConfig.DEBUG)
                {
                    Log.v(AppConfig.LOG_TAG, "NetworkService > handleMessage > msg: " + msg.what);
                }

                switch (msg.what)
                {
                    case MSG_SERVICE_SET_REGISTER_CLIENT:
                    {
                        if (AppConfig.DEBUG)
                        {
                            Log.v(AppConfig.LOG_TAG, "NetworkService > register client > " + msg.replyTo.hashCode());
                        }

                        ClientMessenger clientMessenger = new ClientMessenger();
                        clientMessenger.messenger = msg.replyTo;
                        clientMessenger.filterType = msg.arg1;
                        mClients.add(clientMessenger);


                        sendConnectionState(msg.replyTo);

                        sendAuthenticationState(msg.replyTo);

                        onRegisterClientToService(msg.replyTo);

                        msg.replyTo.send(Message.obtain(null, MSG_CLIENT_READY));

                        break;
                    }
                    case MSG_SERVICE_SET_UNREGISTER_CLIENT:
                    {
                        if (AppConfig.DEBUG)
                        {
                            Log.v(AppConfig.LOG_TAG, "NetworkService > unregister client > " + msg.replyTo.hashCode());
                        }

                        removeClient(msg.replyTo);
                        break;
                    }
                    case MSG_SERVICE_SET_EDIT_CLIENT:
                    {
                        ClientMessenger client = findClient(msg.replyTo);
                        if (client != null)
                        {
                            if (AppConfig.DEBUG)
                            {
                                Log.v(AppConfig.LOG_TAG, "NetworkService > edit client > filterType: " + msg.arg1);
                            }
                            client.filterType = msg.arg1;
                        }
                        break;
                    }
                    case MSG_CONNECTION_STATE:
                    {
                        sendConnectionState(msg.replyTo);
                        break;
                    }
                    case MSG_SERVICE_TRYCONNECT:
                    {
                        if (mConnectionEngine.isConnect() && !isAuthenticated())
                        {
                            authenticate();
                        }
                        else
                        {
                            tryConnect();
                        }
                        break;
                    }
                    case MSG_AUTHENTICATION_STATE:
                    {
                        sendAuthenticationState(msg.replyTo);
                        break;
                    }
                    case MSG_SERVICE_SEND_MESSAGE_SERVER:
                    {

                        try
                        {
                            Bundle bundle = msg.getData();
                            bundle.setClassLoader(BaseDto.class.getClassLoader());
                            BaseDto dto = (BaseDto) bundle.getSerializable(NetworkService.MSG_KEY_DTO);
                            Class<? extends BaseDto.Result> callbackType = (Class<? extends BaseDto.Result>) bundle.getSerializable(NetworkService.MSG_KEY_DTO_RESULT_TYPE);
                            String method = bundle.getString(NetworkService.MSG_KEY_DTO_METHOD);


                            sendDtoToServerCallback(dto, callbackType, method, msg.replyTo);


                        }
                        catch (Exception ex)
                        {
                            sendError(msg.replyTo, ex.getMessage());
                        }
                        break;
                    }
                    default:
                    {
                        driveHandleMessage(msg);
                    }

                }// switch

            }
            catch (RemoteException e)
            {
                if (msg.replyTo != null)
                {
                    removeClient(msg.replyTo);
                }
            }
            catch (Exception ex)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "NetworkService > Exception handler message > " + ex.getMessage());
                }

                Bundle bundle = new Bundle();
                bundle.putString(NetworkService.MSG_KEY_ERROR_MESSAGE, ex.getMessage());
                sendMessageToAllClients(MSG_CLIENT_EXCEPTION_ERROR, bundle);
            }// catch

        }// handleMessage

    }// NetworkServiceHandler


    protected abstract boolean preSendDtoToServerCallback(BaseDto dto, Class<? extends BaseDto.Result> callbackType, String method, final Messenger sender);

    protected abstract void onRegisterClientToService(Messenger replyTo);

    protected abstract void driveHandleMessage(Message msg);


    final Messenger mMessenger = new Messenger(new NetworkServiceHandler());

    // =========================================================================================================
    // Service method
    // =========================================================================================================

    private ClientMessenger findClient(Messenger replyTo)
    {
        for (int i = 0; i < mClients.size(); i++)
        {
            if (replyTo.equals(mClients.get(i).messenger))
            {
                return mClients.get(i);
            }
        }
        return null;
    }

    private void removeClient(Messenger replyTo)
    {

        ClientMessenger client = findClient(replyTo);

        if (client != null)
        {
            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkService > removeClient > client: " + client.hashCode());
            }
            mClients.remove(client);
        }

    }

    protected void sendDtoToClients(BaseDto baseDTO, String subscribe, int flags, boolean isNeedModel)
    {

        for (int i = mClients.size() - 1; i >= 0; i--)
        {
            ClientMessenger client = mClients.get(i);

            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkService > sendDtoToClients > client.filterType: " + client.filterType + " - baseDTO.getTypeDto(): " + flags + " hasFlags:" + Helper.hasFlags(client.filterType, flags));
            }

            if (client.filterType > 0)
            {
                if (!Helper.hasFlags(client.filterType, flags))
                {
                    continue;
                }
            }

            Message msg = Message.obtain(null, MSG_CLIENT_ALL_SERVER_MESSAGE);
            Bundle bundle = new Bundle();
            bundle.putSerializable(NetworkService.MSG_KEY_DTO, baseDTO);
            bundle.putString(NetworkService.MSG_KEY_DTO_METHOD, subscribe);
            msg.setData(bundle);
            try
            {
                if (isNeedModel && baseDTO.modelId < 0)
                {
                    sendError(client.messenger, "Can not insert model in database");
                }

                client.messenger.send(msg);
            }
            catch (RemoteException e)
            {
                // The client is dead. Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }// try
            catch (Exception e)
            {
                // ...
            }// try
        }// for

    }

    protected boolean isExistClient()
    {
        return isExistClient(-1);
    }

    protected boolean isExistClient(int flags)
    {

        for (int i = mClients.size() - 1; i >= 0; i--)
        {
            ClientMessenger client = mClients.get(i);

            if (client.filterType <= 0)
            {
                return true;
            }
            else if (Helper.hasFlags(client.filterType, flags))
            {
                return true;
            }

        }// for

        return false;
    }

    //----------------

    private Bundle getConnectionStateBundle()
    {
        Bundle bundle = new Bundle();
        bundle.putString(MSG_KEY_CONNECTION_TOKEN, mConnectionEngine.getConnectionToken());
        bundle.putInt(MSG_KEY_CONNECTION_STATE, mConnectionEngine.getConnectionState());
        bundle.putString(MSG_KEY_CONNECTION_ID, mConnectionEngine.getConnectionID());

        return bundle;
    }

    private void sendConnectionState(Messenger replyTo) throws RemoteException
    {

        Message message = Message.obtain(null, MSG_CONNECTION_STATE);
        message.setData(getConnectionStateBundle());
        replyTo.send(message);
    }

    private void sendConnectionStateToAllClient()
    {
        sendMessageToAllClients(MSG_CONNECTION_STATE, getConnectionStateBundle());
    }

    //-------------------

    private Bundle getAuthenticationStateBundle()
    {
        Bundle bundle = new Bundle();
        bundle.putString(NetworkService.MSG_KEY_AUTH_MESSAGE, mAuthenticatedMessage);
        bundle.putInt(NetworkService.MSG_KEY_AUTH_ISSUE, mAuthenticateIssue);
        bundle.putBoolean(NetworkService.MSG_KEY_AUTH_STATE, isAuthenticated());
        return bundle;
    }

    private void sendAuthenticationState(Messenger replyTo) throws RemoteException
    {
        Message replyMsg = Message.obtain(null, MSG_AUTHENTICATION_STATE);

        replyMsg.setData(getAuthenticationStateBundle());
        replyTo.send(replyMsg);
    }

    private void sendAuthenticationStateToAllClient()
    {
        sendMessageToAllClients(MSG_AUTHENTICATION_STATE, getAuthenticationStateBundle());
    }

    //----------------------

    private Bundle getErrorBundle(String message)
    {
        Bundle bundle = new Bundle();
        bundle.putString(NetworkService.MSG_KEY_ERROR_MESSAGE, message);
        return bundle;
    }

    private void sendError(Messenger replyTo, String message) throws RemoteException
    {
        Message replyMsg = Message.obtain(null, MSG_CLIENT_EXCEPTION_ERROR);

        replyMsg.setData(getErrorBundle(message));
        replyTo.send(replyMsg);
    }

    private void sendErrorToAllClient(String message)
    {
        sendMessageToAllClients(MSG_CLIENT_EXCEPTION_ERROR, getErrorBundle(message));
    }

    //-------------------

    private void sendMessageToAllClients(int what, Bundle bundle)
    {

        for (int i = mClients.size() - 1; i >= 0; i--)
        {
            try
            {
                Message replyMsg = Message.obtain(null, what);
                replyMsg.setData(bundle);
                mClients.get(i).messenger.send(replyMsg);
            }
            catch (RemoteException e)
            {
                // The client is dead. Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }// try
            catch (Exception e)
            {
                // ...
            }// try
        }// for

    }

    protected void sendMessageToAllClients(int what, Bundle bundle, int flags)
    {
        Message replyMsg = Message.obtain(null, what);
        replyMsg.setData(bundle);
        sendMessageToAllClients(replyMsg, flags);
    }

    protected void sendMessageToAllClients(Message message, int flags)
    {

        for (int i = mClients.size() - 1; i >= 0; i--)
        {
            ClientMessenger client = mClients.get(i);

            Log.v(AppConfig.LOG_TAG, "NetworkService > sendMessageToAllClients > client.filterType: " + client.filterType + " - baseDTO.getTypeDto(): " + flags + " hasFlags:" + Helper.hasFlags(client.filterType, flags));

            if (client.filterType > 0)
            {
                if (!Helper.hasFlags(client.filterType, flags))
                {
                    continue;
                }
            }


            try
            {
                client.messenger.send(message);
            }
            catch (RemoteException e)
            {
                mClients.remove(i);
            }

        }

    }


    protected abstract void versionNotSupport(String message);

    public void expiredToken(boolean sendForClient, String message)
    {


        mUserSetting = null;
        mAuthenticateIssue = AUT_EXPIRED;
        onChangeAuthenticate(false);
        mAuthenticatedMessage = message;

        refreshNotification();

        DbBase.UserSetting.updateUserSettingExpire(true);
        //send data to client [expired, please login again]
        if (sendForClient)
        {
            sendAuthenticationStateToAllClient();
        }
    }

    public void onChangeAuthenticate(boolean isAuthenticated)
    {
        mIsAuthenticatedX = isAuthenticated;
    }

    public void authenticate()
    {

        if (AppConfig.DEBUG)
        {
            Log.v(AppConfig.LOG_TAG, "NetworkService > authenticating...");
        }

        mUserSetting = DbBase.UserSetting.select();


        if (mUserSetting != null && !mUserSetting.expired)
        {
            if (mConnectionEngine.isConnect())
            {
                if (!isRunningLoginAsyncTask)
                {
                    isRunningLoginAsyncTask = true;
                    AuthenticationJto.Login.Post post = new AuthenticationJto.Login.Post(mConnectionEngine.getConnectionID());

                    if (AppConfig.DEBUG)
                    {
                        Log.v(AppConfig.LOG_TAG, "NetworkService > authenticating...execute");
                    }

                    new LoginAsyncTask(post).execute();
                }

            }
            else
            {
                tryConnect();
            }
        }
        else
        {

            mAuthenticatedMessage = getString(R.string.samim_service_message_not_active);

            if (mUserSetting == null)
            {
                mAuthenticateIssue = AUT_NOT_ACTIVE;
            }
            else if (mUserSetting.expired)
            {
                mAuthenticateIssue = AUT_EXPIRED;
            }

            sendAuthenticationStateToAllClient();
            /*if (mIsExistBindService)
            {
				startLoginActivity(LoginActivity.START_FROM_SERVICE);
			}
			else
			{
				createLoginNotification();
			}*/
        }


    }


    public static boolean isRunningLoginAsyncTask = false;

    private class LoginAsyncTask extends AsyncTask<Void, Void, AuthenticationJto.Login.PostBack>
    {

        private AuthenticationJto.Login.Post mLoginPostData;
        private RestApi mRestApi;


        public LoginAsyncTask(AuthenticationJto.Login.Post loginPostData)
        {
            this.mLoginPostData = loginPostData;
            mRestApi = new RestApi(NetworkService.this, mUserSetting.token, mUserSetting.key);
            mRestApi.setBroadcastUnAuthorization(false);
        }

        @Override
        protected void onPreExecute()
        {
            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkService > authenticating...onPreExecute");
            }
            isRunningLoginAsyncTask = true;
        }

        @Override
        protected void onCancelled()
        {
            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkService > authenticating...onCancelled");
            }
            isRunningLoginAsyncTask = false;
            sendAuthenticationStateToAllClient();
        }

        @Override
        protected AuthenticationJto.Login.PostBack doInBackground(Void... s)
        {

            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkService > authenticating...doInBackground");
            }

            AuthenticationJto.Login.PostBack postBackJTO;

            try
            {
                postBackJTO = mRestApi.login(mLoginPostData);
            }
            catch (SamimException e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "authenticate > onError > error: " + e.getMessage());
                }

                postBackJTO = new AuthenticationJto.Login.PostBack();
                postBackJTO.stateCode = PostBackJto.RESULT_BAD_REQUEST;
                postBackJTO.detailMessage = e.getMessage();
                postBackJTO.subjectMessage = getString(R.string.samim_message_error);
                postBackJTO.isSuccessful = false;
                postBackJTO.isErrorSocket = true;
            }

            return postBackJTO;
        }

        @Override
        protected void onPostExecute(AuthenticationJto.Login.PostBack jto)
        {

            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkService > authenticating...onPostExecute");
            }

            isRunningLoginAsyncTask = false;

            if (AppConfig.DEBUG)
            {
                Log.i(AppConfig.LOG_TAG, "authenticate > " + ((jto.isSuccessful) ? "Successful" : "Failed"));
            }

            if (jto.stateCode == PostBackJto.RESULT_OK)
            {
                if (jto.isSuccessful)
                {
                    mAuthenticateIssue = AUT_OK;
                    onChangeAuthenticate(true);
                }
                else if (jto.isErrorSocket)
                {
                    mAuthenticateIssue = AUT_ERROR;
                    onChangeAuthenticate(false);

                    if (mConnectionEngine.isConnect())
                    {
                        mConnectionEngine.disconnect();
                    }
                    tryConnect();
                }
                else
                {
                    mAuthenticateIssue = AUT_ERROR;
                    onChangeAuthenticate(false);
                }
            }
            else if (jto.stateCode == PostBackJto.RESULT_BAD_REQUEST)
            {
                onChangeAuthenticate(false);
            }
            else if (jto.stateCode == PostBackJto.RESULT_BLOCKED)
            {
                onChangeAuthenticate(false);
                mAuthenticateIssue = AUT_BLOCKED;
            }
            else if (jto.stateCode == PostBackJto.RESULT_UNAUTHORIZED)
            {
                mAuthenticateIssue = AUT_ERROR;
                expiredToken(false, jto.detailMessage);
            }
            else if (jto.stateCode == PostBackJto.RESULT_NOT_SUPPORT_VERSION)
            {
                onChangeAuthenticate(false);
                mAuthenticateIssue = AUT_VERSION_NOT_SUPPORT;
            }

            mAuthenticatedMessage = jto.detailMessage;

            // send to all client

            refreshNotification();

            sendAuthenticationStateToAllClient();

            if (jto.stateCode == PostBackJto.RESULT_NOT_SUPPORT_VERSION)
            {
                versionNotSupport(jto.detailMessage);
            }

        }
    }


    // =========================================================================================================
    // Subscribe method server side
    // =========================================================================================================

    @SuppressWarnings("unused")
    private Object mSubscribeMethod = new Object()
    {

        public void Logout(int reason, String message)
        {
            if (AppConfig.DEBUG)
            {
                Log.e(AppConfig.LOG_TAG, "NetworkService > Logout: " + reason + ":" + message);
            }

            mConnectionEngine.disconnect();

            //ServerRequest = 1, Exception = 2, Unauthorized = 3, ClientRequest = 4, VersionNotSupport = 5, UserIsDisable = 6
            if (reason == LOGOUT_VERSION_NOT_SUPPORT)
            {
                versionNotSupport(message);
            }

        }

        public void Hello(HelloDto dto)
        {
            sendDtoToClients(dto, "hello", FLAG_HELLO, false);
        }

        public void UpdateApp(UpdateAppDto dto)
        {

            if (dto.newVersion.equals(AppConfig.getAppVersion()))
            {
                return;
            }

            onUpdateAppNotification(dto);
        }

    };


    private Object[] getSubscribes()
    {
        return new Object[]{mSubscribeMethod, getSubscribe()};
    }


    // =========================================================================================================
    // Connection event
    // =========================================================================================================


    @Override
    public void onConnectionError(Throwable error)
    {
        try
        {
            if (AppConfig.DEBUG)
            {
                if (error != null)
                {
                    Log.e(AppConfig.LOG_TAG, "NetworkService > onError > error: " + error.getClass().getName() + " - " + error.getMessage());
                }
            }
            //tryConnect();
        }
        catch (Exception ignored)
        {
        }
        // ...


    }

    @Override
    public void onConnectionStateChanged(int state)
    {
        try
        {
            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkService > onConnectionStateChanged > state: " + state);
            }

            if (state != ConnectionEventHandler.NET_Connected)
            {
                if (isAuthenticated())
                {
                    onChangeAuthenticate(false);
                    mAuthenticatedMessage = getString(R.string.samim_service_message_login_broken);
                    mAuthenticateIssue = AUT_LOADING;

                    sendAuthenticationStateToAllClient();
                }
            }

            sendConnectionStateToAllClient();

            if (state == ConnectionEventHandler.NET_Connected)
            {

                authenticate();

            }
            else if (state == ConnectionEventHandler.NET_Disconnected)
            {

                // ping http and reconnect
                tryConnect();

            }

        }
        catch (Exception e)
        {
            if (AppConfig.DEBUG)
            {
                Log.e(AppConfig.LOG_TAG, "NetworkService > onConnectionStateChanged Exception: " + e.getMessage());
            }
        }

        refreshNotification();

    }

    @Override
    public void onConnectionMessageChangeState(Messenger sender, String method, BaseDto dto, int state)
    {
        onConnectionMessageChangeState(sender, method, dto, state, -1);
    }

    public void onConnectionMessageChangeState(String method, BaseDto dto, int state, int flag)
    {
        onConnectionMessageChangeState(null, method, dto, state, flag);
    }

    public void onConnectionMessageChangeState(final Messenger sender, String method, final BaseDto dto, final int state, int flag)
    {
        try
        {
            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkService > onConnectionMessageChangeState method: " + method + "state: " + state);
            }


            if (onMessageDtoChangeState(dto, state))
            {

                Message replyMsg = Message.obtain(null, MSG_CLIENT_SEND_MESSAGE_SERVER_STATE);
                Bundle bundle = new Bundle();
                bundle.putSerializable(MSG_KEY_DTO_STATE_DTO, dto);
                bundle.putInt(MSG_KEY_DTO_STATE_VALUE, state);
                bundle.putString(MSG_KEY_DTO_STATE_METHOD, method);
                replyMsg.setData(bundle);

                if (sender == null)
                {
                    sendMessageToAllClients(replyMsg, flag);
                }
                else
                {
                    try
                    {
                        sender.send(replyMsg);
                    }
                    catch (RemoteException e)
                    {
                        removeClient(sender);
                    }
                }


            }
        }
        catch (Exception e)
        {
            if (AppConfig.DEBUG)
            {
                Log.e(AppConfig.LOG_TAG, "NetworkService > onConnectionMessageChangeState Exception: " + e.getMessage());
            }
        }


    }

    @Override
    public void onConnectionMessageCallback(Messenger sender, String method, BaseDto.Result resultDto)
    {
        onConnectionMessageCallback(sender, method, resultDto, -1);
    }

    public void onConnectionMessageCallback(String method, BaseDto.Result resultDto, int flag)
    {
        onConnectionMessageCallback(null, method, resultDto, flag);
    }

    public void onConnectionMessageCallback(Messenger sender, String method, BaseDto.Result resultDto, int flag)
    {
        try
        {
            if (AppConfig.DEBUG)
            {
                Log.v(AppConfig.LOG_TAG, "NetworkService > onConnectionMessageCallback method: " + method + "result: " + resultDto.toString());
            }

            if (onMessageDtoCallback(method, resultDto))
            {

                Message replyMsg = Message.obtain(null, MSG_CLIENT_SEND_MESSAGE_SERVER_CALLBACK);
                Bundle bundle = new Bundle();
                bundle.putSerializable(MSG_KEY_DTO_CALLBACK, resultDto);
                bundle.putString(MSG_KEY_DTO_CALLBACK_METHOD, method);
                replyMsg.setData(bundle);

                if (sender == null)
                {
                    sendMessageToAllClients(replyMsg, flag);
                }
                else
                {
                    try
                    {
                        sender.send(replyMsg);
                    }
                    catch (RemoteException e)
                    {
                        removeClient(sender);
                    }
                }


            }

        }
        catch (Exception e)
        {
            if (AppConfig.DEBUG)
            {
                Log.e(AppConfig.LOG_TAG, "NetworkService > onConnectionMessageCallback Exception: " + e.getMessage());
            }
        }


    }


    // =========================================================================================================
    // send message to server
    // =========================================================================================================

    protected final void sendDtoToServerCallback(final BaseDto dto, final Class<? extends BaseDto.Result> resultType, final String method, final Messenger sender)
    {
        if (preSendDtoToServerCallback(dto, resultType, method, sender))
        {
            mConnectionEngine.invoke(dto, resultType, method, sender);
        }
    }

    // =========================================================================================================
    // Property
    // =========================================================================================================

    protected final boolean isAuthenticated()
    {
        return mIsAuthenticatedX;
    }

    protected final int getAuthenticateIssue()
    {
        return mAuthenticateIssue;
    }

    protected final String getAuthenticateMessage()
    {
        return mAuthenticatedMessage;
    }

    protected final int getConnectionState()
    {
        return mConnectionEngine.getConnectionState();
    }

    protected final UserSettingModel getUserSetting()
    {
        return mUserSetting;
    }
    // =========================================================================================================
    // receiver
    // =========================================================================================================

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            // NetworkInfo mobNetInfo =
            // connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (activeNetInfo != null && activeNetInfo.isConnected())
            {
                if (AppConfig.DEBUG)
                {
                    Log.i(AppConfig.LOG_TAG, "Service Take BroadcastReceiver: " + intent.getAction());
                }
                tryConnect();
            }
            else
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (Helper.pingUrl(AppConfig.NETWORK_HOST_PING))
                            {
                                tryConnect();
                                return;
                            }
                        }
                        catch (IOException ignored)
                        {
                        }

                        mConnectionEngine.disconnect();
                    }
                }).start();
            }
            // if (mobNetInfo != null)
            // {
            // Log.v(AppSetting.LOG_TAG,
            // "NetworkService > BroadcastReceiver > Active Network Type : " +
            // mobNetInfo.getTypeName());
            // }

        }
    };

    // =========================================================================================================
    // ping test connection
    // =========================================================================================================

    private final Object mAsyncPingLock = new Object();
    private Handler mServiceInternalHandler = new Handler();
    private boolean mTryConnectCommand = false;

    private void tryConnect()
    {
        synchronized (mAsyncPingLock)
        {
            mTryConnectCommand = true;
            mAsyncPingLock.notifyAll();
        }

    }

    private Thread mAsyncPing = new Thread(new Runnable()
    {

        @Override
        public void run()
        {
            try
            {
                while (true)
                {

                    if (mTryConnectCommand)
                    {

                    }
                    else
                    {
                        synchronized (mAsyncPingLock)
                        {
                            if (AppConfig.DEBUG)
                            {
                                Log.v(AppConfig.LOG_TAG, "AsyncPing > Wait for ping(Forever!)");
                            }

                            mAsyncPingLock.wait();
                        }
                    }

                    mTryConnectCommand = false;

                    int time = AppConfig.NETWORK_HOST_TRY_CONNECT_EMERGENCY_DURATION;

                    try
                    {
                        if (AppConfig.DEBUG)
                        {
                            Log.v(AppConfig.LOG_TAG, "AsyncPing > Ping:" + AppConfig.NETWORK_HOST_PING);
                        }

                        if (Helper.pingUrl(AppConfig.NETWORK_HOST_PING))
                        {
                            if (AppConfig.DEBUG)
                            {
                                Log.i(AppConfig.LOG_TAG, "AsyncPing > Ping: Successful");
                            }

                            time = AppConfig.NETWORK_HOST_TRY_CONNECT_DURATION;
                        }
                        else
                        {
                            if (AppConfig.DEBUG)
                            {
                                Log.e(AppConfig.LOG_TAG, "AsyncPing > Ping: Failed");
                            }
                        }

                    }
                    catch (Exception ignored)
                    {

                    }

                    if (mTryConnectCommand)
                    {
                        continue;
                    }
                    else
                    {
                        synchronized (mAsyncPingLock)
                        {
                            if (AppConfig.DEBUG)
                            {
                                Log.v(AppConfig.LOG_TAG, "AsyncPing > Wait for connect:" + time);
                            }

                            mTryConnectCommand = false;
                            mAsyncPingLock.wait(time);
                            if (mTryConnectCommand)
                            {
                                continue;
                            }
                        }
                    }

                    if (AppConfig.DEBUG)
                    {
                        Log.v(AppConfig.LOG_TAG, "AsyncPing > Try Connect ...");
                    }

                    mServiceInternalHandler.post(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            if (!mConnectionEngine.isConnect())
                            {
                                mConnectionEngine.connect();
                            }
                        }
                    });

                    Thread.sleep(1500);

                }
            }
            catch (InterruptedException e)
            {
                Log.e(AppConfig.LOG_TAG, "AsyncPing -> Interrupted!");
            }

            Log.i(AppConfig.LOG_TAG, "AsyncPing -> Stoped");

        }

    });


    // =========================================================================================================
    // Activation receiver
    // =========================================================================================================

    BroadcastReceiver mActivationReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {

            if (AppConfig.DEBUG)
            {
                Log.i(AppConfig.LOG_TAG, "Service Take BroadcastReceiver: " + intent.getAction());
            }

            if (intent.getAction().equalsIgnoreCase(SamimAction.ACTIVATION_SUCCESSFUL))
            {

                authenticate();

            }
            else if (intent.getAction().equalsIgnoreCase(SamimAction.ACTIVATION_EXPIRED))
            {

                expiredToken(true, getString(R.string.samim_service_message_not_active));
                //send data to client [expired, please login again]
            }

        }

    };

    // =========================================================================================================
    // Notification Manager
    // =========================================================================================================

    @Override
    public final NotificationManager getNotificationManager()
    {
        return mNotificationManager;
    }

    @Override
    public final Context getNotificationManagerContext()
    {
        return this;
    }

}
