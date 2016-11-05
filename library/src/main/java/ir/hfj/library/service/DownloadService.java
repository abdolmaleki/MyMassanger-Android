package ir.hfj.library.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import ir.hfj.library.R;
import ir.hfj.library.activity.DownloadActivity;
import ir.hfj.library.application.AppConfig;
import ir.hfj.library.application.ConstantBase;
import ir.hfj.library.connection.restapi.RestApi;
import ir.hfj.library.connection.restapi.jto.UtilityJto;
import ir.hfj.library.database.DbBase;
import ir.hfj.library.database.model.UserSettingModel;
import ir.hfj.library.exception.MyMessangerException;
import ir.hfj.library.holder.UpdateAppHolder;


public class DownloadService extends IntentService
{

    public final static int STATE_INIT = 0;
    public final static int STATE_RUNNING = 1;
    public final static int STATE_CANCELED = 2;
    public final static int STATE_FAILED = 3;
    public final static int STATE_COMPLETE = 4;
    public final static int STATE_STOPING = 5;
    public final static int STATE_FINISH = 6;

    private int mCurrentProgress = 0;
    private int mDownloadState = STATE_INIT;
    private NotificationManager mNotificationManager;
    private Builder mBuilder;
    private String mVersionCurrent = "";
    private String mVersionNew = "";

    private boolean mIsDownloadStop;

    private final Object lock = new Object();


    public DownloadService()
    {
        super("UpdateSamimService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        initNotification();

        return START_STICKY;

    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        synchronized (lock)
        {
            if (mDownloadState == STATE_RUNNING)
            {

                setState(STATE_STOPING);
                mIsDownloadStop = true;

            }
        }
    }


    @Override
    protected void onHandleIntent(Intent intent)
    {

        try
        {


            UserSettingModel userSetting = DbBase.UserSetting.select();

            if (userSetting == null)
            {
                //show active message
                sendUiUpdate(getString(R.string.messanger_service_message_not_active));
                return;
            }

            RestApi rest = new RestApi(this, userSetting.token, userSetting.key);
            rest.setBroadcastUnAuthorization(false);


            UtilityJto.UpdateAppJto.PostBack result = null;

            try
            {
                result = rest.updateApp(new UtilityJto.UpdateAppJto.Post());
            }
            catch (MyMessangerException e)
            {
                //show exception error
                sendUiUpdate(e.getMessage());
                return;
            }

            //show version info
            mVersionCurrent = result.currentVersion;
            mVersionNew = result.newVersion;

            sendUiUpdate(result.detailMessage);


            if (result.isNeedUpdate)
            {

                File apkFile = new File(Environment.getExternalStorageDirectory() + "/" + "samim." + result.newVersion + ".apk");

                if (downloadApp(result.apkUrl, apkFile.getPath()))
                {
                    //start install apk.
                    sendUiUpdate("در حال نصب نسخه جدید");
                    installApk(apkFile);
                    setState(STATE_FINISH);
                }

            }
            else
            {
            }

        }
        catch (Exception e)
        {
            setState(STATE_FAILED);
        }


    }
    private void installApk(File file)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean downloadApp(String address, String destination)
    {
        boolean isComplete = true;

        try
        {

            URL url = new URL(address);
            URLConnection connection = url.openConnection();
            connection.connect();

            setState(STATE_RUNNING);
            onProgressChange(0);
            int fileLength = connection.getContentLength();

            InputStream input = new BufferedInputStream(url.openStream());

            OutputStream output = new FileOutputStream(destination);

            byte data[] = new byte[1024 * 5];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1)
            {
                synchronized (lock)
                {
                    if (mIsDownloadStop)
                    {
                        setState(STATE_CANCELED);
                        isComplete = false;
                        break;
                    }
                }
                total += count;
                onProgressChange((int) (total * 100) / fileLength);
                output.write(data, 0, count);

            }

            output.flush();
            output.close();
            input.close();
        }
        catch (Exception e)
        {
            if (AppConfig.DEBUG)
            {
                Log.e(AppConfig.LOG_TAG, "DownloadService > onHandleIntent > Error: " + e.getMessage());
            }

            setState(STATE_FAILED);
            isComplete = false;


        }

        if (isComplete)
        {
            onDownloadComplete();
        }

        return isComplete;
    }


    private void initNotification()
    {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void sendUiUpdate()
    {
        sendUiUpdate((mDownloadState == STATE_RUNNING) ? "%" + mCurrentProgress : getStateMessage());
    }

    private void sendUiUpdate(String message)
    {

        if (AppConfig.DEBUG)
        {
            Log.i(AppConfig.LOG_TAG, "DownloadService > " + message);
        }

        Intent intent = new Intent(ConstantBase.Intent.DOWNLOAD_RESOURCE);
        UpdateAppHolder holder = new UpdateAppHolder();
        holder.message = message;
        holder.progress = mCurrentProgress;
        holder.state = mDownloadState;
        holder.newVersion = mVersionNew;
        holder.currentVersion = mVersionCurrent;
        intent.putExtra(ConstantBase.Param.KEY_DOWNLOAD_STATE, holder);

        this.sendNotification(message);
        this.sendBroadcast(intent);
    }

    private void sendNotification(String message)
    {

        Intent intent = new Intent(getBaseContext(), DownloadActivity.class);

        if (mDownloadState == STATE_RUNNING || mDownloadState == STATE_INIT || mDownloadState == STATE_STOPING)
        {
            if (mBuilder == null)
            {
                PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder = new Builder(this);
                mBuilder.setContentTitle(getString(R.string.messanger_label_update))
                        .setContentText("در حال دانلود نسخه جدید")
                        .setContentIntent(pIntent)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.ic_samim_logo);

                if(mDownloadState == STATE_INIT)
                {
                    mBuilder.setTicker(message);
                }

            }


            mBuilder.setProgress(100, mCurrentProgress, (mCurrentProgress <= 0));
            mBuilder.setContentInfo(message);
            mNotificationManager.notify(ConstantBase.NotificationId.UPDATE_SERVICE, mBuilder.build());


        }
        else
        {

            PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder = new Builder(this);
            mBuilder.setContentTitle(getString(R.string.messanger_label_update))
                    .setContentText("")
                    //.setContentIntent(pIntent)
                    .setContentInfo(message)
                    .setOngoing(false)
                    .setTicker(message)
                    .setSmallIcon(R.drawable.ic_samim_logo);

            mNotificationManager.notify(ConstantBase.NotificationId.UPDATE_SERVICE, mBuilder.build());
        }

    }


    private void setState(int state)
    {
        mDownloadState = state;
        setState(state, (mDownloadState == STATE_RUNNING) ? mCurrentProgress + "%" : getStateMessage());
    }

    private void setState(int state, String message)
    {

        mDownloadState = state;
        sendUiUpdate(message);

    }

    private void onProgressChange(int progress)
    {

        if (mCurrentProgress != progress)
        {
            mCurrentProgress = progress;
            sendUiUpdate();
        }

    }

    private void onDownloadComplete()
    {
        mCurrentProgress = 100;
        setState(STATE_COMPLETE);
    }

    public String getStateMessage()
    {
        switch (mDownloadState)
        {
            case STATE_INIT:
                return "در حال آماده سازی برای بروز رسانی";
            case STATE_RUNNING:
                return mCurrentProgress + "%";
            case STATE_COMPLETE:
                return "نسخه جدید با موفقیت دریافت شد";
            case STATE_CANCELED:
                return "بروز رسانی متوقف شد";
            case STATE_FAILED:
                return "خطا در بروز رسانی";
            case STATE_STOPING:
                return "در حال توقف";
            case STATE_FINISH:
                return "پایان عملیات بروز رسانی";
        }

        return "وضعیت نامشخص";

    }
}
