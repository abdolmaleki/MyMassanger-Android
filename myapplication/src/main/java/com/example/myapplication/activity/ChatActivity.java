package com.example.myapplication.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.myapplication.R;
import com.example.myapplication.application.Constant;
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
import com.example.myapplication.fragment.ChatFragment;
import com.example.myapplication.fragment.ChatHistoryFragment;
import com.example.myapplication.fragment.IChatController;
import com.example.myapplication.fragment.IChatFragment;
import com.example.myapplication.fragment.IChatHistoryFragment;
import com.example.myapplication.fragment.IChatHistoryUpdatable;
import com.example.myapplication.fragment.IChatUpdatable;
import com.example.myapplication.fragment.IDownloadController;
import com.example.myapplication.fragment.IDownloadMediaListener;
import com.example.myapplication.fragment.IUploadController;
import com.example.myapplication.fragment.IUploadMediaListener;
import com.example.myapplication.handler.SamimClientHandler;
import com.example.myapplication.holder.ChatHolder;
import com.example.myapplication.holder.DownloadHolder;
import com.example.myapplication.holder.UploadHolder;
import com.example.myapplication.mapper.ChatMapper;
import com.example.myapplication.mapper.ContactMapper;
import com.example.myapplication.service.ClientFlags;
import com.example.myapplication.setting.Setting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ir.hfj.library.activity.ActivationActivity;
import ir.hfj.library.application.App;
import ir.hfj.library.connection.socket.ConnectionEventHandler;
import ir.hfj.library.connection.socket.dto.BaseDto;
import ir.hfj.library.exception.SamimException;
import ir.hfj.library.service.NetworkService;
import ir.hfj.library.ui.NhDialog;
import ir.hfj.library.util.ContactUtil;
import ir.hfj.library.util.DangerousPermission;
import ir.hfj.library.util.DateUtil;
import ir.hfj.library.util.Helper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChatActivity extends AppCompatActivity implements
        IChatUpdatable,
        IChatController,
        IChatHistoryUpdatable,
        IDownloadController,
        IUploadController


{

    private ServiceHandler mNetworkService;
    private IChatHistoryFragment mIChatHistoryFragment;
    private IChatFragment mIChatFragment;
    private IDownloadMediaListener mIDownloadMediaListener;
    private IUploadMediaListener mIUploadMediaListener;
    private long mAutoIdResponse;
    private NhDialog mDialogLoading;
    private UUID mChatUserGuid;
    private boolean mIsBigView;
    private boolean mIsShownSplashScreen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        initActionBar();


        initView();

        splashScreen();

        loadCurrentContact();

        loadData();

        //loadSavedInstanceData(savedInstanceState);

        //initActionBar();

        if (savedInstanceState == null)
        {
            submitChatHistoryFragment();
        }
        else
        {
            UUID chatUserGuid = (UUID) savedInstanceState.getSerializable(Constant.Param.KEY_CHAT_CONTACT_GUID);

            if (chatUserGuid != null)
            {
                switchChatUserFragment(chatUserGuid);
            }
            else
            {
                submitChatHistoryFragment();
            }
        }

        activationIfNeeded();

        bindService();

    }

    private void activationIfNeeded()
    {
        if (App.getInstance(this).isExpired())
        {
            Intent myIntent = new Intent(this, ActivationActivity.class);
            //myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myIntent.putExtra(ActivationActivity.KEY_CAUSE, App.getInstance(this).isUserSetting() ? ActivationActivity.PARAM_CAUSE_EXPIRE : ActivationActivity.PARAM_CAUSE_ACTIVE);
            startActivity(myIntent);
        }
    }

    private void loadCurrentContact()
    {

    }

    private void splashScreen()
    {
        final View uiLytSplash = findViewById(R.id.activity_main_lvt_splash);


        if (mIsShownSplashScreen)
        {
            uiLytSplash.setVisibility(View.GONE);
            return;
        }

        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setStartOffset(1000);
        animation.setDuration(500);
        animation.setFillAfter(true);
        animation.setInterpolator(new AccelerateInterpolator(2));


        uiLytSplash.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }
            @Override
            public void onAnimationEnd(Animation animation)
            {
                uiLytSplash.setVisibility(View.GONE);
                mIsShownSplashScreen = true;
            }
            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
    }

    @SuppressWarnings("deprecation")
    private void initView()
    {
        // get landscape orientation
        //if (getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_90 || getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_270)
        //if (Helper.isLandscapeOrientation(this))

        mIsBigView = findViewById(R.id.content).getTag().equals("landscape");

        if (isBigView())
        {
            Display display = getWindowManager().getDefaultDisplay();
            int wholeWidth = display.getWidth();
            LinearLayout.LayoutParams params;
            final float scale = getResources().getDisplayMetrics().density;

            FrameLayout chatLayout = (FrameLayout) findViewById(R.id.fragment_place_chat_panel);
            params = (LinearLayout.LayoutParams) chatLayout.getLayoutParams();
            params.width = (int) (((wholeWidth / 3f) * scale) * 2);
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            chatLayout.setLayoutParams(params);

            FrameLayout historyLayout = (FrameLayout) findViewById(R.id.fragment_place_chathistory_panel);
            params = (LinearLayout.LayoutParams) historyLayout.getLayoutParams();
            params.width = (int) (((wholeWidth / 3f) * scale) * 1);
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            historyLayout.setLayoutParams(params);

        }


        refreshFragmentUi();


    }

    @Override
    protected void onSaveInstanceState(Bundle onOrientChange)
    {
        onOrientChange.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(onOrientChange);
        if (mChatUserGuid != null)
        {
            onOrientChange.putSerializable(Constant.Param.KEY_CHAT_CONTACT_GUID, mChatUserGuid);
        }

    }
    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (!DangerousPermission.checkRequestPermissionsResult(this, requestCode, permissions, grantResults))
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void loadData()
    {

    }

    private void initActionBar()
    {

        android.support.v7.app.ActionBar myActionBar = getSupportActionBar();
        myActionBar.hide();
//        mActionBar = new ChatActionBar(this, getSupportActionBar());
//        mActionBar.setTitle(R.string.title_activity_chat);
    }

    private boolean isBigView()
    {
        return mIsBigView;
    }

    private void submitChatHistoryFragment()
    {
        try
        {
            if (!isFinishing())
            {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                android.support.v4.app.Fragment fragment = ChatHistoryFragment.newInstance();
                ft.replace(R.id.fragment_place_chathistory, fragment);
                ft.commit();

                refreshStatusBar();
            }

        }
        catch (Exception ignored)
        {

        }
    }

    private void bindService()
    {
        mNetworkService = new ServiceHandler(this);
        mNetworkService.doBindService();
    }

    //  ___ ____ _           _   _   _           _       _        _     _
    // |_ _/ ___| |__   __ _| |_| | | |_ __   __| | __ _| |_ __ _| |__ | | ___
    //  | | |   | '_ \ / _` | __| | | | '_ \ / _` |/ _` | __/ _` | '_ \| |/ _ \
    //  | | |___| | | | (_| | |_| |_| | |_) | (_| | (_| | || (_| | |_) | |  __/
    // |___\____|_| |_|\__,_|\__|\___/| .__/ \__,_|\__,_|\__\__,_|_.__/|_|\___|
    //                                |_|

    @Override
    public void switchChatUserFragment(UUID chatUserGuid)
    {
        try
        {
            mChatUserGuid = chatUserGuid;

            if (!isFinishing())
            {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                android.support.v4.app.Fragment fragment = ChatFragment.newInstance(chatUserGuid);
                ft.replace(R.id.fragment_place_chat, fragment);
                ft.commit();
            }
            else
            {
                return;
            }

            refreshFragmentUi();

            if (mIChatHistoryFragment != null && isBigView())
            {
                mIChatHistoryFragment.setCurrentContact(mChatUserGuid);
            }

        }
        catch (Exception ignored)
        {
            Log.e("Chat Fragment ---> ", ignored.getMessage());
        }
    }

    private void refreshFragmentUi()
    {
        if (isBigView())
        {
            findViewById(R.id.fragment_place_chat_panel).setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_place_chathistory_panel).setVisibility(View.VISIBLE);
        }
        else if (mChatUserGuid != null)
        {
            findViewById(R.id.fragment_place_chat_panel).setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_place_chathistory_panel).setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.fragment_place_chat_panel).setVisibility(View.GONE);
            findViewById(R.id.fragment_place_chathistory_panel).setVisibility(View.VISIBLE);
        }

    }


    //  ___ ____ _           _    ____            _             _ _
    // |_ _/ ___| |__   __ _| |_ / ___|___  _ __ | |_ _ __ ___ | | | ___ _ __
    //  | | |   | '_ \ / _` | __| |   / _ \| '_ \| __| '__/ _ \| | |/ _ \ '__|
    //  | | |___| | | | (_| | |_| |__| (_) | | | | |_| | | (_) | | |  __/ |
    // |___\____|_| |_|\__,_|\__|\____\___/|_| |_|\__|_|  \___/|_|_|\___|_|
    //

    @Override
    public void readChat(UUID chatUserGuid) throws SamimException
    {

        if (!Db.Chat.readAllUnReadMessage(chatUserGuid))
        {
            return;
        }

        ChatReadReportResponsibleDto dto = new ChatReadReportResponsibleDto();
        dto.chatGuids = Db.Chat.selectUnReportedReadedMessage(chatUserGuid);

        try
        {
            if (dto.chatGuids.size() > 0)
            {
                mNetworkService.invokeChatReportRead(dto);
            }
        }
        catch (RemoteException e)
        {
            throw new SamimException(getString(R.string.messanger_message_service_not_responding));
        }
    }

//    @Override
//    public void setChatUserImage(String imageUrl)
//    {
//        if (imageUrl != null)
//        {
//            mActionBar.setChatUserImage(imageUrl);
//        }
//        else
//        {
//            mActionBar.setUserImageInvisible();
//        }
//
//    }

    @Override
    public void sendMessage(ChatResponsibleDto dto) throws SamimException
    {
        try
        {
            mNetworkService.invokeChat(dto);
        }
        catch (RemoteException ignored)
        {
            throw new SamimException(getString(R.string.messanger_message_service_not_responding));
        }
    }

    @Override
    public void sendChatTypingReport(ChatTypingReportDto dto) throws SamimException
    {
        try
        {
            mNetworkService.invokeChatTypingReport(dto);
        }
        catch (RemoteException ignored)
        {
            throw new SamimException(getString(R.string.messanger_message_service_not_responding));
        }
    }

    //  ___ ____ _           _   _   _ _     _                   _   _           _       _        _     _
    // |_ _/ ___| |__   __ _| |_| | | (_)___| |_ ___  _ __ _   _| | | |_ __   __| | __ _| |_ __ _| |__ | | ___
    //  | | |   | '_ \ / _` | __| |_| | / __| __/ _ \| '__| | | | | | | '_ \ / _` |/ _` | __/ _` | '_ \| |/ _ \
    //  | | |___| | | | (_| | |_|  _  | \__ \ || (_) | |  | |_| | |_| | |_) | (_| | (_| | || (_| | |_) | |  __/
    // |___\____|_| |_|\__,_|\__|_| |_|_|___/\__\___/|_|   \__, |\___/| .__/ \__,_|\__,_|\__\__,_|_.__/|_|\___|
    //                                                     |___/      |_|

    @Override
    public void updateChatHistory(UUID contactGuid)
    {
        if (mIChatHistoryFragment != null)
        {
            mIChatHistoryFragment.updateChatHistory(contactGuid);
        }
    }

    //  ___ ____                      _                 _  ____            _             _ _
    // |_ _|  _ \  _____      ___ __ | | ___   __ _  __| |/ ___|___  _ __ | |_ _ __ ___ | | | ___ _ __
    //  | || | | |/ _ \ \ /\ / / '_ \| |/ _ \ / _` |/ _` | |   / _ \| '_ \| __| '__/ _ \| | |/ _ \ '__|
    //  | || |_| | (_) \ V  V /| | | | | (_) | (_| | (_| | |__| (_) | | | | |_| | | (_) | | |  __/ |
    // |___|____/ \___/ \_/\_/ |_| |_|_|\___/ \__,_|\__,_|\____\___/|_| |_|\__|_|  \___/|_|_|\___|_|
    //

    @Override
    public void download(DownloadHolder.Send holder) throws SamimException
    {
        try
        {
            mNetworkService.sendDownload(holder);
        }
        catch (RemoteException e)
        {
            throw new SamimException(getString(R.string.messanger_message_service_not_responding));
        }
    }

    @Override
    public void cancelDownload(UUID guid) throws SamimException
    {
        try
        {
            mNetworkService.cancelDownload(guid);
        }
        catch (RemoteException e)
        {
            throw new SamimException(getString(R.string.messanger_message_service_not_responding));
        }
    }

    //  ___ _   _       _                 _  ____            _             _ _
    // |_ _| | | |_ __ | | ___   __ _  __| |/ ___|___  _ __ | |_ _ __ ___ | | | ___ _ __
    //  | || | | | '_ \| |/ _ \ / _` |/ _` | |   / _ \| '_ \| __| '__/ _ \| | |/ _ \ '__|
    //  | || |_| | |_) | | (_) | (_| | (_| | |__| (_) | | | | |_| | | (_) | | |  __/ |
    // |___|\___/| .__/|_|\___/ \__,_|\__,_|\____\___/|_| |_|\__|_|  \___/|_|_|\___|_|
    //           |_|

    @Override
    public void cancelUpload(UUID guid) throws SamimException
    {
        try
        {
            mNetworkService.cancelUpload(guid);
        }
        catch (RemoteException e)
        {
            throw new SamimException(getString(R.string.messanger_message_service_not_responding));
        }
    }


    //  ____                  _          _   _                 _ _
    // / ___|  ___ _ ____   _(_) ___ ___| | | | __ _ _ __   __| | | ___ _ __
    // \___ \ / _ \ '__\ \ / / |/ __/ _ \ |_| |/ _` | '_ \ / _` | |/ _ \ '__|
    //  ___) |  __/ |   \ V /| | (_|  __/  _  | (_| | | | | (_| | |  __/ |
    // |____/ \___|_|    \_/ |_|\___\___|_| |_|\__,_|_| |_|\__,_|_|\___|_|
    //

    private static class ServiceHandler extends SamimClientHandler<ChatActivity>
    {

        public ServiceHandler(ChatActivity activity)
        {
            super(activity, ClientFlags.FLAG_CHAT);
        }

        @Override
        protected void onChatReceived(ChatDto dto)
        {
            final ChatActivity activity = getActivity();

            final ChatModel model = Db.Chat.select(dto.chatId);


            if (model == null)
            {
                return;
            }


            if (activity.mIChatFragment != null && activity.mIChatFragment.getContactGuid().equals(model.contactUserId))
            {

                activity.mIChatFragment.addMessage(ChatMapper.convertModelToHolder(model));

                if (dto.isOwner)
                {
                    model.state = ChatModel.SENDER_STATE_DELIVER;
                }
                else
                {
                    model.state = ChatModel.RECEIVER_STATE_READ;
                }

                Db.Chat.update(model);

                if (!dto.isOwner)
                {
                    try
                    {
                        ChatReadReportResponsibleDto requestDto = new ChatReadReportResponsibleDto();
                        requestDto.chatGuids = new ArrayList<>();
                        requestDto.chatGuids.add(model.getGuid());
                        invokeChatReportRead(requestDto);
                    }
                    catch (RemoteException ignored)
                    {

                    }
                }

            }

            if (activity.mIChatHistoryFragment != null)
            {
                activity.mIChatHistoryFragment.updateChatHistory(model.contactUserId);
            }

        }

        @Override
        protected void onChatCallback(ChatResponsibleDto.Result dto)
        {
            ChatActivity activity = getActivity();

            if (activity.mIChatFragment != null)
            {
                if (dto.isSuccessful)
                {

                    ChatModel model = Db.Chat.select(dto.chatId);
                    if (model != null)
                    {
                        if (dto.isNewId)
                        {
                            activity.mIChatFragment.replaceMessage(dto.request.chatId, ChatMapper.convertModelToHolder(model));
                        }
                        else
                        {
                            activity.mIChatFragment.updateMessage(ChatMapper.convertModelToHolder(model));
                        }
                    }
                }
                else
                {
                    ChatModel model = Db.Chat.select(dto.request.chatId);
                    if (model != null)
                    {
                        ChatHolder holder = ChatMapper.convertModelToHolder(model);
                        holder.alertMessage = dto.baseMessage;
                        activity.mIChatFragment.updateMessage(holder);
                    }
                }
            }

            if (activity.mIChatHistoryFragment != null)
            {
                activity.mIChatHistoryFragment.updateChatHistory(dto.request.receiverUserId);
            }
        }

        @Override
        protected void onChatDeliverReceived(ChatDeliverDto dto)
        {

            ChatActivity activity = getActivity();

            ChatModel model = Db.Chat.select(dto.chatId);

            if (model == null)
            {
                return;
            }

            if (activity.mIChatFragment != null)
            {
                activity.mIChatFragment.updateMessage(ChatMapper.convertModelToHolder(model));
            }

            if (activity.mIChatHistoryFragment != null)
            {
                activity.mIChatHistoryFragment.updateChatHistory(model.contactUserId);
            }

        }

        @Override
        protected void onChatReadReportReceived(ChatReadReportDto dto)
        {

            ChatActivity activity = getActivity();

            UUID contactUserId = null;

            if (activity.mIChatFragment != null)
            {
                for (UUID chatId : dto.chatGuids)
                {
                    ChatModel model = Db.Chat.select(chatId);
                    if (model != null)
                    {
                        if (contactUserId == null)
                        {
                            contactUserId = model.contactUserId;
                        }
                        activity.mIChatFragment.updateMessage(ChatMapper.convertModelToHolder(model));
                        if (activity.mIChatHistoryFragment != null)
                        {
                            activity.mIChatHistoryFragment.updateChatHistory(contactUserId);

                        }
                    }
                }
            }

            else if (activity.mIChatHistoryFragment != null)
            {
                for (UUID chatId : dto.chatGuids)
                {
                    ChatModel model = Db.Chat.select(chatId);
                    if (model != null)
                    {
                        contactUserId = model.contactUserId;
                    }
                }

                activity.mIChatHistoryFragment.updateChatHistory(contactUserId);
            }

        }

        @Override
        protected void onChatTypingReportReceive(ChatTypingReportDto dto)
        {

            ChatActivity activity = getActivity();


            if (activity.mIChatFragment != null)
            {
                activity.mIChatFragment.setContactTypingState(dto.isTyping, dto.chatUserGuid);
            }
            //ChatActivity activity = getActivity();

            ///////////////////////////////////////
            /// State 1: Portrait and chatFragment
            ///////////////////////////////////////
//            if (activity.mChatUserGuid != null)
//            {
//                if (dto.chatUserGuid.equals(activity.mChatUserGuid))
//                {
//                    if (dto.isTyping)
//                    {
//                        activity.mActionBar.setIsTyping(true);
//                    }
//                    //else
//                    //{
//                    // activity.mActionBar.setIsTyping(false);
//                    //}
//                }
//            }
            //////////////////////////////////////////////
            /// State 2: Portrait and chatHistory Fragment
            //////////////////////////////////////////////


            //////////////////////
            /// State 3: LandScape
            //////////////////////
        }


        @Override
        protected void onUploadChangeState(UploadHolder.Received uploadHolder)
        {
            ChatActivity activity = getActivity();

            if (activity.mIUploadMediaListener != null)
            {
                activity.mIUploadMediaListener.onUploadChangeState(uploadHolder);
            }

        }

        @Override
        protected void onDownloadChangeState(DownloadHolder.Received downloadHolder)
        {
            ChatActivity activity = getActivity();

            if (activity.mIDownloadMediaListener != null)
            {
                activity.mIDownloadMediaListener.onDownloadChangeState(downloadHolder);
            }

        }

        @Override
        protected void onContactCallback(ContactResponsibleDto.Result dto)
        {
            ChatActivity activity = getActivity();

            if (activity.mDialogLoading != null)
            {
                activity.mDialogLoading.dismiss();
            }
            //activity.mActionBar.stopProgressing();

            if (dto.isSuccessful)
            {
                getActivity().SyncContacts(dto.contactDtos);
                //activity.submitChatHistoryFragment();
                //save date refresh

                Setting.SaveRefreshDateTime(activity, Constant.Preference.Keys.REFRESH_TIME_CONTACT, DateUtil.now());
            }
            else if (activity.mAutoIdResponse != dto.autoIdResponse)
            {
                activity.mDialogLoading = new NhDialog(activity, NhDialog.DialogIcon.ERROR);
                activity.mDialogLoading.setCancelable(true);
                activity.mDialogLoading.setMainTitle(ir.hfj.library.R.string.messanger_message_error).setSubTitle(dto.baseMessage);
                activity.mDialogLoading.show();
            }

        }

        @Override
        protected void onServiceConnected()
        {
        }

        @Override
        protected void onServiceDisconnected()
        {
        }

        @Override
        protected void onReady()
        {
            ChatActivity activity = getActivity();

            //load setting
            if (getActivity().mNetworkService.isAuthenticated())
            {
                Date date = Setting.LoadRefreshDateTime(activity, Constant.Preference.Keys.REFRESH_TIME_CONTACT);

                if (Helper.isNeedAutoRefreshTime(date))
                {
                    getActivity().RequestForSyncContacts();
                }
            }

            getActivity().refreshStatusBar();
            // activity.mActionBar.startProgressing();
            try
            {
                //activity.mAutoIdResponse = invokeContact(dto);
            }
            catch (Exception ex)
            {
                //activity.mActionBar.stopProgressing();
            }
        }

        @Override
        protected void onChangeAuthenticationState(boolean isAuthenticated, String message, int authenticateIssue)
        {
            getActivity().refreshStatusBar();
        }

        @Override
        protected void onChangeConnectionState(int state)
        {
            getActivity().refreshStatusBar();

        }

        @Override
        protected void onMessageDtoChangeState(BaseDto dto, int state)
        {
        }
    }

    @Override
    public void refreshStatusBar()
    {
        if (mIChatHistoryFragment != null)
        {
            if (!mNetworkService.isBounded())
            {
                mIChatHistoryFragment.updateConnectionStatus(getResources().getString(R.string.messanger_message_service_not_responding));
                return;
            }
            else if (!mNetworkService.isReady())
            {
                return;
            }


            int connectionState = mNetworkService.getConnectionState();
            int authenticateIssue = mNetworkService.getAuthenticateIssue();
            boolean isAuthenticated = mNetworkService.isAuthenticated();

            if (connectionState == ConnectionEventHandler.NET_Connected)
            {
                if (isAuthenticated)
                {
                    mIChatHistoryFragment.updateConnectionStatus(getResources().getString(R.string.messanger_service_message_ready));
                }
                else
                {
                    mIChatHistoryFragment.updateConnectionStatus(mNetworkService.getAuthenticateMessage());
                    if (authenticateIssue == NetworkService.AUT_EXPIRED ||
                            authenticateIssue == NetworkService.AUT_NOT_ACTIVE ||
                            authenticateIssue == NetworkService.AUT_ERROR)
                    {
                        mIChatHistoryFragment.updateConnectionStatus(getResources().getString(R.string.messanger_login_message_error_auth_expired));
                    }
                    else if (authenticateIssue == NetworkService.AUT_VERSION_NOT_SUPPORT)
                    {
                        mIChatHistoryFragment.updateConnectionStatus(getResources().getString(R.string.messanger_login_message_error_version_notsupport));
                    }
                }
            }
            else if (connectionState == ConnectionEventHandler.NET_Connecting)
            {
                mIChatHistoryFragment.updateConnectionStatus(getResources().getString(R.string.messanger_message_connecting));
            }
            else if (connectionState == ConnectionEventHandler.NET_Disconnected)
            {
                mIChatHistoryFragment.updateConnectionStatus(getResources().getString(R.string.messanger_message_disconnected));
            }
        }


    }

    private void RequestForSyncContacts()
    {
        try
        {
            mNetworkService.invokeContact(createRefreshRequestDto());
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }

    private void SyncContacts(List<ContactDto> dtos)
    {
        Db.Contact.deleteAll();

        for (ContactDto dto : dtos)
        {
            ContactModel newContact = ContactMapper.ConvertDtoToModel(dto);
            Db.Contact.insert(newContact);
        }

    }


    @Override
    protected void onResume()
    {
        super.onResume();
        if (mNetworkService.isAuthenticated())
        {
            Date date = Setting.LoadRefreshDateTime(this, Constant.Preference.Keys.REFRESH_TIME_CONTACT);
            if (Helper.isNeedAutoRefreshTime(date))
            {
                RequestForSyncContacts();
                refreshStatusBar();
            }

        }
    }
    @Override
    public void onAttachFragment(Fragment fragment)
    {
        super.onAttachFragment(fragment);
    }

    @Override
    public void onAttachFragment(android.support.v4.app.Fragment fragment)
    {
        super.onAttachFragment(fragment);

        if (fragment instanceof IChatFragment)
        {
            mIChatFragment = (IChatFragment) fragment;
        }

        if (fragment instanceof IUploadMediaListener)
        {
            mIUploadMediaListener = (IUploadMediaListener) fragment;
        }

        if (fragment instanceof IDownloadMediaListener)
        {
            mIDownloadMediaListener = (IDownloadMediaListener) fragment;
        }

        if (fragment instanceof IChatHistoryFragment)
        {
            mIChatHistoryFragment = (IChatHistoryFragment) fragment;
        }
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        mIChatFragment = null;
        mIChatHistoryFragment = null;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mNetworkService != null)
        {
            mNetworkService.doUnbindService();
        }
    }

    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
        if (!isBigView() && mChatUserGuid != null)
        {
            mChatUserGuid = null;

            removeFragment((android.support.v4.app.Fragment) mIChatFragment);
            mIChatFragment = null;

            refreshFragmentUi();

            //setChatUserImage(null);

        }
        else
        {
            super.onBackPressed();
        }

        mChatUserGuid = null;

    }
    private void removeFragment(android.support.v4.app.Fragment fragment)
    {
        if (fragment != null)
        {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            //fragment = null;
        }

    }

    private ContactResponsibleDto createRefreshRequestDto()
    {
        ContactResponsibleDto dto = new ContactResponsibleDto();
        dto.phoneNumbers = ContactUtil.getContactsPhoneNumbers(this);
        return dto;
    }


}
