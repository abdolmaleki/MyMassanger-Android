package com.example.myapplication.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.database.Db;
import com.example.myapplication.database.model.ContactModel;
import com.example.myapplication.database.model.StudentModel;
import com.example.myapplication.holder.ChatUserHolder;
import com.example.myapplication.mapper.TeacherMapper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import ir.hfj.library.application.AppConfig;


public class ChatUserDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener
{

    private ChatUserAdapterList mAdapterList;
    private StudentModel mStudent;
    private List<ChatUserHolder> holders;
    private IChatUpdatable mChatController;

    public static ChatUserDialogFragment newInstance()
    {
        ChatUserDialogFragment fragment = new ChatUserDialogFragment();
        Bundle args = new Bundle();
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

    private boolean loadData()
    {
        return true;
    }

    private void initView()
    {

    }

    private void initAdapter()
    {

        List<ContactModel> models = Db.Teacher.selectAll();
        holders = TeacherMapper.ConvertModelToHolder(models);
        mAdapterList = new ChatUserAdapterList(getActivity(), holders);

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

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView;

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        rootView = inflater.inflate(R.layout.dialog_fragment_chat_user, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.fragment_chat_user_list);

        listView.setAdapter(mAdapterList);

        listView.setOnItemClickListener(this);

        return rootView;
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j)
    {
//        Intent intent = new Intent(getActivity(), ChatActivity.class);
//        intent.putExtra(Constant.Param.KEY_CHAT_USER_GUID, holders.get(i).guid);
//        intent.putExtra(Constant.Param.KEY_STUDENT_ID, mStudent.getId());
//        startActivity(intent);
        mChatController.switchChatUserFragment(holders.get(i).guid);
        getDialog().dismiss();
    }


    static class ViewHolder
    {

        public TextView name;
        public ImageView img;
    }


    private static class ChatUserAdapterList extends BaseAdapter
    {

        private Activity mContext;
        private final DisplayImageOptions imageOption;
        private List<ChatUserHolder> mChatUserHolders = new ArrayList<>();

        public ChatUserAdapterList(Activity context, List<ChatUserHolder> mChatUserHolders)
        {
            mContext = context;

            this.mChatUserHolders = mChatUserHolders;
            imageOption = AppConfig.createDisplayImageOptions();
        }

        @Override
        public int getCount()
        {
            return mChatUserHolders.size();
        }

        @Override
        public ChatUserHolder getItem(int position)
        {
            return mChatUserHolders.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View rowView = convertView;
            // reuse views
            if (rowView == null)
            {
                LayoutInflater inflater = mContext.getLayoutInflater();
                rowView = inflater.inflate(R.layout.item_list_chat_user, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.name = (TextView) rowView.findViewById(R.id.item_list_chat_user_name);
                viewHolder.img = (ImageView) rowView.findViewById(R.id.item_list_chat_user_imgprofile);

                rowView.setTag(viewHolder);

            }

            // fill data
            ViewHolder holder = (ViewHolder) rowView.getTag();

            ChatUserHolder chatUserHolder = mChatUserHolders.get(position);

            holder.name.setText(chatUserHolder.name);
            ImageLoader.getInstance().displayImage(chatUserHolder.imageUrl, holder.img, imageOption);

            return rowView;
        }
    }

}
