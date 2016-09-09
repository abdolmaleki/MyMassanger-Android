package com.example.myapplication.fragment;

import com.example.myapplication.holder.DownloadHolder;

public interface IDownloadMediaListener
{
    void onDownloadChangeState(DownloadHolder.Received downloadHolder);
}
