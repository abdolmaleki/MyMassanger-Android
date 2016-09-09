package com.example.myapplication.fragment;

import com.example.myapplication.holder.UploadHolder;

public interface IUploadMediaListener
{
    void onUploadChangeState(UploadHolder.Received uploadHolder);
}
