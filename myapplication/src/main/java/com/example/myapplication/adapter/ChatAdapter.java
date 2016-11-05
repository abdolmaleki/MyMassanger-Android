package com.example.myapplication.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.myapplication.R;
import com.example.myapplication.database.model.ChatModel;
import com.example.myapplication.database.model.ContactModel;
import com.example.myapplication.dictionary.DataDictionary;
import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.entity.MediaTransferState;
import com.example.myapplication.factory.FileManager;
import com.example.myapplication.fragment.IDownloadController;
import com.example.myapplication.fragment.IDownloadMediaListener;
import com.example.myapplication.fragment.IUploadController;
import com.example.myapplication.fragment.IUploadMediaListener;
import com.example.myapplication.holder.ChatFileContentHolder;
import com.example.myapplication.holder.ChatHolder;
import com.example.myapplication.holder.ChatImageContentHolder;
import com.example.myapplication.holder.ChatTextContentHolder;
import com.example.myapplication.holder.ChatVideoContentHolder;
import com.example.myapplication.holder.ChatVoiceContentHolder;
import com.example.myapplication.holder.DownloadHolder;
import com.example.myapplication.holder.UploadHolder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.UUID;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.exception.MyMessangerException;
import ir.hfj.library.util.Helper;
import ir.hfj.library.util.MessangerMediaPlayer;
import ir.hfj.library.util.VideoHelper;


public class ChatAdapter extends RecyclerView.Adapter<BaseChatViewHolder> implements IDownloadMediaListener, IUploadMediaListener
{

    private DataDictionary<UUID, ChatHolder> mItems = new DataDictionary<>();
    private Activity mContext;
    private IDownloadController mDownloader;
    private IUploadController mUploader;
    private final DisplayImageOptions imageOptionForUser;
    private final DisplayImageOptions imageOptionForContent;
    private ContactModel mChatUser;
    private MessangerMediaPlayer mSamimMediaPlayer;

    public ChatAdapter(Activity activity, DataDictionary<UUID, ChatHolder> items, ContactModel mChatUser)
    {
        mContext = activity;
        if (activity instanceof IDownloadController)
        {
            mDownloader = (IDownloadController) activity;
        }
        else
        {
            mDownloader = null;
        }

        if (activity instanceof IUploadController)
        {
            mUploader = (IUploadController) activity;
        }
        else
        {
            mUploader = null;
        }


        mItems = items;
        this.mChatUser = mChatUser;

        imageOptionForUser = AppConfig.createDisplayImageOptions();
        imageOptionForContent = AppConfig.createChatContentDisplayImageOptions();
        mSamimMediaPlayer = new MessangerMediaPlayer();
    }

    public void update(ChatHolder holder)
    {
        mItems.update(holder.chatId, holder);
        notifyItemChanged(mItems.getIndexByKey(holder.chatId));
    }

    public void add(ChatHolder holder)
    {
        mItems.add(holder.chatId, holder);
        notifyItemInserted(getItemCount());
    }

    public void replace(UUID chatId, ChatHolder holder)
    {
        mItems.replace(chatId, holder.chatId, holder);
        notifyItemChanged(mItems.getIndexByKey(holder.chatId));
    }

    @Override
    public int getItemViewType(int position)
    {
        ChatHolder holder = mItems.get(position);
        int layout;

        if (holder.isMyMessage())
        {
            if (holder.contentType == ChatContentType.Image)
            {
                layout = R.layout.item_chat_image_out;
            }
            else if (holder.contentType == ChatContentType.File)
            {
                layout = R.layout.item_chat_file_out;
            }
            else if (holder.contentType == ChatContentType.Voice)
            {
                layout = R.layout.item_chat_voice_out;
            }
            else if (holder.contentType == ChatContentType.Video)
            {
                layout = R.layout.item_chat_video_out;
            }
            else
            {
                layout = R.layout.item_chat_out;
            }
        }

        else
        {
            if (holder.contentType == ChatContentType.Image)
            {
                layout = R.layout.item_chat_image_in;
            }
            else if (holder.contentType == ChatContentType.File)
            {
                layout = R.layout.item_chat_file_in;
            }
            else if (holder.contentType == ChatContentType.Voice)
            {
                layout = R.layout.item_chat_voice_in;
            }
            else if (holder.contentType == ChatContentType.Video)
            {
                layout = R.layout.item_chat_video_in;
            }
            else
            {
                layout = R.layout.item_chat_in;
            }

        }
        return layout;
    }

    @Override
    public BaseChatViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View itemView = inflater.inflate(viewType, viewGroup, false);
        return new BaseChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BaseChatViewHolder viewHolder, int position)
    {
        final int itemPosition = position;
        final ChatHolder baseChatHolder = mItems.get(position);
        viewHolder.txvDatetime.setText(baseChatHolder.date + "  " + baseChatHolder.time);

        if (baseChatHolder.isMyMessage())
        {
            viewHolder.imgAuthor.setVisibility(View.GONE);
            //     viewHolder.txvState.setText(baseChatHolder.alertMessage);

            switch (baseChatHolder.state)
            {
                case ChatModel.SENDER_STATE_SENDING:
                    viewHolder.txvIcon.setText(R.string.icon_material_clock100);
                    break;

                case ChatModel.SENDER_STATE_SENDED:
                    viewHolder.txvIcon.setText(R.string.icon_material_check52);
                    break;

                case ChatModel.SENDER_STATE_DELIVER:
                    viewHolder.txvIcon.setText(R.string.icon_material_tick7);
                    break;

                case ChatModel.SENDER_STATE_SEEN:
                    viewHolder.txvIcon.setText(R.string.icon_material_double126);
                    break;
            }
        }

        else
        {
            viewHolder.imgAuthor.setVisibility(View.VISIBLE);
            //viewHolder.txvState.setVisibility(View.GONE);
            ImageLoader.getInstance().displayImage(mChatUser.imageUrl, viewHolder.imgAuthor, imageOptionForUser);
        }


        if (baseChatHolder.contentType == ChatContentType.Text)
        {

            //   _____         _       ____            _             _
            //  |_   _|____  _| |_    / ___|___  _ __ | |_ ___ _ __ | |_
            //    | |/ _ \ \/ / __|  | |   / _ \| '_ \| __/ _ \ '_ \| __|
            //    | |  __/>  <| |_   | |__| (_) | | | | ||  __/ | | | |_
            //    |_|\___/_/\_\\__|   \____\___/|_| |_|\__\___|_| |_|\__|
            //


            bindContentAfterDownload(viewHolder, baseChatHolder);


        }
        else// if (baseChatHolder.contentType == ChatContentType.Image)
        {


            bindContentInitUpload(viewHolder, baseChatHolder);


            /////////////////////////////
            ///   Upload File
            /////////////////////////////

            if (baseChatHolder.needUpload)
            {

                if (FileManager.exist(baseChatHolder.path))
                {
                    //show original image from sdcard

                    bindContentBeforeUpload(viewHolder, baseChatHolder);

                    if (baseChatHolder.content.fileToken == null || baseChatHolder.content.fileToken.isEmpty())
                    {
                        viewHolder.prgProgress.setVisibility(View.VISIBLE);

                        if (baseChatHolder.progress >= 0)
                        {
                            setProgress(viewHolder.prgProgress, baseChatHolder.progress);
                        }
                        else
                        {
                            //waiting..... for service ....
                            setProgress(viewHolder.prgProgress, -1);
                        }

                    }
                    else
                    {
                        //uploaded and OK!
                        viewHolder.prgProgress.setVisibility(View.GONE);
                        setProgress(viewHolder.prgProgress, 0);

                        bindContentAfterUpload(viewHolder, baseChatHolder);

                    }
                }
                else if (baseChatHolder.content.fileToken != null && !baseChatHolder.content.fileToken.isEmpty())
                {

                    //show thumbnail
                    bindThumbnailContent(viewHolder, baseChatHolder);


                    //show download ui
                    viewHolder.prgProgress.setVisibility(View.GONE);
                    viewHolder.btnStartProgressIcon.setVisibility(View.VISIBLE);
                    viewHolder.btnStartProgressIcon.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                            if (mDownloader != null)
                            {
                                DownloadHolder.Send send = new DownloadHolder.Send();
                                send.type = baseChatHolder.contentType;
                                send.extension = baseChatHolder.content.extension;
                                send.guid = baseChatHolder.chatId;
                                send.title = "Hello";
                                send.token = baseChatHolder.content.fileToken;

                                try
                                {
                                    mDownloader.download(send);

                                    v.setVisibility(View.GONE);
                                    viewHolder.prgProgress.setVisibility(View.VISIBLE);
                                    setProgress(viewHolder.prgProgress, -1);
                                }
                                catch (MyMessangerException e)
                                {
                                    v.setVisibility(View.VISIBLE);
                                    viewHolder.prgProgress.setVisibility(View.GONE);
                                    setProgress(viewHolder.prgProgress, 0);
                                }

                            }

                        }
                    });


                }


            }

            else
            {
                /////////////////////////////
                ///   Download File
                /////////////////////////////

                if (FileManager.exist(baseChatHolder.path))
                {
                    //show original image from sdcard
                    bindContentAfterDownload(viewHolder, baseChatHolder);
                }
                else if (baseChatHolder.progress >= 0)
                {
                    setProgress(viewHolder.prgProgress, baseChatHolder.progress);
                }
                else if (baseChatHolder.content.fileToken != null && !baseChatHolder.content.fileToken.isEmpty())
                {

                    //show thumbnail
                    bindThumbnailContent(viewHolder, baseChatHolder);

                    //show download ui
                    viewHolder.prgProgress.setVisibility(View.GONE);
                    viewHolder.btnStartProgressIcon.setVisibility(View.VISIBLE);
                    viewHolder.btnStartProgressIcon.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                            if (mDownloader != null)
                            {
                                DownloadHolder.Send send = new DownloadHolder.Send();
                                send.type = baseChatHolder.contentType;
                                send.extension = baseChatHolder.content.extension;
                                send.guid = baseChatHolder.chatId;
                                send.title = "Hello";
                                send.token = baseChatHolder.content.fileToken;

                                try
                                {
                                    mDownloader.download(send);

                                    v.setVisibility(View.GONE);
                                    viewHolder.prgProgress.setVisibility(View.VISIBLE);
                                    setProgress(viewHolder.prgProgress, -1);
                                }
                                catch (MyMessangerException e)
                                {
                                    v.setVisibility(View.VISIBLE);
                                    viewHolder.prgProgress.setVisibility(View.GONE);
                                    setProgress(viewHolder.prgProgress, -1);
                                }


                            }


                        }
                    });


                }

            }

        }


    }


    private void bindContentInitUpload(BaseChatViewHolder viewHolder, ChatHolder chatHolder)
    {
        if (chatHolder.contentType == ChatContentType.Text)
        {

        }
        else if (chatHolder.contentType == ChatContentType.Image)
        {
            viewHolder.prgProgress.setVisibility(View.GONE);
            viewHolder.btnStartProgressIcon.setVisibility(View.GONE);

            if (((ChatImageContentHolder) chatHolder.content).comment == null || ((ChatImageContentHolder) chatHolder.content).comment.isEmpty())
            {
                viewHolder.txvMessage.setVisibility(View.GONE);
            }
            else
            {
                viewHolder.txvMessage.setText(((ChatImageContentHolder) chatHolder.content).comment);
            }

        }
        else if (chatHolder.contentType == ChatContentType.Video)
        {
            viewHolder.prgProgress.setVisibility(View.GONE);
            viewHolder.btnStartProgressIcon.setVisibility(View.GONE);

            if (((ChatVideoContentHolder) chatHolder.content).comment == null || ((ChatVideoContentHolder) chatHolder.content).comment.isEmpty())
            {
                viewHolder.txvMessage.setVisibility(View.GONE);
            }
            else
            {
                viewHolder.txvMessage.setText(((ChatVideoContentHolder) chatHolder.content).comment);
            }
        }
        else if (chatHolder.contentType == ChatContentType.Voice)
        {
            viewHolder.prgProgress.setVisibility(View.GONE);
            viewHolder.btnStartProgressIcon.setVisibility(View.GONE);
            viewHolder.btnVoicePlayer.setVisibility(View.GONE);

            viewHolder.txvFileLabel.setText(((ChatVoiceContentHolder) chatHolder.content).duration);
            //viewHolder.txvFileSize.setText(((ChatVoiceContentHolder) chatHolder.content).size);
            viewHolder.txvFileLabel.setText("1 دقیقه");
            viewHolder.txvFileSize.setText(Helper.getFileSize(chatHolder.path) + "   " + chatHolder.content.extension);

        }
        else if (chatHolder.contentType == ChatContentType.File)
        {
            viewHolder.prgProgress.setVisibility(View.GONE);
            viewHolder.btnStartProgressIcon.setVisibility(View.GONE);
            viewHolder.imgImage.setVisibility(View.GONE);

            viewHolder.txvFileLabel.setText(((ChatFileContentHolder) chatHolder.content).name);
            ///viewHolder.txvFileSize.setText(((ChatFileContentHolder) chatHolder.content).size);
            ///viewHolder.txvFileSize.setText("102KB " + chatHolder.content.extension);
            viewHolder.txvFileSize.setText(Helper.getFileSize(chatHolder.path) + "   " + chatHolder.content.extension);
        }

    }

    private void bindThumbnailContent(final BaseChatViewHolder viewHolder, final ChatHolder chatHolder)
    {

        if (chatHolder.contentType == ChatContentType.Image)
        {
            ImageLoader.getInstance().displayImage(chatHolder.content.getThumbnailUrl(), viewHolder.imgImage, imageOptionForContent, new SimpleImageLoadingListener()
            {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
                {
                    setImageFit(viewHolder.imgImage, chatHolder.contentType);
                }
            });

        }
        else if (chatHolder.contentType == ChatContentType.Video)
        {
            ImageLoader.getInstance().displayImage(chatHolder.content.getThumbnailUrl(), viewHolder.imgImage, imageOptionForContent, new SimpleImageLoadingListener()
            {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
                {
                    setImageFit(viewHolder.imgImage, chatHolder.contentType);
                }
            });
        }
        else if (chatHolder.contentType == ChatContentType.File)
        {
            /*if(chatHolder.content.extension.equalsIgnoreCase("pdf"))
            {
                //set pdf icon
            }*/

            //Get from server dynamic, use for image, video, etc
            //ImageLoader.getInstance().displayImage(chatHolder.content.getThumbnailUrl(), viewHolder.imgImage, imageOption);
            viewHolder.imgImage.setVisibility(View.VISIBLE);
            viewHolder.imgImage.setOnClickListener(null);
            viewHolder.imgImage.setImageResource(R.drawable.ic_attachment);

        }

    }

    private void bindContentBeforeUpload(final BaseChatViewHolder viewHolder, final ChatHolder chatHolder)
    {

        if (chatHolder.contentType == ChatContentType.Text)
        {

        }
        else if (chatHolder.contentType == ChatContentType.Image)
        {
            viewHolder.imgImage.setImageBitmap(FileManager.loadImage(chatHolder.path));
            setImageFit(viewHolder.imgImage, chatHolder.contentType);

            if (((ChatImageContentHolder) chatHolder.content).comment == null || ((ChatImageContentHolder) chatHolder.content).comment.isEmpty())
            {
                viewHolder.txvMessage.setVisibility(View.GONE);
            }
            else
            {
                viewHolder.txvMessage.setText(((ChatImageContentHolder) chatHolder.content).comment);
            }
        }
        else if (chatHolder.contentType == ChatContentType.Video)
        {
            ImageLoader.getInstance().displayImage(chatHolder.content.getThumbnailUrl(), viewHolder.imgImage, imageOptionForContent, new SimpleImageLoadingListener()
            {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
                {
                    setImageFit(viewHolder.imgImage, chatHolder.contentType);
                }
            });

            if (((ChatVideoContentHolder) chatHolder.content).comment == null || ((ChatVideoContentHolder) chatHolder.content).comment.isEmpty())
            {
                viewHolder.txvMessage.setVisibility(View.GONE);
            }
            else
            {
                viewHolder.txvMessage.setText(((ChatVideoContentHolder) chatHolder.content).comment);
            }

            viewHolder.imgImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    VideoHelper.openVideo(mContext, chatHolder.path);
                }
            });
        }
        else if (chatHolder.contentType == ChatContentType.File)
        {
            viewHolder.imgImage.setImageResource(R.drawable.ic_attachment);
            viewHolder.imgImage.setVisibility(View.VISIBLE);
            viewHolder.imgImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Helper.openFileIntent(mContext, chatHolder.path, chatHolder.content.extension);
                }
            });
        }

    }

    private void bindContentAfterUpload(final BaseChatViewHolder viewHolder, final ChatHolder chatHolder)
    {

        if (chatHolder.contentType == ChatContentType.Text)
        {

        }
        else if (chatHolder.contentType == ChatContentType.Voice)
        {
            viewHolder.btnVoicePlayer.setVisibility(View.VISIBLE);

            final ChatVoiceContentHolder contentHolder = (ChatVoiceContentHolder) chatHolder.content;

            if (((ChatVoiceContentHolder) chatHolder.content).isPlaying)
            {
                viewHolder.btnVoicePlayer.setImageResource(R.drawable.selector_player_pause);
            }
            else
            {
                viewHolder.btnVoicePlayer.setImageResource(R.drawable.selector_player_play);

            }

            viewHolder.btnVoicePlayer.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    if (contentHolder.isPlaying)
                    {
                        mSamimMediaPlayer.stop();
                        contentHolder.isPlaying = false;
                        viewHolder.btnVoicePlayer.setImageResource(R.drawable.selector_player_play);
                    }
                    else
                    {
                        mSamimMediaPlayer.play(chatHolder.path, new MediaPlayer.OnCompletionListener()
                        {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer)
                            {
                                contentHolder.isPlaying = false;
                                viewHolder.btnVoicePlayer.setImageResource(R.drawable.selector_player_play);
                                mSamimMediaPlayer.stop();
                            }
                        });
                        contentHolder.isPlaying = true;
                        viewHolder.btnVoicePlayer.setImageResource(R.drawable.selector_player_pause);
                    }

                   // notifyDataSetChanged();
                }
            });
        }
        else if (chatHolder.contentType == ChatContentType.File)
        {

            /*if(chatHolder.content.extension.equalsIgnoreCase("pdf"))
            {
                //set pdf icon
            }*/

            //Get from server dynamic, use for image, video, etc
            //ImageLoader.getInstance().displayImage(chatHolder.content.getThumbnailUrl(), viewHolder.imgImage, imageOption);
            //else
            viewHolder.imgImage.setImageResource(R.drawable.ic_attachment);

            viewHolder.imgImage.setVisibility(View.VISIBLE);
            viewHolder.imgImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Helper.openFileIntent(mContext, chatHolder.path, chatHolder.content.extension);
                }
            });
        }


    }

    private void bindContentAfterDownload(final BaseChatViewHolder viewHolder, final ChatHolder chatHolder)
    {
        if (chatHolder.contentType == ChatContentType.Text)
        {
            viewHolder.txvMessage.setText(((ChatTextContentHolder) chatHolder.content).text);
        }
        else if (chatHolder.contentType == ChatContentType.File)
        {
            viewHolder.prgProgress.setVisibility(View.GONE);
            viewHolder.btnStartProgressIcon.setVisibility(View.GONE);

            viewHolder.txvFileSize.setText(((ChatFileContentHolder) chatHolder.content).size);
            //viewHolder.txvFileSize.setText("102KB " + chatHolder.content.extension);
            viewHolder.txvFileSize.setText(Helper.getFileSize(chatHolder.path) + "   " + chatHolder.content.extension);

            viewHolder.imgImage.setVisibility(View.VISIBLE);
            viewHolder.imgImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Helper.openFileIntent(mContext, chatHolder.path, chatHolder.content.extension);
                }
            });

            /*if(chatHolder.content.extension.equalsIgnoreCase("pdf"))
            {
                //set pdf icon
            }*/

            //Get from server dynamic, use for image, video, etc
            //ImageLoader.getInstance().displayImage(chatHolder.content.getThumbnailUrl(), viewHolder.imgImage, imageOption);

            viewHolder.imgImage.setImageResource(R.drawable.ic_attachment);

        }
        else if (chatHolder.contentType == ChatContentType.Voice)
        {
            viewHolder.prgProgress.setVisibility(View.GONE);
            viewHolder.btnStartProgressIcon.setVisibility(View.GONE);
            viewHolder.btnVoicePlayer.setVisibility(View.VISIBLE);

            final ChatVoiceContentHolder contentHolder = (ChatVoiceContentHolder) chatHolder.content;

            if (contentHolder.isPlaying)
            {
                viewHolder.btnVoicePlayer.setImageResource(R.drawable.selector_player_pause);
            }
            else
            {
                viewHolder.btnVoicePlayer.setImageResource(R.drawable.selector_player_play);

            }

            viewHolder.btnVoicePlayer.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (mSamimMediaPlayer.isPlaying())
                    {
                        mSamimMediaPlayer.stop();
                        contentHolder.isPlaying = false;
                        viewHolder.btnVoicePlayer.setImageResource(R.drawable.selector_player_play);
                    }
                    else
                    {

                        mSamimMediaPlayer.play(chatHolder.path, new MediaPlayer.OnCompletionListener()
                        {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer)
                            {
                                contentHolder.isPlaying = false;
                                viewHolder.btnVoicePlayer.setImageResource(R.drawable.selector_player_play);
                                mSamimMediaPlayer.stop();
                            }
                        });

                        contentHolder.isPlaying = true;
                        viewHolder.btnVoicePlayer.setImageResource(R.drawable.selector_player_pause);
                    }

                    notifyDataSetChanged();
                }
            });
        }
        else if (chatHolder.contentType == ChatContentType.Image)
        {
            viewHolder.imgImage.setImageBitmap(FileManager.loadImage(chatHolder.path));
            setImageFit(viewHolder.imgImage, chatHolder.contentType);

            viewHolder.prgProgress.setVisibility(View.GONE);
            viewHolder.btnStartProgressIcon.setVisibility(View.GONE);
        }
        else if (chatHolder.contentType == ChatContentType.Video)
        {
            ImageLoader.getInstance().displayImage(chatHolder.content.getThumbnailUrl(), viewHolder.imgImage, imageOptionForContent, new SimpleImageLoadingListener()
            {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
                {
                    setImageFit(viewHolder.imgImage, chatHolder.contentType);
                }
            });

            viewHolder.prgProgress.setVisibility(View.GONE);
            viewHolder.btnStartProgressIcon.setVisibility(View.GONE);

            viewHolder.imgImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    VideoHelper.openVideo(mContext, chatHolder.path);
                }
            });
        }

    }


    public void setImageFit(ImageView imageView, ChatContentType type)
    {

        int maxSize;// = 300;//dp
        if (type == ChatContentType.Video)
        {
            maxSize = Helper.getDpi(mContext, 100);
        }
        else
        {
            maxSize = Helper.getDpi(mContext, 200);
        }


        int w = 0;
        int h = 0;

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if (drawable != null)
        {
            Bitmap bitmap = drawable.getBitmap();
            if (bitmap != null)
            {
                w = bitmap.getWidth();
                h = bitmap.getHeight();
            }
        }

        if (w <= 0 || h <= 0)
        {
            w = Helper.getDpi(mContext, 100);
            h = Helper.getDpi(mContext, 100);
        }


        FrameLayout.LayoutParams layoutParams;
        if (w > h)
        {
            layoutParams = new FrameLayout.LayoutParams(maxSize, maxSize * h / w, Gravity.CENTER);
        }
        else
        {
            layoutParams = new FrameLayout.LayoutParams(maxSize, maxSize * h / w, Gravity.CENTER);
        }

        imageView.setLayoutParams(layoutParams);

    }


    private void setProgress(ProgressWheel p, int progress)
    {
        if (progress < 0)
        {
            p.setSpinSpeed(AppConfig.SPIN_SPEED);
            p.spin();
        }
        else
        {
            p.setSpinSpeed(Float.MAX_VALUE);
            p.setProgress(progress / 100.0f);
        }
    }

    @Override
    public int getItemCount()
    {
        return mItems.size();
    }


    @Override
    public void onDownloadChangeState(DownloadHolder.Received holder)
    {
        //Toast.makeText(mContext, holder.progress + "%", Toast.LENGTH_SHORT).show();

        ChatHolder chatHolder = mItems.getByKey(holder.guid);

        if (chatHolder == null)
        {
            return;
        }

        chatHolder.content.state = holder.state;
        chatHolder.progress = holder.progress;

        if (holder.state == MediaTransferState.Progressing)
        {
            if (holder.progress >= 0)
            {
                notifyItemChanged(mItems.getIndexByKey(holder.guid));
            }
        }
        else if (holder.state == MediaTransferState.Completed)
        {
            chatHolder.path = holder.path;
            notifyItemChanged(mItems.getIndexByKey(holder.guid));
        }


    }

    @Override
    public void onUploadChangeState(UploadHolder.Received holder)
    {

        ChatHolder chatHolder = mItems.getByKey(holder.guid);

        if (chatHolder == null)
        {
            return;
        }

        chatHolder.content.state = holder.state;
        chatHolder.progress = holder.progress;

        if (AppConfig.DEBUG)
        {
            Log.i(AppConfig.LOG_TAG, "Upload: " + holder.progress);
        }

        if (holder.state == MediaTransferState.Progressing)
        {
            if (holder.progress >= 0)
            {
                notifyItemChanged(mItems.getIndexByKey(holder.guid));
            }
        }
        else if (holder.state == MediaTransferState.Completed)
        {
            chatHolder.content.fileToken = holder.fileToken;
            notifyItemChanged(mItems.getIndexByKey(holder.guid));
        }

    }

    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////


}

