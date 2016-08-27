package ir.hfj.library.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import ir.hfj.library.R;
import ir.hfj.library.application.ConstantBase;
import ir.hfj.library.fragment.DownloadFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class DownloadActivity extends FragmentActivity
{

    private String mMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        loadData();

        if (savedInstanceState == null)
        {
            submitFragment();
        }
    }
    private void loadData()
    {
        mMessage = getIntent().getStringExtra(ConstantBase.Param.KEY_DOWNLOAD_STATE);

    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void submitFragment()
    {
        try
        {
            if (!isFinishing())
            {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DownloadFragment fragment = DownloadFragment.newInstance(mMessage);
                fragmentTransaction.replace(R.id.fragment_place, fragment);
                fragmentTransaction.commit();
            }
        }
        catch (Exception ignored)
        {

        }

    }


}
