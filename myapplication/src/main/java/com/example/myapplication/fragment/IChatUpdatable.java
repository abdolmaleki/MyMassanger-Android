package com.example.myapplication.fragment;

import java.util.UUID;

public interface IChatUpdatable
{
    void switchChatUserFragment(UUID chatUserGuid);
    void refreshStatusBar();
}
