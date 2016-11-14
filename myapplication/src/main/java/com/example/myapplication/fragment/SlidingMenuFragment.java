package com.example.myapplication.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activity.ChatActivity;
import com.example.myapplication.adapter.SlidingMenuRecyclerViewAdapter;
import com.example.myapplication.database.Db;
import com.example.myapplication.entity.SlidingMenuItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.database.DbBase;

public class SlidingMenuFragment extends Fragment
{

    RecyclerView recyclerView;
    private DisplayImageOptions mOptions;
    TextView mUserName;
    ImageView mUserAvatar;


    public SlidingMenuFragment()
    {
        // Empty constructor required
    }

    public static SlidingMenuFragment newInstance()
    {
        return new SlidingMenuFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_sliding_menu, parent, false);

        mUserAvatar = (ImageView) rootView.findViewById(R.id.fragment_sliding_menu_avatar);
        mUserName = (TextView) rootView.findViewById(R.id.fragment_sliding_menu_name);


        ImageLoader.getInstance().displayImage(DbBase.UserSetting.select().imageUrl, mUserAvatar, mOptions);
        mUserName.setText(Db.UserSetting.select().name + " " + Db.UserSetting.select().family);

        mUserAvatar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getContext(), "User Avatar Clicked", Toast.LENGTH_SHORT).show();
                ((ChatActivity) getActivity()).toggleMenu();
            }
        });

        mOptions = AppConfig.createDisplayImageOptions();


        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_sliding_menu_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SlidingMenuRecyclerViewAdapter menuAdapter = new SlidingMenuRecyclerViewAdapter(getData(), getContext());
        recyclerView.setAdapter(menuAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener()
        {
            @Override
            public void onClick(View view, int position)
            {
                Toast.makeText(getContext(), "Item Num: " + position + " Clicked", Toast.LENGTH_SHORT).show();

                ((ChatActivity) getActivity()).toggleMenu();
            }

            @Override
            public void onLongClick(View view, int position)
            {
                Toast.makeText(getContext(), "Item Num: " + position + " Clicked", Toast.LENGTH_SHORT).show();
            }
        }));

        return rootView;
    }

    private List<SlidingMenuItem> getData()
    {
        List<SlidingMenuItem> menuList = new ArrayList<>();
        menuList.add(new SlidingMenuItem(getString(R.string.messanger_menu_contact), R.drawable.ic_contact));
        menuList.add(new SlidingMenuItem(getString(R.string.messanger_menu_newchannel), R.drawable.ic_channel));
        menuList.add(new SlidingMenuItem(getString(R.string.messanger_menu_edit_profileImage), R.drawable.ic_edit_avatar));
        menuList.add(new SlidingMenuItem(getString(R.string.messanger_menu_about_us), R.drawable.ic_about));

        return menuList;
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener
    {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener)
        {

            this.clickListener = clickListener;

            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener()
            {
                @Override
                public boolean onSingleTapUp(MotionEvent e)
                {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e)
                {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null)
                    {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e)
        {
            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e))
            {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e)
        {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept)
        {

        }
    }

    public interface ClickListener
    {

        public void onClick(View view, int position);

        public void onLongClick(View view, int position);
    }
}
