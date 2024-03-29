package com.example.myapplication.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ChatAdapter;
import com.example.myapplication.application.Constant;
import com.example.myapplication.connection.socket.dto.ChatResponsibleDto;
import com.example.myapplication.connection.socket.dto.ChatTypingReportDto;
import com.example.myapplication.database.Db;
import com.example.myapplication.database.model.ChatModel;
import com.example.myapplication.database.model.ContactModel;
import com.example.myapplication.dictionary.DataDictionary;
import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.factory.ChatFactory;
import com.example.myapplication.factory.FileManager;
import com.example.myapplication.holder.ChatHolder;
import com.example.myapplication.holder.DownloadHolder;
import com.example.myapplication.holder.UploadHolder;
import com.example.myapplication.mapper.ChatMapper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.UUID;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.exception.MyMessangerException;
import ir.hfj.library.ui.NhToast;
import ir.hfj.library.util.Helper;
import ir.hfj.library.util.MesssangerVoiceRecorder;

import static com.example.myapplication.actionbar.ChatActionBar.COMMAND_STEP;


public class ChatFragment extends Fragment implements
        OnClickListener,
        IChatFragment,
        IUploadMediaListener,
        IDownloadMediaListener,
        MediaFragment.OnSelectedListener,
        View.OnTouchListener
{

    private RecyclerView uiRecyclerView;
    private ChatAdapter mAdapter = null;
    private EditText uiEdtMessage;
    private ImageView uiImgContactImage;
    private TextView uiTxvContactName;
    private TextView uiTxvTypingState;
    private IChatController mChatController;
    private IChatHistoryUpdatable mChatHistoryController;
    private ContactModel mChatUser;
    public long mStartTypingTime = 0;
    public long mStopTypingTime = 0;
    private RecyclerView.LayoutManager mLayoutManager;
    private SlidingUpPanelLayout uiSlidingUpPanelLayout;
    private MediaFragment mMediaFragment;
    private MesssangerVoiceRecorder mVoiceRecorder;
    private DisplayImageOptions mOptions;


    String outputPath = null;
    public ChatFragment()
    {
    }

    public static ChatFragment newInstance(UUID userGuid)
    {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        // In side by side view, In First Time maybe user is null
        if (userGuid != null)
        {
            args.putSerializable(Constant.Param.KEY_CHAT_CONTACT_GUID, userGuid);
        }
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadData();

        initView();

        initAdapter();

    }

    private void loadData()
    {
        UUID userGuid = (UUID) getArguments().getSerializable(Constant.Param.KEY_CHAT_CONTACT_GUID);

        if (userGuid != null)
        {
            mChatUser = Db.Contact.selectByGuid(userGuid);
        }
    }
    private void initView()
    {
        setRetainInstance(true);
    }

    private void initAdapter()
    {
        if (mAdapter == null)
        {
            if (mChatUser != null)
            {
                DataDictionary<UUID, ChatHolder> chatHolders = ChatMapper.convertModelToHolder(Db.Chat.selectByUser(mChatUser.getGuid()));
                mAdapter = new ChatAdapter(getActivity(), chatHolders, mChatUser);
            }
        }
    }

    //     _        _   _             ____               ____            _   _
    //    / \   ___| |_(_) ___  _ __ | __ )  __ _ _ __  / ___|  ___  ___| |_(_) ___  _ __
    //   / _ \ / __| __| |/ _ \| '_ \|  _ \ / _` | '__| \___ \ / _ \/ __| __| |/ _ \| '_ \
    //  / ___ \ (__| |_| | (_) | | | | |_) | (_| | |     ___) |  __/ (__| |_| | (_) | | | |
    // /_/   \_\___|\__|_|\___/|_| |_|____/ \__,_|_|    |____/ \___|\___|\__|_|\___/|_| |_|
    //

    private void setChatUserImage(String imageUrl)
    {
        if (uiImgContactImage != null)
        {
            uiImgContactImage.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(imageUrl, uiImgContactImage, mOptions);
        }
    }

    public void setIsTyping(boolean isTyping)
    {
        MyRunnable runnable;

        if (uiTxvTypingState != null)
        {
            if (isTyping)
            {
                uiTxvTypingState.setText(R.string.messanger_chat_message_isTyping);
                runnable = new MyRunnable();
                uiTxvTypingState.postDelayed(runnable, 8000);
            }

            else
            {
                uiTxvTypingState.setText("");
            }
        }
    }

    private class MyRunnable implements Runnable
    {

        private final int command;

        private MyRunnable()
        {
            command = ++COMMAND_STEP;
        }

        public void run()
        {
            if (COMMAND_STEP == command)
            {
                uiTxvTypingState.setText("");
            }
        }
    }

    public void setChatUserName(String name)
    {
        if (uiTxvContactName != null)
        {
            uiTxvContactName.setText(name);
        }
    }

    //  _____                                     _      ___                      _     _        __  __      _   _               _
    // |  ___| __ __ _  __ _ _ __ ___   ___ _ __ | |_   / _ \__   _____ _ __ _ __(_) __| | ___  |  \/  | ___| |_| |__   ___   __| |
    // | |_ | '__/ _` |/ _` | '_ ` _ \ / _ \ '_ \| __| | | | \ \ / / _ \ '__| '__| |/ _` |/ _ \ | |\/| |/ _ \ __| '_ \ / _ \ / _` |
    // |  _|| | | (_| | (_| | | | | | |  __/ | | | |_  | |_| |\ V /  __/ |  | |  | | (_| |  __/ | |  | |  __/ |_| | | | (_) | (_| |
    // |_|  |_|  \__,_|\__, |_| |_| |_|\___|_| |_|\__|  \___/  \_/ \___|_|  |_|  |_|\__,_|\___| |_|  |_|\___|\__|_| |_|\___/ \__,_|
    //                 |___/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {


        mLayoutManager = new LinearLayoutManager(getActivity());
        final View rootView;
        rootView = inflater.inflate(R.layout.fragment_chat_list, container, false);

        uiRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_chat_list_recycleview);
        uiEdtMessage = (EditText) rootView.findViewById(R.id.fragment_chat_edt_message);
        uiSlidingUpPanelLayout = (SlidingUpPanelLayout) rootView.findViewById(R.id.fragment_chat_list_slidingUpPanel_layout);
        uiImgContactImage = (ImageView) rootView.findViewById(R.id.fragment_chat_list_user_image);
        uiTxvContactName = (TextView) rootView.findViewById(R.id.fragment_chat_list_contact_name);
        uiTxvTypingState = (TextView) rootView.findViewById(R.id.fragment_chat_list_typing_state);

        rootView.findViewById(R.id.fragment_chat_btn_send).setOnClickListener(this);
        rootView.findViewById(R.id.fragment_chat_btn_voice).setOnClickListener(this);
        rootView.findViewById(R.id.fragment_chat_btn_media).setOnClickListener(this);
        rootView.findViewById(R.id.fragment_chat_btn_voice).setOnTouchListener(this);

        if (mMediaFragment == null)
        {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            mMediaFragment = MediaFragment.newInstance();
            mMediaFragment.setTargetFragment(this, 0);
            ft.replace(R.id.fragment_chat_list_media_frame_layout, mMediaFragment);
            ft.commit();
        }

        else
        {
            mMediaFragment.setTargetFragment(this, 0);
        }

        mOptions = AppConfig.createDisplayImageOptions();

        return rootView;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        if (activity instanceof IChatController)
        {
            mChatController = (IChatController) activity;
        }

        if (activity instanceof IChatHistoryUpdatable)
        {
            mChatHistoryController = (IChatHistoryUpdatable) activity;
        }
    }

    @Override
    public void onResume()
    {
        Activity activity = getActivity();
        super.onResume();
        if (mChatUser != null)
        {
            try
            {
                setChatUserImage(mChatUser.imageUrl);
                setChatUserName(mChatUser.firstName + " " + mChatUser.lastName);
                mChatController.readChat(mChatUser.getGuid());
            }
            catch (MyMessangerException e)
            {

                if (activity != null)
                {
                    NhToast.makeText(activity, activity.getString(R.string.messanger_message_service_not_responding), NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
                }
            }
        }

        else
        {
            setChatUserImage(null);
        }
    }


    @Override
    public void onStart()
    {
        super.onStart();
        uiEdtMessage.addTextChangedListener(chatTextWatcher);


    }

    @Override
    public void onStop()
    {
        super.onStop();
        uiEdtMessage.removeTextChangedListener(chatTextWatcher);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mMediaFragment.setTargetFragment(null, -1);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        uiRecyclerView.setLayoutManager(mLayoutManager);
        uiRecyclerView.setAdapter(mAdapter);

        // uiRecyclerView.scrollToPosition(uiRecyclerView.getAdapter().getItemCount());
    }

    //   ___           ____ _ _      _      _     _     _
    //  / _ \ _ __    / ___| (_) ___| | __ | |   (_)___| |_ ___ _ __   ___ _ __
    // | | | | '_ \  | |   | | |/ __| |/ / | |   | / __| __/ _ \ '_ \ / _ \ '__|
    // | |_| | | | | | |___| | | (__|   <  | |___| \__ \ ||  __/ | | |  __/ |
    //  \___/|_| |_|  \____|_|_|\___|_|\_\ |_____|_|___/\__\___|_| |_|\___|_|
    //

    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        if (id == R.id.fragment_chat_btn_send)
        {

            final Activity activity = getActivity();
            if (activity == null)
            {
                return;
            }

            ChatFactory.Text(activity, mChatUser.getGuid(), uiEdtMessage.getText().toString(), new ChatFactory.EventListener()
            {
                @Override
                public void onInvalidation(String message)
                {
                    NhToast.makeText(activity, message, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
                }
                @Override
                public void onErrorDb(String message)
                {
                    NhToast.makeText(activity, message, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
                }
                @Override
                public void onException(Exception ex)
                {

                }
                @Override
                public void onReadyForSend(ChatResponsibleDto dto, ChatModel model)
                {

                    addMessage(ChatMapper.convertModelToHolder(model));

                    try
                    {
                        mChatController.sendMessage(dto);

                        if (mChatHistoryController != null)
                        {
                            mChatHistoryController.updateChatHistory(model.contactUserId);
                        }

                        restChatText();

                    }
                    catch (MyMessangerException e)
                    {
                        NhToast.makeText(activity, e.getMessage(), NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
                    }

                }
            });


        }
        else if (id == R.id.fragment_chat_btn_media)
        {
            uiSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }


    }

    //  ___ ____ _           _     _____                                     _
    // |_ _/ ___| |__   __ _| |_  |  ___| __ __ _  __ _ _ __ ___   ___ _ __ | |_
    //  | | |   | '_ \ / _` | __| | |_ | '__/ _` |/ _` | '_ ` _ \ / _ \ '_ \| __|
    //  | | |___| | | | (_| | |_  |  _|| | | (_| | (_| | | | | | |  __/ | | | |_
    // |___\____|_| |_|\__,_|\__| |_|  |_|  \__,_|\__, |_| |_| |_|\___|_| |_|\__|
    //                                            |___/

    @Override
    public UUID getContactGuid()
    {
        return mChatUser.getGuid();
    }

    @Override
    public void addMessage(ChatHolder holder)
    {
        mAdapter.add(holder);
    }

    @Override
    public void replaceMessage(UUID chatId, ChatHolder holder)
    {
        mAdapter.replace(chatId, holder);
    }

    @Override
    public void updateMessage(ChatHolder holder)
    {
        mAdapter.update(holder);
    }
    @Override
    public void setContactTypingState(boolean isTyping, UUID contactGuid)
    {
        if (mChatUser.getGuid().equals(contactGuid))
        {
            setIsTyping(isTyping);
        }
    }

    //  ___ ____                      _                 _   __  __          _ _         _     _     _
    // |_ _|  _ \  _____      ___ __ | | ___   __ _  __| | |  \/  | ___  __| (_) __ _  | |   (_)___| |_ ___ _ __   ___ _ __
    //  | || | | |/ _ \ \ /\ / / '_ \| |/ _ \ / _` |/ _` | | |\/| |/ _ \/ _` | |/ _` | | |   | / __| __/ _ \ '_ \ / _ \ '__|
    //  | || |_| | (_) \ V  V /| | | | | (_) | (_| | (_| | | |  | |  __/ (_| | | (_| | | |___| \__ \ ||  __/ | | |  __/ |
    // |___|____/ \___/ \_/\_/ |_| |_|_|\___/ \__,_|\__,_| |_|  |_|\___|\__,_|_|\__,_| |_____|_|___/\__\___|_| |_|\___|_|
    //

    @Override
    public void onDownloadChangeState(DownloadHolder.Received holder)
    {
        mAdapter.onDownloadChangeState(holder);
    }

    //  ___ _   _       _                 _   __  __          _ _         _     _     _
    // |_ _| | | |_ __ | | ___   __ _  __| | |  \/  | ___  __| (_) __ _  | |   (_)___| |_ ___ _ __   ___ _ __
    //  | || | | | '_ \| |/ _ \ / _` |/ _` | | |\/| |/ _ \/ _` | |/ _` | | |   | / __| __/ _ \ '_ \ / _ \ '__|
    //  | || |_| | |_) | | (_) | (_| | (_| | | |  | |  __/ (_| | | (_| | | |___| \__ \ ||  __/ | | |  __/ |
    // |___|\___/| .__/|_|\___/ \__,_|\__,_| |_|  |_|\___|\__,_|_|\__,_| |_____|_|___/\__\___|_| |_|\___|_|
    //           |_|

    @Override
    public void onUploadChangeState(UploadHolder.Received holder)
    {
        mAdapter.onUploadChangeState(holder);
    }

    //  _____         _ __        __    _       _
    // |_   _|____  _| |\ \      / /_ _| |_ ___| |__   ___ _ __
    //   | |/ _ \ \/ / __\ \ /\ / / _` | __/ __| '_ \ / _ \ '__|
    //   | |  __/>  <| |_ \ V  V / (_| | || (__| | | |  __/ |
    //   |_|\___/_/\_\\__| \_/\_/ \__,_|\__\___|_| |_|\___|_|


    private TextWatcher chatTextWatcher = new TextWatcher()
    {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {

        }
        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count)
        {

        }
        @Override
        public void afterTextChanged(Editable editable)
        {
            long now = System.currentTimeMillis();

            if (uiEdtMessage.getText().length() <= 0)
            {
                if (!(mStopTypingTime + AppConfig.CHAT_REPORT_TYPING_INTERVAL > now))
                {
                    mStopTypingTime = now;
                    ChatTypingReportDto dto = createChatTypingReportDto(false);
                    try
                    {
                        mChatController.sendChatTypingReport(dto);
                    }
                    catch (MyMessangerException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                if (!(mStartTypingTime + AppConfig.CHAT_REPORT_TYPING_INTERVAL > now))
                {
                    mStartTypingTime = now;
                    ChatTypingReportDto dto = createChatTypingReportDto(true);
                    try
                    {
                        mChatController.sendChatTypingReport(dto);
                    }
                    catch (MyMessangerException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        }

        private ChatTypingReportDto createChatTypingReportDto(boolean isTyping)
        {
            ChatTypingReportDto dto = new ChatTypingReportDto();
            dto.isTyping = isTyping;
            dto.chatUserGuid = mChatUser.getGuid();
            return dto;
        }
    };

    //  __  __          _ _       _____                                     _         _     _     _
    // |  \/  | ___  __| (_) __ _|  ___| __ __ _  __ _ _ __ ___   ___ _ __ | |_      | |   (_)___| |_ ___ _ __   ___ _ __
    // | |\/| |/ _ \/ _` | |/ _` | |_ | '__/ _` |/ _` | '_ ` _ \ / _ \ '_ \| __|     | |   | / __| __/ _ \ '_ \ / _ \ '__|
    // | |  | |  __/ (_| | | (_| |  _|| | | (_| | (_| | | | | | |  __/ | | | |_   _  | |___| \__ \ ||  __/ | | |  __/ |
    // |_|  |_|\___|\__,_|_|\__,_|_|  |_|  \__,_|\__, |_| |_| |_|\___|_| |_|\__| (_) |_____|_|___/\__\___|_| |_|\___|_|
    //                                           |___/

    @Override
    public void onSelectedImage(String path)
    {
        final Activity activity = getActivity();
        if (activity == null)
        {
            return;
        }

        ChatFactory.Image(activity, mChatUser.getGuid(), path, uiEdtMessage.getText().toString(), new ChatFactory.EventListener()
        {
            @Override
            public void onInvalidation(String message)
            {
                NhToast.makeText(activity, message, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
            }
            @Override
            public void onErrorDb(String message)
            {
                NhToast.makeText(activity, message, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
            }
            @Override
            public void onException(Exception ex)
            {

            }
            @Override
            public void onReadyForSend(ChatResponsibleDto dto, ChatModel model)
            {

                addMessage(ChatMapper.convertModelToHolder(model));

                try
                {
                    mChatController.sendMessage(dto);

                    if (mChatHistoryController != null)
                    {
                        mChatHistoryController.updateChatHistory(model.contactUserId);
                    }

                    restChatText();

                }
                catch (MyMessangerException e)
                {
                    NhToast.makeText(getActivity(), e.getMessage(), NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onSelectedVideo(String path)
    {
        final Activity activity = getActivity();
        if (activity == null)
        {
            return;
        }

        ChatFactory.Video(activity, mChatUser.getGuid(), path, new ChatFactory.EventListener()
        {
            @Override
            public void onInvalidation(String message)
            {
                NhToast.makeText(activity, message, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
            }
            @Override
            public void onErrorDb(String message)
            {
                NhToast.makeText(activity, message, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
            }
            @Override
            public void onException(Exception ex)
            {

            }
            @Override
            public void onReadyForSend(ChatResponsibleDto dto, ChatModel model)
            {

                addMessage(ChatMapper.convertModelToHolder(model));

                try
                {
                    mChatController.sendMessage(dto);

                    if (mChatHistoryController != null)
                    {
                        mChatHistoryController.updateChatHistory(model.contactUserId);
                    }

                    restChatText();

                }
                catch (MyMessangerException e)
                {
                    NhToast.makeText(getActivity(), e.getMessage(), NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onSelectedAudio(String path)
    {
        final Activity activity = getActivity();
        if (activity == null)
        {
            return;
        }

        ChatFactory.Voice(activity, mChatUser.getGuid(), path, new ChatFactory.EventListener()
        {
            @Override
            public void onInvalidation(String message)
            {
                NhToast.makeText(activity, message, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
            }
            @Override
            public void onErrorDb(String message)
            {
                NhToast.makeText(activity, message, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
            }
            @Override
            public void onException(Exception ex)
            {
            }
            @Override
            public void onReadyForSend(ChatResponsibleDto dto, ChatModel model)
            {

                addMessage(ChatMapper.convertModelToHolder(model));

                try
                {
                    mChatController.sendMessage(dto);

                    if (mChatHistoryController != null)
                    {
                        mChatHistoryController.updateChatHistory(model.contactUserId);
                    }

                    restChatText();

                }
                catch (MyMessangerException e)
                {
                    NhToast.makeText(getActivity(), e.getMessage(), NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onSelectedFile(String path)
    {
        final Activity activity = getActivity();
        if (activity == null)
        {
            return;
        }

        ChatFactory.File(activity, mChatUser.getGuid(), path, new ChatFactory.EventListener()
        {
            @Override
            public void onInvalidation(String message)
            {
                NhToast.makeText(activity, message, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
            }
            @Override
            public void onErrorDb(String message)
            {
                NhToast.makeText(activity, message, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
            }
            @Override
            public void onException(Exception ex)
            {

            }
            @Override
            public void onReadyForSend(ChatResponsibleDto dto, ChatModel model)
            {

                addMessage(ChatMapper.convertModelToHolder(model));

                try
                {
                    mChatController.sendMessage(dto);

                    if (mChatHistoryController != null)
                    {
                        mChatHistoryController.updateChatHistory(model.contactUserId);
                    }

                    restChatText();

                }
                catch (MyMessangerException e)
                {
                    NhToast.makeText(getActivity(), e.getMessage(), NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void restChatText()
    {
        try
        {
            uiEdtMessage.setText("");

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(uiEdtMessage.getWindowToken(), 0);
        }
        catch (Exception ignored)
        {

        }

    }
    @Override
    public boolean onTouch(View view, MotionEvent event)
    {

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (view.getId() == R.id.fragment_chat_btn_voice)
                {
                    try
                    {
                        mVoiceRecorder = new MesssangerVoiceRecorder();
                        outputPath = FileManager.getDirectory(ChatContentType.Voice) + "/" + Helper.generateRandomString(64) + ".3gp";
                        mVoiceRecorder.record(outputPath);
                        //VoiceHelper.recordVoice(outputPath);
                        NhToast.makeText(getActivity(), "شروع ضبط صدا", NhToast.ToastIcon.INFO, NhToast.LENGTH_SHORT).show();
                    }
                    catch (Exception e)
                    {
                        Log.e("Record Voice --------> ", e.toString());
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (view.getId() == R.id.fragment_chat_btn_voice)
                {
//                    VoiceHelper.stopRecording();
                    mVoiceRecorder.release();
                    NhToast.makeText(getActivity(), "اتمام ضبط صدا", NhToast.ToastIcon.INFO, NhToast.LENGTH_SHORT).show();
                    onSelectedAudio(outputPath);

                }
                return true;
        }
        return false;
    }


}
