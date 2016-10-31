package com.example.myapplication.actionbar;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.hfj.library.actionbar.BaseActionBar;
import ir.hfj.library.application.AppConfig;

public class ChatActionBar extends BaseActionBar
{

    private final DisplayImageOptions mOptions;
    private CircleImageView uiChatUserImage;
    private TextView uiChatUserName;
    private TextView uiChatTypingState;

    public ChatActionBar(AppCompatActivity activity, ActionBar actionBar)
    {
        super(activity, actionBar, R.layout.actionbar_chat);
        mOptions = AppConfig.createDisplayImageOptions();
    }

    @Override
    protected int[] getItems()
    {
        return new int[0];
    }

    @Override
    protected void initView(View rootView)
    {
        uiChatUserImage = (CircleImageView) rootView.findViewById(R.id.fragment_chat_list_user_image);
        uiChatUserName = (TextView) rootView.findViewById(R.id.fragment_chat_list_contact_name);
        uiChatTypingState = (TextView) rootView.findViewById(R.id.fragment_chat_list_typing_state);

    }

    public void setChatUserName(String name)
    {
        if (uiChatUserName != null)
        {
            uiChatUserName.setText(name);
        }
    }

    public void setChatUserName(int res)
    {
        if (uiChatUserName != null)
        {
            uiChatUserName.setText(res);
        }
    }

    public void setChatUserImage(String imageUrl)
    {
        if (uiChatUserImage != null)
        {
            uiChatUserImage.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(imageUrl, uiChatUserImage, mOptions);
        }
    }

    public void setChatUserImage(int res)
    {
        if (uiChatUserImage != null)
        {
            uiChatUserImage.setVisibility(View.VISIBLE);
            uiChatUserImage.setImageResource(res);
        }
    }

    public void setUserImageInvisible()
    {
        uiChatUserImage.setVisibility(View.GONE);
    }

    public void setIsTyping(boolean isTyping)
    {
        MyRunnable runnable;

        if (uiChatTypingState != null)
        {
            if (isTyping)
            {
                uiChatTypingState.setText(R.string.messanger_chat_message_isTyping);
                runnable = new MyRunnable();
                uiChatTypingState.postDelayed(runnable, 8000);
            }

            else
            {
                uiChatTypingState.setText("");
            }
        }
    }

    public static int COMMAND_STEP = 0;

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
                uiChatTypingState.setText("");
            }
        }

    }


    @Override
    protected void onItemsClick(View v)
    {

    }
}
