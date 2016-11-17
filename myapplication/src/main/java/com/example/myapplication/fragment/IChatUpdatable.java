package com.example.myapplication.fragment;

import java.util.UUID;

import ir.hfj.library.exception.MyMessangerException;

public interface IChatUpdatable
{

    void switchChatUserFragment(UUID chatUserGuid);

    void refreshStatusBar();

    void getProfileImage() throws MyMessangerException;

    void toggleMenu();

}
