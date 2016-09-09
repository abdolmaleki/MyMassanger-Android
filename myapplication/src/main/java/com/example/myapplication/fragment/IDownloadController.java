package com.example.myapplication.fragment;

import com.example.myapplication.holder.DownloadHolder;

import java.util.UUID;

import ir.hfj.library.exception.SamimException;

public interface IDownloadController
{
    void download(DownloadHolder.Send holder) throws SamimException;
    void cancelDownload(UUID guid) throws SamimException;
}
