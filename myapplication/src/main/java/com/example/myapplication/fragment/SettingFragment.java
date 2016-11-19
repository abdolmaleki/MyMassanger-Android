package com.example.myapplication.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;

public class SettingFragment extends Fragment
{

    public SettingFragment()
    {
    }

    public static SettingFragment newInstance()
    {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadData();

        initView();

        initAdapter();

    }
    private void initAdapter()
    {

    }
    private void initView()
    {
        setRetainInstance(true);
    }
    private void loadData()
    {

    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        final View rootView;
        rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        return rootView;
    }

}
