package ir.hfj.library.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import ir.hfj.library.R;
import ir.hfj.library.application.ConstantBase;
import ir.hfj.library.holder.UpdateAppHolder;
import ir.hfj.library.service.DownloadService;


public class DownloadFragment extends Fragment implements OnClickListener
{

    private Button uiBtnDownload;
    private TextView uiTxvState;
    private ProgressBar uiPrgDownload;

    private UpdateAppHolder mUpdateHolder;

    private TextView uiTxvNewVersion;
    private TextView uiTxvCurrentVersion;
    private String mMessage;

    public static DownloadFragment newInstance(String message)
    {
        DownloadFragment fragment = new DownloadFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ConstantBase.Param.KEY_MESSAGE, message);
        fragment.setArguments(bundle);
        return fragment;
    }


    private String getStateButtonLabel()
    {

        switch (mUpdateHolder.state)
        {
            case DownloadService.STATE_RUNNING:
                return "لغو دانلود";
            case DownloadService.STATE_FAILED:
                return "تلاش دوباره";
            case DownloadService.STATE_STOPING:
                return "درحال توقف";
            default:
                return "دانلود";
        }


    }

    private void refreshUI()
    {

        switch (mUpdateHolder.state)
        {
            case DownloadService.STATE_INIT:
            case DownloadService.STATE_CANCELED:
            {
                uiBtnDownload.setEnabled(true);
                break;
            }
            case DownloadService.STATE_STOPING:
            {
                uiBtnDownload.setEnabled(false);
                break;
            }
        }

        uiPrgDownload.setProgress(mUpdateHolder.progress);
        uiBtnDownload.setText(getStateButtonLabel());
        uiTxvState.setText(mUpdateHolder.message);

        if (!mUpdateHolder.currentVersion.isEmpty() && !mUpdateHolder.newVersion.isEmpty())
        {
            uiTxvCurrentVersion.setText("نسخه فعلی: " + mUpdateHolder.currentVersion);
            uiTxvNewVersion.setText("آخرین نسخه: " + mUpdateHolder.newVersion);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadData();

    }

    private void loadData()
    {
        mMessage = getArguments().getString(ConstantBase.Param.KEY_MESSAGE);
        if (mMessage == null || mMessage.isEmpty())
        {
            mMessage = getActivity().getString(R.string.messanger_label_update);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        //if (mIsAutoStart)
        //{
        //    startService();
        //}
    }
    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter mIntentFilter = new IntentFilter(ConstantBase.Intent.DOWNLOAD_RESOURCE);
        getActivity().registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        if (savedInstanceState != null)
        {
            mUpdateHolder = (UpdateAppHolder) savedInstanceState.getSerializable(ConstantBase.Param.KEY_DOWNLOAD_STATE);
        }
        else
        {
            mUpdateHolder = new UpdateAppHolder();
            mUpdateHolder.state = DownloadService.STATE_INIT;
            mUpdateHolder.currentVersion = "";
            mUpdateHolder.newVersion = "";
            mUpdateHolder.message = mMessage;
            mUpdateHolder.progress = 0;
        }

        View view = inflater.inflate(R.layout.fragment_place_download, container, false);
        uiPrgDownload = (ProgressBar) view.findViewById(R.id.DownloadFragment_ProgressBar);
        uiTxvState = (TextView) view.findViewById(R.id.DownloadFragment_txv_state);
        uiTxvState.setText(mMessage);
        uiTxvCurrentVersion = (TextView) view.findViewById(R.id.DownloadFragment_txv_current_version);
        uiTxvNewVersion = (TextView) view.findViewById(R.id.DownloadFragment_txv_last_version);
        uiBtnDownload = (Button) view.findViewById(R.id.DownloadFragment_btn_download);
        uiBtnDownload.setText(getStateButtonLabel());
        uiBtnDownload.setOnClickListener(this);


        if (mUpdateHolder != null)
        {
            uiPrgDownload.post(new Runnable()
            {
                @Override
                public void run()
                {
                    uiPrgDownload.setProgress(mUpdateHolder.progress);
                }
            });

            refreshUI();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putSerializable(ConstantBase.Param.KEY_DOWNLOAD_STATE, mUpdateHolder);

    }

    // =================================================================================================================
    //  Events Handler
    //==================================================================================================================


    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            //setIsConnectToService(true);
            Bundle extra = intent.getExtras();
            mUpdateHolder = (UpdateAppHolder) extra.getSerializable(ConstantBase.Param.KEY_DOWNLOAD_STATE);


            refreshUI();

            if (mUpdateHolder.state == DownloadService.STATE_FINISH)
            {
                getActivity().finish();
            }
        }
    };


    @Override
    public void onClick(View view)
    {

        int id = view.getId();

        if (id == R.id.DownloadFragment_btn_download)
        {
            if (mUpdateHolder.state == DownloadService.STATE_RUNNING)
            {
                stopService();
            }
            else
            {
                startService();
            }

        }
    }

    public void startService()
    {
        Intent intent = new Intent(getActivity(), DownloadService.class);
        getActivity().startService(intent);
    }

    public void stopService()
    {
        Intent intent = new Intent(getActivity(), DownloadService.class);
        getActivity().stopService(intent);
    }

}
