package com.example.myapplication.actionbar;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.myapplication.R;

import ir.hfj.library.actionbar.BaseActionBar;

public final class SimpleActionBar extends BaseActionBar
{


    public SimpleActionBar(AppCompatActivity activity, ActionBar actionBar)
    {
        super(activity, actionBar, R.layout.actionbar_simple);

    }

    @Override
    protected int[] getItems()
    {
        return null;
    }

    @Override
    protected void initView(View rootView)
    {

    }

    @Override
    protected void onItemsClick(View v)
    {

    }



}
