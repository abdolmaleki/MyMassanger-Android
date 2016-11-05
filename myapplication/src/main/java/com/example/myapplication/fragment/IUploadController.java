package com.example.myapplication.fragment;

import java.util.UUID;

import ir.hfj.library.exception.MyMessangerException;

public interface IUploadController
{
    void cancelUpload(UUID guid) throws MyMessangerException;
}
