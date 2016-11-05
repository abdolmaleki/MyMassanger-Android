package com.example.myapplication.fragment;

import com.example.myapplication.holder.DownloadHolder;

import java.util.UUID;

import ir.hfj.library.exception.MyMessangerException;

public interface IDownloadController
{
    void download(DownloadHolder.Send holder) throws MyMessangerException;
    void cancelDownload(UUID guid) throws MyMessangerException;
}
