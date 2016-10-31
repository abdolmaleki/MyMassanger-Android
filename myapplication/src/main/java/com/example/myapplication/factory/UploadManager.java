package com.example.myapplication.factory;

import android.content.Context;
import android.os.AsyncTask;

import com.example.myapplication.R;
import com.example.myapplication.connection.restapi.FileUploadApi;
import com.example.myapplication.connection.restapi.jto.FileJto;
import com.example.myapplication.entity.MediaTransferState;
import com.example.myapplication.fragment.IUploadMediaListener;
import com.example.myapplication.holder.UploadHolder;

import java.util.Hashtable;
import java.util.UUID;

import ir.hfj.library.connection.restapi.UploadRestApi;
import ir.hfj.library.connection.restapi.jto.PostBackJto;
import ir.hfj.library.exception.SamimException;


public final class UploadManager
{


    private final Hashtable<UUID, Task> mDictionary;
    private final Context mContext;
    private String mToken;
    private String mKey;


    public UploadManager(Context context)
    {
        this.mContext = context;

        mDictionary = new Hashtable<>();
    }

    public void upload(UploadHolder.Send sendHolder, String token, String key, IUploadMediaListener listener)
    {
        if (sendHolder.guid == null)
        {
            return;
        }

        synchronized (mDictionary)
        {
            mToken = token;
            mKey = key;

            Task task = new Task(sendHolder, listener);
            mDictionary.put(sendHolder.guid, task);
            task.start();
        }
    }

    public void cancel(UUID guid)
    {
        if (guid == null)
        {
            return;
        }

        synchronized (mDictionary)
        {
            Task task = mDictionary.get(guid);
            if (task != null)
            {
                task.stop();
            }

            mDictionary.remove(guid);
        }
    }

    public void cleanAndStopAll()
    {
        synchronized (mDictionary)
        {
            for (Task task : mDictionary.values())
            {
                task.stop();
            }

            mDictionary.clear();
        }
    }

    private final IUploadMediaListener mListenerInternal = new IUploadMediaListener()
    {
        @Override
        public void onUploadChangeState(UploadHolder.Received uploadHolder)
        {
            Task task = mDictionary.get(uploadHolder.guid);

            if (uploadHolder.state == MediaTransferState.Cancel ||
                    uploadHolder.state == MediaTransferState.Completed ||
                    uploadHolder.state == MediaTransferState.Error)
            {
                mDictionary.remove(uploadHolder.guid);
            }

            if (task != null)
            {
                task.mListener.onUploadChangeState(uploadHolder);
            }


        }
    };


    private class Task
    {

        private final UploadHolder.Send mHolder;
        private final UploaderAsyncTask mAsyncTask;
        private final IUploadMediaListener mListener;

        public Task(UploadHolder.Send sendHolder, IUploadMediaListener listener)
        {
            this.mHolder = sendHolder;
            mListener = listener;
            mAsyncTask = new UploaderAsyncTask();
        }

        public void start()
        {
            if (!mAsyncTask.isCancelled())
            {
                mAsyncTask.execute();
            }
        }

        public void stop()
        {
            if (!mAsyncTask.isCancelled())
            {
                mAsyncTask.cancel(true);
            }
        }

        private class UploaderAsyncTask extends AsyncTask<Void, Integer, FileJto.PostBack> implements UploadRestApi.OnUploadProgress
        {

            private UploadHolder.Received mUploadHolder;

            public UploaderAsyncTask()
            {
                mUploadHolder = new UploadHolder.Received();
                mUploadHolder.title = mHolder.title;
                mUploadHolder.extension = mHolder.extension;
                mUploadHolder.guid = mHolder.guid;
                mUploadHolder.type = mHolder.type;
                mUploadHolder.state = MediaTransferState.Queue;
                mUploadHolder.progress = -1;
                mUploadHolder.fileToken = null;
            }

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                mUploadHolder.state = MediaTransferState.Queue;
                mUploadHolder.progress = -1;
            }
            @Override
            protected FileJto.PostBack doInBackground(Void... files)
            {

                FileUploadApi restApi = new FileUploadApi(mContext, mToken, mKey);


                FileJto.PostBack postBackJto;


                try
                {
                    postBackJto = restApi.uploadFile(
                            new FileJto.Post(
                                    mHolder.path,
                                    mHolder.type,
                                    mHolder.extension
                            ), new FileUploadApi.OnProgressListener()
                            {
                                @Override
                                public void OnProgress(int percent)
                                {
                                    publishProgress(percent);
                                }
                            });
                }
                catch (SamimException e)
                {
                    postBackJto = new FileJto.PostBack();
                    postBackJto.stateCode = PostBackJto.RESULT_BAD_REQUEST;
                    postBackJto.detailMessage = e.getMessage();
                    postBackJto.subjectMessage = mContext.getString(ir.hfj.library.R.string.messanger_login_message_title_error);
                }

                return postBackJto;
            }

            @Override
            protected void onProgressUpdate(Integer... values)
            {
                mUploadHolder.state = MediaTransferState.Progressing;
                mUploadHolder.progress = values[0];
                mListenerInternal.onUploadChangeState(mUploadHolder);
            }

            @Override
            protected void onCancelled()
            {
                super.onCancelled();
                mUploadHolder.state = MediaTransferState.Cancel;
                mListenerInternal.onUploadChangeState(mUploadHolder);
            }

            @Override
            protected void onCancelled(FileJto.PostBack s)
            {
                super.onCancelled(s);
                mUploadHolder.state = MediaTransferState.Cancel;
                mListenerInternal.onUploadChangeState(mUploadHolder);
            }

            int oldPercent = -1;
            @Override
            public void OnUploadProgress(long v, long total)
            {
                int percent = (int) ((v / (float) total) * 100);
                if (oldPercent != percent)
                {
                    oldPercent = percent;
                    publishProgress(oldPercent);
                }
            }

            @Override
            protected void onPostExecute(FileJto.PostBack postBack)
            {
                if (postBack.stateCode == PostBackJto.RESULT_OK)
                {
                    if (postBack.token != null && !postBack.token.isEmpty())
                    {
                        if (oldPercent != 100)
                        {
                            oldPercent = 100;
                            mUploadHolder.state = MediaTransferState.Progressing;
                            mUploadHolder.progress = 100;
                            mListenerInternal.onUploadChangeState(mUploadHolder);
                        }

                        mUploadHolder.state = MediaTransferState.Completed;
                        mUploadHolder.fileToken = postBack.token;

                        mListenerInternal.onUploadChangeState(mUploadHolder);

                    }
                    else
                    {
                        mUploadHolder.state = MediaTransferState.Error;
                        mUploadHolder.message = mContext.getString(R.string.messanger_message_upload_error);
                        mListenerInternal.onUploadChangeState(mUploadHolder);
                    }
                }
                else
                {
                    mUploadHolder.state = MediaTransferState.Error;
                    mUploadHolder.message = postBack.detailMessage;
                    mListenerInternal.onUploadChangeState(mUploadHolder);
                }


            }


        }
    }

}
