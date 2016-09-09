package com.example.myapplication.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.application.Constant;
import com.example.myapplication.database.Db;
import com.example.myapplication.database.model.ChatModel;
import com.example.myapplication.database.model.StudentModel;
import com.example.myapplication.database.model.TeacherModel;
import com.example.myapplication.dictionary.DataDictionary;
import com.example.myapplication.holder.ChatHistoryHolder;
import com.example.myapplication.mapper.ChatMapper;
import com.melnykov.fab.FloatingActionButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
import java.util.UUID;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.ui.SimpleDividerItemDecoration;


public class ChatHistoryFragment extends Fragment implements View.OnClickListener, IChatHistoryFragment
{

    private RecyclerView uiRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ChatHistoryAdapter mAdapter;
    private StudentModel mStudent;
    private IChatUpdatable mChatController;

    public static ChatHistoryFragment newInstance(long studentId)
    {
        ChatHistoryFragment fragment = new ChatHistoryFragment();
        Bundle args = new Bundle();
        args.putLong(Constant.Param.KEY_STUDENT_ID, studentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadData();

        if (mStudent == null)
        {
            return;
        }

        initView();

        initAdapter();

    }

    private void loadData()
    {
        long currentStudentId = getArguments().getLong(Constant.Param.KEY_STUDENT_ID, -1);

        if (currentStudentId != -1)
        {
            mStudent = Db.Student.select(currentStudentId);
        }
    }

    private void initView()
    {
        //setRetainInstance(true);
    }

    private void initAdapter()
    {
        List<TeacherModel> teacherModels = Db.Teacher.selectByStudentId(mStudent.getId());
        DataDictionary<UUID, ChatHistoryHolder> holders = new DataDictionary<>();

        for (TeacherModel model : teacherModels)
        {

            ChatHistoryHolder holder = ChatMapper.getChatHistory(model.getGuid());

            if (holder != null)
            {
                holders.add(model.getGuid(), holder);
            }

        }

        mAdapter = new ChatHistoryAdapter(getActivity(), holders);

        if (uiRecyclerView != null)
        {
            uiRecyclerView.setAdapter(mAdapter);
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView;

        rootView = inflater.inflate(R.layout.fragment_chat_history, container, false);
        mLayoutManager = new LinearLayoutManager(getActivity());
        uiRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_chat_history_recycleview);

        uiRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fragment_chat_history_newchat);
        fab.attachToRecyclerView(uiRecyclerView);
        fab.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        initAdapter();

        mAdapter.setOnItemClickListener(new ChatHistoryAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                UUID userGuid = Db.Teacher.selectById(mAdapter.getItemId(position));
                mChatController.switchChatUserFragment(userGuid);
                mAdapter.updateReadedMessage(position);
            }
        });

    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        if (id == R.id.fragment_chat_history_newchat)
        {
            ChatUserDialogFragment chatUserDialogFragment = ChatUserDialogFragment.newInstance(mStudent.getId());
            chatUserDialogFragment.show(getActivity().getFragmentManager(), null);
        }
    }

    @Override
    public void updateChatHistory(UUID contactGuid)
    {

        ///////////////////////////////////////////////////////////
        /// Firsts check is exist history for this user. if yes then update else add new one
        ///////////////////////////////////////////////////////////

        ChatHistoryHolder holder = getChatHistoryByGuid(contactGuid);

        if (holder != null)
        {
            ChatModel lastChatModel = Db.Chat.selectHistory(contactGuid);
            holder.description = lastChatModel.getSummary();
            holder.count = Db.Chat.selectUnreadHistoryCount(contactGuid);
            mAdapter.update(holder);
        }
        else
        {
            holder = ChatMapper.getChatHistory(contactGuid);
            mAdapter.add(holder);
        }
    }

    @Override
    public void setCurrentContact(UUID contactGuid)
    {
        mAdapter.setSelectedContact(contactGuid);
    }

    @Override
    public ChatHistoryHolder getChatHistoryByGuid(UUID contactGuid)
    {
        return mAdapter.getByGuid(contactGuid);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        if (activity instanceof IChatUpdatable)
        {
            mChatController = (IChatUpdatable) activity;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        uiRecyclerView.setLayoutManager(mLayoutManager);
        uiRecyclerView.setAdapter(mAdapter);
    }


    private static class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder>
    {

        OnItemClickListener mItemClickListener;
        private final DisplayImageOptions imageOption;
        private Activity mContext;
        private int mSelectedPosition = -1;
        private DataDictionary<UUID, ChatHistoryHolder> mItems;

        public void setSelectedContact(UUID contactGuid)
        {
            if (contactGuid == null)
            {
                mSelectedPosition = -1;
                return;
            }

            ChatHistoryHolder holder = mItems.getByKey(contactGuid);
            if (holder != null)
            {
                int oldPosition = mSelectedPosition;
                mSelectedPosition = mItems.getIndexByKey(contactGuid);
                notifyItemChanged(oldPosition);
                notifyItemChanged(mSelectedPosition);
            }

        }


        class ChatHistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
        {

            public TextView name;
            public TextView description;
            public TextView time;
            public TextView count;
            public ImageView img;
            public RelativeLayout background;

            public ChatHistoryViewHolder(View itemView)
            {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.item_list_chat_history_txttitle);
                description = (TextView) itemView.findViewById(R.id.item_list_chat_history_txtdes);
                time = (TextView) itemView.findViewById(R.id.item_list_chat_history_time);
                count = (TextView) itemView.findViewById(R.id.item_list_chat_history_newmessage);
                img = (ImageView) itemView.findViewById(R.id.item_list_chat_history_imgprofile);
                background = (RelativeLayout) itemView.findViewById(R.id.item_list_chat_history_background);
                itemView.setOnClickListener(this);
            }
            @Override
            public void onClick(View view)
            {
                if (mItemClickListener != null)
                {
                    mItemClickListener.onItemClick(view, getPosition());
                }
            }
        }

        public interface OnItemClickListener
        {

            void onItemClick(View view, int position);
        }

        public void setOnItemClickListener(final OnItemClickListener mItemClickListener)
        {
            this.mItemClickListener = mItemClickListener;
        }

        public ChatHistoryAdapter(Activity activity, DataDictionary<UUID, ChatHistoryHolder> items)
        {
            mContext = activity;
            imageOption = AppConfig.createDisplayImageOptions();
            mItems = items;
        }

        @Override
        public int getItemViewType(int position)
        {
            return R.layout.item_list_chat_history;
        }

        @Override
        public long getItemId(int position)
        {
            return mItems.get(position).id;
        }

        @Override
        public ChatHistoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
        {
            LayoutInflater inflater = mContext.getLayoutInflater();
            View itemView = inflater.inflate(viewType, viewGroup, false);
            return new ChatHistoryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ChatHistoryViewHolder viewHolder, int position)
        {
            ChatHistoryHolder holder = mItems.get(position);

            viewHolder.background.setBackgroundResource((mSelectedPosition == position) ?
                                                                R.drawable.selector_item_gray :
                                                                R.drawable.selector_item_gray_white);

            viewHolder.name.setText(holder.name);
            if (holder.count == 0)
            {
                viewHolder.count.setVisibility(View.GONE);
            }
            else
            {
                viewHolder.count.setVisibility(View.VISIBLE);
                viewHolder.count.setText(String.valueOf(holder.count));
            }
            viewHolder.description.setText(holder.description);
            viewHolder.time.setText(holder.time);

            ImageLoader.getInstance().displayImage(holder.img, viewHolder.img, imageOption);

        }
        @Override
        public int getItemCount()
        {
            return mItems.size();
        }

        public void add(ChatHistoryHolder holder)
        {
            mItems.add(holder.contactGuid, holder);
            notifyItemInserted(getItemCount());
        }

        public void update(ChatHistoryHolder holder)
        {
            mItems.update(holder.contactGuid, holder);
            notifyItemChanged(mItems.getIndexByKey(holder.contactGuid));
        }

        public void updateReadedMessage(int position)
        {
            mItems.get(position).count = 0;
            notifyItemChanged(position);
        }

        public ChatHistoryHolder getByGuid(UUID contactGuid)
        {
            return mItems.getByKey(contactGuid);
        }
    }

}
