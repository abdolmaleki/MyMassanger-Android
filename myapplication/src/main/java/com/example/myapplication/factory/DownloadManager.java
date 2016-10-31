package com.example.myapplication.factory;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.NotificationCompat.Builder;

import com.example.myapplication.R;
import com.example.myapplication.dm.DefaultRetryPolicy;
import com.example.myapplication.dm.DownloadRequest;
import com.example.myapplication.dm.DownloadStatusListener;
import com.example.myapplication.dm.ThinDownloadManager;
import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.entity.MediaTransferState;
import com.example.myapplication.holder.DownloadHolder;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class DownloadManager implements DownloadStatusListener
{


    //-----------<downloadId, info>
    private HashMap<Integer, Holder> mDictionary = new HashMap<>();
    private ThinDownloadManager dm;
    private OnDownloadManagerListener mListener;
    private final Context mContext;

    private NotificationManager mNotificationManager;
    private Builder mQueueBuilder;
    private final int mQueueID = Integer.MAX_VALUE - 1000;


    public DownloadManager(Context context, OnDownloadManagerListener listener)
    {
        dm = new ThinDownloadManager(2);
        mListener = listener;
        mContext = context;
    }

    public interface OnDownloadManagerListener
    {

        void onDownloadChangeState(DownloadHolder.Received holder);
    }

    public void download(DownloadHolder.Send sendHolder)
    {


        String tempFile = null;
        try
        {
            //UUID.randomUUID() because; maybe multi download say time
            tempFile = FileManager.getTempDirectory() + "/" + UUID.randomUUID().toString();
        }
        catch (Exception e)
        {
            Holder holder = new Holder(sendHolder, "");
            holder.state = MediaTransferState.Error;
            holder.message = mContext.getString(R.string.messanger_message_file_write_ex);
            sendUiUpdate(holder);
        }

        Holder holder = new Holder(sendHolder, tempFile);


        try
        {

            Uri downloadUri = Uri.parse(FileManager.getMediaUrl(holder._token));
            Uri destinationUri = Uri.parse(holder._tempDestination);

            DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                    //.addCustomHeader("Auth-Token", "YourTokenApiKey")
                    .setRetryPolicy(new DefaultRetryPolicy())
                    .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.NORMAL)
                    .setDownloadListener(this);


            holder.state = MediaTransferState.Queue;
            holder.message = mContext.getString(R.string.messanger_message_download_in_queue);
            holder.process = -1;

            int downloadId = dm.add(downloadRequest);

            mDictionary.put(downloadId, holder);

            updateQueueNotification(holder);

            sendUiUpdate(holder);
        }
        catch (Exception ex)
        {
            holder.state = MediaTransferState.Error;
            holder.message = mContext.getString(R.string.messanger_message_download_error);
            holder.process = -1;

            sendUiUpdate(holder);
        }


    }

    public void getState(UUID guid)
    {
        for (Holder holder : mDictionary.values())
        {
            if (holder._guid.equals(guid))
            {
                sendUiUpdate(holder);
                //break; maybe multi ids with different tag
                break;
            }
        }
    }

    public void cancel(UUID guid)
    {

        for (int key : mDictionary.keySet())
        {
            Holder holder = mDictionary.get(key);
            if (holder != null && holder._guid.equals(guid))
            {
                holder.message = mContext.getString(R.string.messanger_message_download_canceled);
                holder.state = MediaTransferState.Cancel;

                dm.cancel(key);

                sendUiUpdate(holder);

                //************
                removeDownload(key, holder);
                //************
            }
        }

    }


    public void release()
    {
        try
        {
            if (dm != null)
            {
                dm.release();
            }
        }
        catch (Exception ignored)
        {

        }
    }


    //=============================================================================================
    //Download Manager ============================================================================
    //=============================================================================================


    @Override
    public void onDownloadComplete(int id)
    {


        Holder holder = mDictionary.get(id);
        if (holder != null)
        {


            try
            {
                //move to original place

                //if (holder._tempDestination.endsWith(holder._guid.toString()))
                {
                    File temp = new File(holder._tempDestination);

                    if (temp.exists())
                    {

                        holder.path = FileManager.getDirectory(holder._type) + "/" + holder._token + "." + holder._extension;

                        File des = new File(holder.path);
                        if (temp.renameTo(des))
                        {
                            holder.process = 100;
                            holder.state = MediaTransferState.Completed;
                            holder.message = mContext.getString(R.string.messanger_message_download_complete);

                            sendUiUpdate(holder);

                            //************
                            removeDownload(id, holder);
                            //************
                            return;
                        }
                    }

                }

            }
            catch (Exception ignored)
            {

            }


            onDownloadFailed(id, -1, "");


        }


    }


    @Override
    public void onDownloadFailed(int id, int errorCode, String errorMessage)
    {
        Holder holder = mDictionary.get(id);
        if (holder != null)
        {
            //DownloadManager.ERROR_DOWNLOAD_CANCELLED ...
            holder.message = mContext.getString(R.string.messanger_message_download_error);
            holder.state = MediaTransferState.Error;

            sendUiUpdate(holder);

            //************
            removeDownload(id, holder);
            //************

        }

    }


    @Override
    public void onProgress(int id, long totalBytes, long downloadedBytes, int progress)
    {

        Holder holder = mDictionary.get(id);
        if (holder != null && holder.process != progress)
        {
            if (holder.state == MediaTransferState.Queue)
            {
                holder.state = MediaTransferState.Progressing;
                updateQueueNotification(holder);
            }
            else
            {
                holder.state = MediaTransferState.Progressing;
            }

            holder.message = String.format("%.2f", downloadedBytes / 1024f / 1024f) + "/" + String.format("%.2f", totalBytes / 1024f / 1024f) + " MB";
            holder.process = progress;


            sendUiUpdate(holder);

        }

    }

    private void removeDownload(int id, Holder holder)
    {
        mDictionary.remove(id);

        updateQueueNotification(holder);

        if (mDictionary.size() <= 0)
        {
            //stopSelf();
        }
    }

    //=============================================================================================
    // ============================================================================
    //=============================================================================================

    private void sendUiUpdate(Holder holder)
    {

        mListener.onDownloadChangeState(holder.toDownloadHolder());

        sendNotification(holder);
    }

    //===================================================================================================


    private void sendNotification(Holder holder)
    {
        /*

        if (holder.txvState == MediaTransferState.Progressing)
        {

            long current = System.currentTimeMillis();
            if (lastProgressUpdate + 800 > current)
            {
                return;
            }

            lastProgressUpdate = current;

            if (holder.mBuilder == null)
            {
                Intent intent = new Intent(mContext, ChatActivity.class);
                //intent.putExtra("id", (int) holder._guid);


                PendingIntent pIntent = PendingIntent.getActivity(mContext, (int) holder._guid.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                holder.mBuilder = new Builder(this);
                holder.mBuilder
                        .setContentText(holder._periodTitle)
                        .setContentIntent(pIntent)
                        .setSmallIcon(R.drawable.ic_notification_logo)
                        .setOngoing(true);

            }

            holder.mBuilder.setProgress(100, holder.progress, (holder.progress <= 0));
            holder.mBuilder.setContentInfo(holder.message);
            holder.mBuilder.setContentTitle("%" + holder.progress + " " + "دریافت فایل صوتی");
            Notification notification = holder.mBuilder.build();
            notification.txvIcon = R.drawable.ic_download_anim;
            mNotificationManager.notify((int) holder._id, notification);

        }
        else
        {
            Intent intent;

            if (holder.mContentParentId < 0 || holder.mContentPosition < 0)
            {
                intent = new Intent(getBaseContext(), ActivitySimpleText.class);
                intent.putExtra("id", (int) holder._id);
            }
            else
            {
                intent = new Intent(getBaseContext(), ActivityPlayerList.class);
                intent.putExtra("id", holder.mContentParentId);
                intent.putExtra("selected_position", holder.mContentPosition);
            }

            PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), (int) holder._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            holder.mBuilder = new Builder(this);
            holder.mBuilder.setContentTitle("دریافت فایل صوتی")
                    .setContentText(holder._periodTitle)
                    .setContentIntent(pIntent)
                    .setContentInfo(holder.message)
                    .setOngoing(false)
                    .setSmallIcon(R.drawable.ic_notification_logo)
                    .setTicker(holder.message);

            Notification notification = holder.mBuilder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify((int) holder._id, notification);

        }*/

    }


    private void updateQueueNotification(Holder holder)
    {
        /*int count = 0;
        String names = "";
        for (Holder h : mDictionary.values())
        {
            if (h.txvState == Constant.DownloadManager.STATE_QUEUE)
            {
                count++;
                if (count <= 3)
                {
                    names += ((count > 1) ? "، " : "") + h._periodTitle;
                }
            }
        }


        if (count > 0)
        {

            if (count > 3)
            {
                names += " و ...";
            }

            if (mQueueBuilder == null)
            {
                mQueueBuilder = new Builder(this);
            }

            mQueueBuilder.setContentTitle("صف دانلود فایل صوتی");
            mQueueBuilder.setContentText(names);
            mQueueBuilder.setSmallIcon(R.drawable.ic_notification_queue);
            mQueueBuilder.setOngoing(true);
            mQueueBuilder.setContentInfo("تعداد " + count + " فایل");

            if (holder.txvState == MediaTransferState.Queue)
            {
                mQueueBuilder.setTicker(holder.message);
            }

            Notification notification = mQueueBuilder.build();
            mNotificationManager.notify(mQueueID, notification);
        }
        else
        {
            mNotificationManager.cancel(mQueueID);
        }*/
    }

    //=============================================================================================
    //=============================================================================================
    //=============================================================================================
    //=============================================================================================
    //=============================================================================================
    //=============================================================================================
    //=============================================================================================
    //=============================================================================================

    private class Holder implements Serializable
    {

        public final UUID _guid;
        public final String _token;
        public final String _extension;
        public final String _tempDestination;
        public final String _title;
        public final ChatContentType _type;


        public Builder mBuilder;
        public String message = "";
        public MediaTransferState state = MediaTransferState.Queue;
        public int process = -1;
        public String path;

        public Holder(DownloadHolder.Send sendHolder, String tempDestination)
        {
            _guid = sendHolder.guid;
            _type = sendHolder.type;
            _token = sendHolder.token;
            _title = sendHolder.title;
            _extension = sendHolder.extension;
            _tempDestination = tempDestination;
        }

        private DownloadHolder.Received downloadHolder = null;
        public DownloadHolder.Received toDownloadHolder()
        {
            if (downloadHolder == null)
            {
                downloadHolder = new DownloadHolder.Received();
            }

            downloadHolder.guid = _guid;
            downloadHolder.type = _type;
            downloadHolder.message = message;
            downloadHolder.state = state;
            downloadHolder.progress = process;
            downloadHolder.path = path;
            downloadHolder.extension = _extension;
            downloadHolder.title = _title;
            return downloadHolder;
        }

    }

}
