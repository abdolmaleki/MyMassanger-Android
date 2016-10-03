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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ChatAdapter;
import com.example.myapplication.application.Constant;
import com.example.myapplication.connection.socket.dto.ChatResponsibleDto;
import com.example.myapplication.connection.socket.dto.ChatTypingReportDto;
import com.example.myapplication.database.Db;
import com.example.myapplication.database.model.ChatModel;
import com.example.myapplication.database.model.StudentModel;
import com.example.myapplication.database.model.TeacherModel;
import com.example.myapplication.dictionary.DataDictionary;
import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.factory.ChatFactory;
import com.example.myapplication.factory.FileManager;
import com.example.myapplication.holder.ChatHolder;
import com.example.myapplication.holder.DownloadHolder;
import com.example.myapplication.holder.UploadHolder;
import com.example.myapplication.mapper.ChatMapper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.UUID;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.exception.SamimException;
import ir.hfj.library.ui.NhToast;
import ir.hfj.library.util.Helper;
import ir.hfj.library.util.SamimVoiceRecorder;


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
    private StudentModel mStudent;
    private EditText uiEdtMessage;
    private IChatController mChatController;
    private IChatHistoryUpdatable mChatHistoryController;
    private TeacherModel mChatUser;
    public long mStartTypingTime = 0;
    public long mStopTypingTime = 0;
    private RecyclerView.LayoutManager mLayoutManager;
    private SlidingUpPanelLayout uiSlidingUpPanelLayout;
    private MediaFragment mMediaFragment;
    private SamimVoiceRecorder mVoiceRecorder;

    String outputPath = null;

    public static ChatFragment newInstance(UUID userGuid)
    {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        // In side by side view, In First Time maybe user is null
        if (userGuid != null)
        {
            args.putSerializable(Constant.Param.KEY_CHAT_USER_GUID, userGuid);
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
        UUID userGuid = (UUID) getArguments().getSerializable(Constant.Param.KEY_CHAT_USER_GUID);

        if (userGuid != null)
        {
            mChatUser = Db.Teacher.selectByGuid(userGuid);
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
                mChatController.setChatUserImage(mChatUser.imageUrl);
                mChatController.readChat(mChatUser.getGuid());
            }
            catch (SamimException e)
            {

                if (activity != null)
                {
                    NhToast.makeText(activity, activity.getString(R.string.samim_message_service_not_responding), NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
                }
            }
        }

        else
        {
            try
            {
                mChatController.setChatUserImage(null);
            }
            catch (SamimException e)
            {
                e.printStackTrace();
            }
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
                    catch (SamimException e)
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
                    catch (SamimException e)
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
                    catch (SamimException e)
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
                catch (SamimException e)
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
                catch (SamimException e)
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
                catch (SamimException e)
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
                catch (SamimException e)
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
                        mVoiceRecorder = new SamimVoiceRecorder();
                        outputPath = FileManager.getDirectory(ChatContentType.Voice) + "/" + Helper.generateRandomString(64) + ".3gp";
                        mVoiceRecorder.record(outputPath);
                        //VoiceHelper.recordVoice(outputPath);
                        NhToast.makeText(getActivity(), "شروع ضبط صدا", NhToast.ToastIcon.INFO, NhToast.LENGTH_SHORT).show();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
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
