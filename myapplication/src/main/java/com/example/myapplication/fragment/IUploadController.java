package com.example.myapplication.fragment;

import java.util.UUID;

import ir.hfj.library.exception.SamimException;

public interface IUploadController
{
    void cancelUpload(UUID guid) throws SamimException;
}
