package com.example.myapplication.actionbar;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.myapplication.R;
import com.pnikosis.materialishprogress.ProgressWheel;

public class RefreshableActionBar extends StudentActionBar
{

    private View uiBtnRefresh;
    private ProgressWheel uiPrgLoading;

    public RefreshableActionBar(AppCompatActivity activity, ActionBar actionBar)
    {
        super(activity, actionBar, R.layout.actionbar_refreshable);

    }

    public RefreshableActionBar(AppCompatActivity activity, ActionBar actionBar, int layoutRes)
    {
        super(activity, actionBar, layoutRes);

    }

    @Override
    protected void initView(View rootView)
    {
        super.initView(rootView);

        uiBtnRefresh = rootView.findViewById(R.id.actionbar_refreshable_ico_refresh);
        uiPrgLoading = (ProgressWheel) rootView.findViewById(R.id.actionbar_refreshable_progressbar);
        uiPrgLoading.setBarColor(getActivity().getResources().getColor(R.color.samim_color_white));

        uiPrgLoading.spin();
    }

    @Override
    protected int[] getItemsWithOutStudent()
    {
        return new int[]
                {
                        R.id.actionbar_refreshable_ico_refresh
                };
    }

    @Override
    protected void onItemsClickOther(View v)
    {
        /*switch (v.getId())
        {
            case R.id.actionbar_refreshable_img_refresh:
            {

                break;
            }
            default:
                break;

        }*/
    }

    public void startProgressing()
    {
        uiBtnRefresh.setVisibility(View.GONE);
        uiPrgLoading.setVisibility(View.VISIBLE);
    }

    public void stopProgressing()
    {
        uiBtnRefresh.setVisibility(View.VISIBLE);
        uiPrgLoading.setVisibility(View.GONE);
    }


}
