package com.example.myapplication.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.pnikosis.materialishprogress.ProgressWheel;


public class BaseChatViewHolder extends RecyclerView.ViewHolder
{

    public TextView txvMessage;
    public TextView txvDatetime;
    public TextView txvIcon;
    public TextView txvFileLabel;
    public ImageView imgAuthor;
    public ImageView imgImage;
    //public TextView txvImageComment;
    public ProgressWheel prgProgress;
    public ImageView btnStartProgressIcon;
    //public ImageView imgFileIcon;
    public TextView txvFileSize;
    public ImageButton btnVoicePlayer;

    public BaseChatViewHolder(View itemView)
    {
        super(itemView);

        txvMessage = (TextView) itemView.findViewById(R.id.item_chat_txv_message);
        imgAuthor = (ImageView) itemView.findViewById(R.id.item_chat_img_user);
        txvDatetime = (TextView) itemView.findViewById(R.id.item_chat_txv_datetime);
        txvIcon = (TextView) itemView.findViewById(R.id.item_chat_txv_icon);
        imgImage = (ImageView) itemView.findViewById(R.id.item_chat_img_view);
        //txvImageComment = (TextView) itemView.findViewById(R.id.item_chat_img_comment);
        prgProgress = (ProgressWheel) itemView.findViewById(R.id.item_chat_prg);
        if(prgProgress != null)
        {
            prgProgress.setLinearProgress(true);
        }
        btnStartProgressIcon = (ImageView) itemView.findViewById(R.id.item_chat_btn_download);
        //imgFileIcon = (ImageView) itemView.findViewById(R.id.item_chat_file_img_icon);
        txvFileLabel = (TextView) itemView.findViewById(R.id.item_chat_txv_label);
        txvFileSize = (TextView) itemView.findViewById(R.id.item_chat_txv_size);
        btnVoicePlayer =(ImageButton) itemView.findViewById(R.id.item_chat_player_btn_play);
    }
}