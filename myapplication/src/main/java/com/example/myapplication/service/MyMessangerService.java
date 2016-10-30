package com.example.myapplication.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.myapplication.R;
import com.example.myapplication.activity.ChatHistoryActivity;
import com.example.myapplication.application.Constant;
import com.example.myapplication.connection.socket.SubscribeMethod;
import com.example.myapplication.connection.socket.dto.ChatDeliverDto;
import com.example.myapplication.connection.socket.dto.ChatDto;
import com.example.myapplication.connection.socket.dto.ChatReadReportDto;
import com.example.myapplication.connection.socket.dto.ChatReadReportResponsibleDto;
import com.example.myapplication.connection.socket.dto.ChatResponsibleDto;
import com.example.myapplication.connection.socket.dto.ChatTypingReportDto;
import com.example.myapplication.connection.socket.dto.ContactDto;
import com.example.myapplication.connection.socket.dto.ContactResponsibleDto;
import com.example.myapplication.database.Db;
import com.example.myapplication.database.model.ChatModel;
import com.example.myapplication.database.model.ContactModel;
import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.entity.MediaTransferState;
import com.example.myapplication.factory.DownloadManager;
import com.example.myapplication.factory.Notifier;
import com.example.myapplication.factory.UploadManager;
import com.example.myapplication.fragment.IUploadMediaListener;
import com.example.myapplication.holder.DownloadHolder;
import com.example.myapplication.holder.UploadHolder;
import com.example.myapplication.mapper.ChatMapper;
import com.example.myapplication.mapper.ContactMapper;

import java.util.UUID;

import ir.hfj.library.activity.DownloadActivity;
import ir.hfj.library.application.AppConfig;
import ir.hfj.library.application.ConstantBase;
import ir.hfj.library.connection.socket.ConnectionEventHandler;
import ir.hfj.library.connection.socket.dto.BaseDto;
import ir.hfj.library.connection.socket.dto.UpdateAppDto;
import ir.hfj.library.service.NetworkService;
import ir.hfj.library.util.Helper;


public class MyMessangerService extends NetworkService implements DownloadManager.OnDownloadManagerListener
{


    //===========================================================================
    public static final String MSG_KEY_UPLOAD = "upload";
    public static final String MSG_KEY_UPLOAD_GUID = "upload.guid";
    public static final int MSG_CLIENT_UPLOAD_STATE = 1000;
    //public static final int MSG_SERVICE_UPLOAD = 1001;
    public static final int MSG_SERVICE_UPLOAD_CANCEL = 1002;
    //===========================================================================
    //===========================================================================
    public static final String MSG_KEY_DOWNLOAD = "download";
    public static final String MSG_KEY_DOWNLOAD_GUID = "download.guid";
    public static final int MSG_CLIENT_DOWNLOAD_STATE = 2000;
    public static final int MSG_SERVICE_DOWNLOAD = 2001;
    public static final int MSG_SERVICE_DOWNLOAD_CANCEL = 2002;
    //===========================================================================

    private DownloadManager mDownloadManager;
    private UploadManager mUploadManager;

    public synchronized static void start(Context context)
    {
        NetworkService.start(context, MyMessangerService.class);
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        if (!mIsAlive)
        {
            mChatSenderTask.start();
            mDownloadManager = new DownloadManager(this, this);
            mUploadManager = new UploadManager(this);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mChatSenderTask.interrupt();
    }


    @Override
    protected void onRegisterClientToService(Messenger replyTo)
    {

    }

    @Override
    protected final void driveHandleMessage(Message msg)
    {
        switch (msg.what)
        {
            case MSG_SERVICE_DOWNLOAD:
            {
                Bundle bundle = msg.getData();
                DownloadHolder.Send holder = (DownloadHolder.Send) bundle.getSerializable(MSG_KEY_DOWNLOAD);
                mDownloadManager.download(holder);
                break;
            }
            case MSG_SERVICE_DOWNLOAD_CANCEL:
            {
                Bundle bundle = msg.getData();
                UUID guid = (UUID) bundle.getSerializable(MSG_KEY_DOWNLOAD_GUID);
                mDownloadManager.cancel(guid);
                break;
            }
            case MSG_SERVICE_UPLOAD_CANCEL:
            {
                Bundle bundle = msg.getData();
                UUID guid = (UUID) bundle.getSerializable(MSG_KEY_UPLOAD_GUID);
                mUploadManager.cancel(guid);
                break;
            }
        }
    }

    @Override
    protected void versionNotSupport(String message)
    {
        Intent intent = new Intent(this, DownloadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ConstantBase.Param.KEY_DOWNLOAD_STATE, message);
        startActivity(intent);
    }


    @Override
    public void onUpdateAppNotification(UpdateAppDto dto)
    {
        Intent intent = new Intent(getBaseContext(), DownloadActivity.class);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat
                .Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setTicker(dto.message)
                .setContentText(dto.message)
                .setSmallIcon(R.drawable.ic_samim)
                //.setLargeIcon()
                .setOngoing(true).setContentIntent(pIntent)
                .build();

        getNotificationManager().notify(Constant.NotificationId.SAMIM_UPDATE_APP, notification);
    }

    @Override
    public boolean onMessageDtoCallback(String method, BaseDto.Result result)
    {
        if (method.equalsIgnoreCase(SubscribeMethod.AddChat))
        {
            onChatCallback((ChatResponsibleDto.Result) result);
        }
        else if (method.equalsIgnoreCase(SubscribeMethod.GetContact))
        {
            onTeacherCallBack((ContactResponsibleDto.Result) result);
        }
        else if (method.equalsIgnoreCase(SubscribeMethod.SetChatReadReport))
        {
            onSetChatReadReportCallBack((ChatReadReportResponsibleDto.Result) result);
        }


        return true;
    }

    @Override
    protected boolean preSendDtoToServerCallback(BaseDto dto, Class<? extends BaseDto.Result> callbackType, final String method, final Messenger sender)
    {
        if (method.equalsIgnoreCase(SubscribeMethod.AddChat) && dto instanceof ChatResponsibleDto)
        {
            final ChatResponsibleDto chatDto = (ChatResponsibleDto) dto;


            if (chatDto.contentType != ChatContentType.Text)
            {

                if (chatDto.content.fileToken != null)
                {
                    //ok
                    return true;
                }
                else if (chatDto.content.pathForUpload != null && !chatDto.content.pathForUpload.isEmpty())
                {
                    UploadHolder.Send send = new UploadHolder.Send();
                    send.path = chatDto.content.pathForUpload;
                    send.extension = chatDto.content.extension;
                    send.guid = chatDto.chatId;
                    send.type = chatDto.contentType;
                    send.title = "";

                    mUploadManager.upload(send, getUserSetting().token, getUserSetting().key, new IUploadMediaListener()
                    {
                        @Override
                        public void onUploadChangeState(UploadHolder.Received holder)
                        {

                            MyMessangerService.this.sendUploadChangeState(holder);

                            if (holder.state == MediaTransferState.Completed)
                            {
                                if (holder.fileToken != null && !holder.fileToken.isEmpty())
                                {

                                    ChatModel chatModel = Db.Chat.select(holder.guid);

                                    if (chatModel != null)
                                    {

                                        chatDto.content.fileToken = holder.fileToken;
                                        chatModel.content = chatDto.content;

                                        Db.Chat.update(chatModel);

                                        //send again
                                        sendDtoToServerCallback(chatDto, ChatResponsibleDto.Result.class, method, sender);

                                    }
                                    else
                                    {
                                        holder.state = MediaTransferState.Error;
                                        holder.message = getString(R.string.samim_message_upload_error);
                                        MyMessangerService.this.sendUploadChangeState(holder);
                                    }


                                }
                                else
                                {
                                    holder.state = MediaTransferState.Error;
                                    holder.message = getString(R.string.samim_message_upload_error);
                                    MyMessangerService.this.sendUploadChangeState(holder);
                                }
                            }


                        }
                    });

                    //new UploaderAsyncTask(chatDto, ChatResponsibleDto.Result.class, method, sender).execute();//call back
                    return false;
                }
                else
                {
                    return false;
                }

            }
            else
            {
                return true;
            }

        }
        else
        {
            return true;
        }


    }

    @Override
    public boolean onMessageDtoChangeState(BaseDto dto, int state)
    {
        return true;
    }


    @Override
    public void setServiceNotification(Intent startIntent, int flags, int startId)
    {
        Intent intent = new Intent(getBaseContext(), ChatHistoryActivity.class);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat
                .Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setTicker(getString(R.string.messanger_service_message_loading))
                .setContentText(getString(R.string.messanger_service_message_loading))
                .setSmallIcon(R.drawable.ic_samim)
                //.setLargeIcon()
                .setOngoing(true)
                .setContentIntent(pIntent)
                //.addAction(android.R.drawable.ic_media_previous, "Previous", pIntent)
                .build();


        startForeground(Constant.NotificationId.SAMIM_SERVICE, notification);
    }

    @Override
    public void refreshNotification()
    {

        int connectionState = getConnectionState();
        //int authenticateIssue = getAuthenticateIssue();
        boolean isAuthenticated = isAuthenticated();

        String ticker = getString(R.string.messanger_service_message_ready);

        if (connectionState == ConnectionEventHandler.NET_Connected)
        {
            if (!isAuthenticated)
            {

                ticker = getAuthenticateMessage();
            }
        }
        else if (connectionState == ConnectionEventHandler.NET_Connecting)
        {
            ticker = getString(R.string.messanger_message_connecting);
        }
        else if (connectionState == ConnectionEventHandler.NET_Disconnected)
        {
            ticker = getString(R.string.messanger_message_disconnected);
        }


        Intent intent = new Intent(getBaseContext(), ChatHistoryActivity.class);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat
                .Builder(this)
                .setContentTitle(getString(R.string.app_name))
                //.setTicker(ticker)
                .setContentText(ticker)
                .setSmallIcon((isAuthenticated) ?
                                      R.drawable.ic_samim :
                                      R.drawable.ic_samim_alert)
                //.setLargeIcon()
                .setOngoing(true).setContentIntent(pIntent)
                //.addAction(android.R.drawable.ic_media_previous, "Previous", pIntent)
                .build();

        getNotificationManager().notify(Constant.NotificationId.SAMIM_SERVICE, notification);

    }


    @Override
    public Object getSubscribe()
    {
        return mSubscribeMethod;
    }


    @SuppressWarnings("unused")
    private Object mSubscribeMethod = new Object()
    {


        public void ChatReceive(ChatDto dto)
        {
            ChatModel model;

            if (dto.isOwner)
            {
                model = ChatMapper.convertDtoToModel(dto, ChatModel.SENDER_STATE_SENDED);
            }
            else
            {
                model = ChatMapper.convertDtoToModel(dto, ChatModel.RECEIVER_STATE_RECEIVED);
            }

            if (Db.Chat.insert(model))
            {
                dto.modelId = model.getId();
                sendDtoToClients(dto, SubscribeMethod.ChatReceive, ClientFlags.FLAG_CHAT, true);
            }

            if (!dto.isOwner)
            {

                if (!isExistClient(ClientFlags.FLAG_CHAT))
                {
                    Notifier.sendNotifyChat(MyMessangerService.this, dto);

                }
            }

        }

        public void ChatReadReport(ChatReadReportDto dto)
        {
            if (dto == null || dto.chatGuids == null)
            {
                return;
            }

            for (UUID chatGuid : dto.chatGuids)
            {
                //Db.Chat.updateState(chatGuid, (dto.isOwner) ? ChatModel.RECEIVER_STATE_REPORT_READ : ChatModel.SENDER_STATE_SEEN);


                ChatModel model = Db.Chat.select(chatGuid);
                if (model != null)
                {


                    if (dto.isOwner)
                    {
                        if (ChatModel.RECEIVER_STATE_RECEIVED <= model.state && model.state <= ChatModel.RECEIVER_STATE_REPORT_READ)
                        {
                            if (model.state == ChatModel.RECEIVER_STATE_REPORT_READ)
                            {
                                //dont need update
                                continue;
                            }
                            model.state = ChatModel.RECEIVER_STATE_REPORT_READ;

                        }
                    }
                    else
                    {
                        if (ChatModel.SENDER_STATE_SENDING <= model.state && model.state <= ChatModel.SENDER_STATE_SEEN)
                        {
                            if (model.state == ChatModel.SENDER_STATE_SEEN)
                            {
                                //dont need update
                                continue;
                            }
                            model.state = ChatModel.SENDER_STATE_SEEN;
                        }
                    }

                    model.readDate = Helper.getDateTimeNow(dto.intervalTime);
                    Db.Chat.update(model);
                }


            }

            sendDtoToClients(dto, SubscribeMethod.ChatReadReport, ClientFlags.FLAG_CHAT, false);

        }

        public void ChatTypingReportReceive(ChatTypingReportDto dto)
        {
            if (dto == null || dto.chatUserGuid == null)
            {
                return;
            }

            sendDtoToClients(dto, SubscribeMethod.ChatTypingReportReceive, ClientFlags.FLAG_CHAT, false);

        }

        public void ChatDeliver(ChatDeliverDto dto)
        {

            ChatModel model = Db.Chat.select(dto.chatId);
            if (model != null && dto.isDeliver)
            {
                if (model.state >= ChatModel.SENDER_STATE_SEEN)
                {
                    //dont need update
                    return;
                }

                model.state = ChatModel.SENDER_STATE_DELIVER;
                model.deliverDate = Helper.getDateTimeNow(dto.intervalTime);
                if (Db.Chat.update(model))
                {
                    dto.modelId = model.getId();
                    sendDtoToClients(dto, SubscribeMethod.ChatDeliver, ClientFlags.FLAG_CHAT, true);
                }
            }

        }

    };

    private void onTeacherCallBack(ContactResponsibleDto.Result result)
    {
        if (result.isValid())
        {

            for (ContactDto dto : result.contactDtos)
            {
                ContactModel model = ContactMapper.ConvertDtoToModel(dto);

                if (!Db.Contact.insert(model))
                {
                    result.isSuccessful = false;
                    result.baseMessage = getString(R.string.samim_message_db_insert_error);
                }
            }
        }
    }

    private void onSetChatReadReportCallBack(ChatReadReportResponsibleDto.Result result)
    {
        if (result.isValid())
        {
            for (UUID chatId : result.request.chatGuids)
            {
                ChatModel model = Db.Chat.select(chatId);
                if (model != null)
                {
                    if (ChatModel.RECEIVER_STATE_RECEIVED <= model.state && model.state <= ChatModel.RECEIVER_STATE_REPORT_READ)
                    {
                        if (model.state != ChatModel.RECEIVER_STATE_REPORT_READ)
                        {
                            model.state = ChatModel.RECEIVER_STATE_REPORT_READ;
                            Db.Chat.update(model);
                        }
                    }

                }
            }
        }
    }

    private void onChatCallback(ChatResponsibleDto.Result result)
    {


        if (result.isSuccessful)
        {
            if (result.isNewId)
            {
                ChatModel model = Db.Chat.select(result.request.chatId);
                if (model != null)
                {
                    ChatModel newModel = ChatMapper.clone(model, result.chatId);
                    Db.Chat.insert(newModel);
                    Db.Chat.delete(model);
                }
            }


            ChatModel model = Db.Chat.select(result.chatId);
            if (model != null)
            {
                model.state = ChatModel.SENDER_STATE_SENDED;
                if (Db.Chat.update(model))
                {
                    return;
                }
            }

            result.isSuccessful = false;
            result.baseMessage = getString(R.string.samim_message_db_insert_error);
        }


    }


    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    @Override
    public void onChangeAuthenticate(boolean isAuthenticated)
    {
        super.onChangeAuthenticate(isAuthenticated);

        if (getConnectionState() == ConnectionEventHandler.NET_Connected && isAuthenticated())
        {
            notifyChatSender();
        }

    }


    private static final Object mChatWaitObject = new Object();
    // private boolean mChatSenderRunning = true;

    private void notifyChatSender()
    {
        //  mChatSenderRunning = true;
        synchronized (mChatWaitObject)
        {
            mChatWaitObject.notifyAll();
        }
    }

    /*private Thread mChatSenderTask = new Thread(new Runnable()
    {
        private final Object sendWaitObject = new Object();
        private boolean isSuccess = false;
        private int mChatCountTry = 0;

        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    isSuccess = false;

                    ChatModel model = Db.Chat.selectFirstNotSended();

                    if (model != null)
                    {
                        ChatResponsibleDto chatDto = ChatMapper.convertModelToDto(model);

                        //===========================================
                        //===========================================
                        if (chatDto.contentType != ChatContentType.Text)
                        {

                            if (chatDto.content.fileToken != null)
                            {
                                //ok
                            }
                            else if (chatDto.content.pathForUpload != null && !chatDto.content.pathForUpload.isEmpty())
                            {
                                UploadHolder.Send send = new UploadHolder.Send();
                                send.path = chatDto.content.pathForUpload;
                                send.extension = chatDto.content.extension;
                                send.guid = chatDto.chatId;
                                send.type = chatDto.contentType;
                                send.periodTitle = "";

                                mUploadManager.upload(send, getUserSetting().token, getUserSetting().key, new IUploadMediaListener()
                                {
                                    @Override
                                    public void onUploadChangeState(UploadHolder.Received holder)
                                    {

                                        MyMessangerService.this.sendUploadChangeState(holder);

                                        if (holder.state == MediaTransferState.Completed)
                                        {
                                            if (holder.fileToken != null && !holder.fileToken.isEmpty())
                                            {

                                                ChatModel chatModel = Db.Chat.select(holder.guid);

                                                if (chatModel != null)
                                                {

                                                    chatDto.content.fileToken = holder.fileToken;
                                                    chatDto.content.thumbnailToken = holder.thumbnailToken;
                                                    chatModel.content = chatDto.content;

                                                    Db.Chat.update(chatModel);

                                                    //send again
                                                    //sendDtoToServerCallback(chatDto, ChatResponsibleDto.Result.class, method, sender);
                                                    //============================================================
                                                    //Wake up
                                                    isSuccess = true;
                                                    synchronized (sendWaitObject)
                                                    {
                                                        sendWaitObject.notifyAll();
                                                    }
                                                    //=============================================================

                                                }
                                                else
                                                {
                                                    holder.state = MediaTransferState.Error;
                                                    holder.message = getString(R.string.samim_message_upload_error);
                                                    MyMessangerService.this.sendUploadChangeState(holder);
                                                }


                                            }
                                            else
                                            {
                                                holder.state = MediaTransferState.Error;
                                                holder.message = getString(R.string.samim_message_upload_error);
                                                MyMessangerService.this.sendUploadChangeState(holder);
                                            }
                                        }
                                        else if (holder.state == MediaTransferState.Cancel)
                                        {
                                            isSuccess = true;
                                        }
                                        else if (holder.state == MediaTransferState.Error)
                                        {

                                        }


                                    }
                                });


                            }


                        }
                        //======================================================================================
                        //======================================================================================
                        //Send dto
                        mConnectionEngine.invokeDirect(chatDto, ChatResponsibleDto.Result.class, SubscribeMethod.AddChat, new ConnectionMessageEventHandler()
                        {
                            @Override
                            public void onConnectionMessageChangeState(Messenger sender, String method, BaseDto dto, int state)
                            {
                                MyMessangerService.this.onConnectionMessageChangeState(method, dto, state, ClientFlags.FLAG_CHAT);
                            }
                            @Override
                            public void onConnectionMessageCallback(Messenger sender, String method, BaseDto.Result result)
                            {
                                MyMessangerService.this.onConnectionMessageCallback(method, result, ClientFlags.FLAG_CHAT);
                                isSuccess = result.isSuccessful;
                                synchronized (sendWaitObject)
                                {
                                    sendWaitObject.notifyAll();
                                }
                            }
                        });

                        //==============================================================
                        //Wait for response
                        try
                        {
                            synchronized (sendWaitObject)
                            {
                                sendWaitObject.wait();
                            }
                        }
                        catch (InterruptedException ignored)
                        {
                        }
                        //==============================================================

                        if (isSuccess)
                        {
                            mChatCountTry = 0;
                        }
                        else
                        {
                            mChatCountTry++;
                        }

                        mChatSenderRunning = (mChatCountTry < 3);
                    }
                    else
                    {
                        mChatSenderRunning = false;
                    }


                    if (!mChatSenderRunning)
                    {
                        mChatCountTry = 0;
                        synchronized (mChatWaitObject)
                        {
                            mChatWaitObject.wait();
                        }
                    }


                }
            }

            catch (InterruptedException e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "ChatSender -> Interrupted!");
                }
            }

        }
    });*/

    private Thread mChatSenderTask = new Thread(new Runnable()
    {
        //private final Object sendWaitObject = new Object();
        //private boolean isSuccess = false;
        //private int mChatCountTry = 0;

        @Override
        public void run()
        {
            try
            {
                while (true)
                {

                    synchronized (mChatWaitObject)
                    {
                        mChatWaitObject.wait();
                    }

                    //-----------------------------------------------------------------
                    //Send stored waiting chat message
                    //-----------------------------------------------------------------

                    for (ChatModel chatModel : Db.Chat.selectAllNotSended())
                    {
                        if (chatModel != null)
                        {

                            ChatResponsibleDto chatDto = ChatMapper.convertModelToDto(chatModel);

                            sendDtoToServerCallback(chatDto, ChatResponsibleDto.Result.class, SubscribeMethod.AddChat, null);

                        }
                    }

                    //-----------------------------------------------------------------
                    //Send stored read report
                    //-----------------------------------------------------------------

                    for (UUID userGuid : Db.Chat.selectUnReportedReadedUser())
                    {
                        ChatReadReportResponsibleDto dto = new ChatReadReportResponsibleDto();
                        dto.chatGuids = Db.Chat.selectUnReportedReadedMessage(userGuid);
                        if (dto.chatGuids.size() > 0)
                        {
                            sendDtoToServerCallback(dto, ChatReadReportResponsibleDto.Result.class, SubscribeMethod.SetChatReadReport, null);
                        }
                    }


                }
            }
            catch (InterruptedException e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "ChatSender -> Interrupted!");
                }
            }

        }
    });

    //==============================================================================================
    //==============================================================================================
    //==============================================================================================
    //==============================================================================================


//    public class UploaderAsyncTask extends AsyncTask<Void, Integer, FileJto.PostBack> implements UploadRestApi.OnUploadProgress
//    {
//
//        private final ChatResponsibleDto mDto;
//        private final String mMethod;
//        private final Class<? extends ChatResponsibleDto.Result> mResultType;
//        private final Messenger mReplyTo;
//
//        public UploaderAsyncTask(ChatResponsibleDto chatDto, final Class<? extends ChatResponsibleDto.Result> resultType, final String method, final Messenger replyTo)
//        {
//            this.mDto = chatDto;
//            this.mMethod = method;
//            this.mResultType = resultType;
//            this.mReplyTo = replyTo;
//        }
//
//        @Override
//        protected FileJto.PostBack doInBackground(Void... files)
//        {
//
//            UserSettingModel userSetting = getUserSetting();
//            SamimRestApi restApi = new SamimRestApi(MyMessangerService.this, userSetting.token, userSetting.key);
//
//
//            FileJto.PostBack postBackJto;
//
//
//            try
//            {
//                postBackJto = restApi.uploadFile(
//                        new FileJto.Post(
//                                Base64.encodeBytes(FileManager.read(MyMessangerService.this, mDto.content.pathForUpload)),
//                                mDto.contentType,
//                                ""
//                        ));
//            }
//            catch (SamimException e)
//            {
//                postBackJto = new FileJto.PostBack();
//                postBackJto.stateCode = PostBackJTO.RESULT_BAD_REQUEST;
//                postBackJto.detailMessage = e.getMessage();
//                postBackJto.subjectMessage = getString(ir.hfj.library.R.string.samim_login_message_title_error);
//            }
//
//            /*switch (mDto.contentType)
//            {
//                case ChatModel.CONTENT_TYPE_IMAGE:
//                {
//
//                    try
//                    {
//                        File file = new File(((ChatImageContent) mDto.content).path);
//                        int size = (int) file.length();
//                        byte[] bytes = new byte[size];
//                        try
//                        {
//                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
//                            buf.read(bytes, 0, bytes.length);
//                            buf.close();
//                        }
//                        catch (Exception e)
//                        {
//                            throw new SamimException(e.getMessage());
//                        }
//
//                        //=================================================================
//
//                        postBackJto = restApi.uploadFile(
//                                new FileJto.Post(
//                                        Base64.encodeBytes(bytes),
//                                        mDto.contentType
//                                ));
//                    }
//                    catch (SamimException e)
//                    {
//                        postBackJto = new FileJto.PostBack();
//                        postBackJto.stateCode = PostBackJTO.RESULT_BAD_REQUEST;
//                        postBackJto.detailMessage = e.getMessage();
//                        postBackJto.subjectMessage = getString(ir.hfj.library.R.string.samim_login_message_title_error_network);
//                    }
//                    break;
//                }
//                default:
//                    return null;
//            }*/
//
//            return postBackJto;
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values)
//        {
//            sendUploadProgress(mDto.chatId, values[0]);
//        }
//
//        @Override
//        protected void onCancelled()
//        {
//            super.onCancelled();
//            sendUploadError(mDto.chatId, "onCancelled");
//        }
//
//        @Override
//        protected void onCancelled(FileJto.PostBack selector_gridview)
//        {
//            super.onCancelled(selector_gridview);
//            sendUploadError(mDto.chatId, "onCancelled");
//        }
//
//        int oldPercent = -1;
//        @Override
//        public void OnUploadProgress(long v, long total)
//        {
//            int percent = (int) ((v / (float) total) * 100);
//            if (oldPercent != percent)
//            {
//                oldPercent = percent;
//                publishProgress(oldPercent);
//            }
//        }
//
//        @Override
//        protected void onPostExecute(FileJto.PostBack postBack)
//        {
//            if (postBack.stateCode == PostBackJTO.RESULT_OK)
//            {
//                if (postBack.token != null && !postBack.token.isEmpty())
//                {
//                    if (oldPercent != 100)
//                    {
//                        oldPercent = 100;
//                        sendUploadProgress(mDto.chatId, oldPercent);
//                    }
//
//                    ChatModel chatModel = Db.Chat.select(mDto.chatId);
//
//                    if (chatModel != null)
//                    {
//
//                        switch (mDto.contentType)
//                        {
//                            case Image:
//                            {
//                                ((ChatImageContent) mDto.content).fileToken = postBack.token;
//                                ((ChatImageContent) mDto.content).thumbnailToken = postBack.thumbnailToken;
//                                chatModel.content = mDto.content;
//                                break;
//                            }
//                            default:
//                                return;
//                        }
//
//                        Db.Chat.update(chatModel);
//
//                        sendUploadComplete(mDto.chatId, postBack.token);
//
//                        sendDtoToServerCallback(mDto, mResultType, mMethod, mReplyTo);
//
//                    }
//                    else
//                    {
//                        sendUploadError(mDto.chatId, "onPostExecute chatModel not found");
//                    }
//
//
//                }
//                else
//                {
//                    sendUploadError(mDto.chatId, "onPostExecute null");
//                }
//            }
//            else
//            {
//                sendUploadError(mDto.chatId, postBack.detailMessage);
//            }
//
//
//        }
//
//
//    }


//    public void sendUploadProgress(UUID guid, int value)
//    {
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(MSG_KEY_CLIENT_UPLOAD_GUID, guid);
//        bundle.putInt(MSG_KEY_CLIENT_UPLOAD_PROGRESS, value);
//        sendMessageToAllClients(MSG_CLIENT_UPLOAD_PROGRESS, bundle, ClientFlags.FLAG_CHAT);
//    }
//
//    public void sendUploadError(UUID guid, String message)
//    {
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(MSG_KEY_CLIENT_UPLOAD_GUID, guid);
//        bundle.putString(MSG_KEY_CLIENT_UPLOAD_MESSAGE, message);
//        sendMessageToAllClients(MSG_CLIENT_UPLOAD_ERROR, bundle, ClientFlags.FLAG_CHAT);
//    }
//
//    public void sendUploadComplete(UUID guid, String token)
//    {
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(MSG_KEY_CLIENT_UPLOAD_GUID, guid);
//        bundle.putString(MSG_KEY_CLIENT_UPLOAD_TOKEN, token);
//        sendMessageToAllClients(MSG_CLIENT_UPLOAD_COMPLETE, bundle, ClientFlags.FLAG_CHAT);
//    }

    //==============================================================================================
    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    @Override
    public void onDownloadChangeState(DownloadHolder.Received holder)
    {

        if (holder.state == MediaTransferState.Completed)
        {
            ChatModel model = Db.Chat.select(holder.guid);
            if (model != null)
            {
                model.path = holder.path;
                Db.Chat.update(model);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable(MSG_KEY_DOWNLOAD, holder);
        sendMessageToAllClients(MSG_CLIENT_DOWNLOAD_STATE, bundle, ClientFlags.FLAG_CHAT);
    }


    public void sendUploadChangeState(UploadHolder.Received holder)
    {
        if (holder.state == MediaTransferState.Completed)
        {
            ChatModel model = Db.Chat.select(holder.guid);
            if (model != null)
            {
                model.content.fileToken = holder.fileToken;
                model.content.extension = holder.extension;
                Db.Chat.update(model);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable(MSG_KEY_UPLOAD, holder);
        sendMessageToAllClients(MSG_CLIENT_UPLOAD_STATE, bundle, ClientFlags.FLAG_CHAT);
    }


}
