package ir.hfj.library.actionbar;


import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import ir.hfj.library.R;

public abstract class BaseActionBar
{

    private AppCompatActivity mActivity = null;
    private int mLayoutRes;
    private TextView uiTxvTitle = null;

    public BaseActionBar(AppCompatActivity activity, ActionBar actionBar, int layoutRes)
    {

        mActivity = activity;
        mLayoutRes = layoutRes;

        initActionbar(actionBar);


    }

    private void initActionbar(ActionBar actionBar)
    {
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setElevation(0);
        LayoutInflater mInflater = LayoutInflater.from(mActivity);

        View rootView = mInflater.inflate(mLayoutRes, null);

        int[] items = getItems();
        if (items != null)
        {
            for (int id : items)
            {
                View v = rootView.findViewById(id);
                if (v != null)
                {
                    v.setOnClickListener(mClickListener);
                }
            }
        }

        uiTxvTitle = (TextView) rootView.findViewById(R.id.samim_actionbar_title);

        actionBar.setCustomView(rootView);

        //remove margin left and right
        Toolbar parent =(Toolbar) rootView.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        initView(rootView);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener()
    {

        @Override
        public void onClick(View v)
        {

            if (mActivity instanceof OnSamimActionBarItemClick)
            {
                boolean result = ((OnSamimActionBarItemClick) mActivity).onActionBarItemsClick(v);
                if (!result)
                {
                    return;
                }
            }

            onItemsClick(v);

        }
    };

    public void setTitle(String s)
    {
        if (uiTxvTitle != null)
        {
            uiTxvTitle.setText(s);
        }
    }

    public void setTitle(int res)
    {
        if (uiTxvTitle != null)
        {
            uiTxvTitle.setText(res);
        }
    }

    public AppCompatActivity getActivity()
    {
        return mActivity;
    }

    protected abstract int[] getItems();

    protected abstract void initView(View rootView);

    protected abstract void onItemsClick(View v);


}
