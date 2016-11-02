package com.example.myapplication.application;

public final class Constant
{

    public static final class Param
    {

        public static final String KEY_STUDENT_ID = "student.id";
        public static final String KEY_SPLASH_SCREEN_STATE = "splashscreen";
        public static final String KEY_CHAT_CONTACT_GUID = "chat.user.id";
        public static final String KEY_CHATHISTORY_CONNECTION_STATUS = "chathistory.connection.status";


        //fake
        public static final String KEY_DATE_START = "date.start";
        public static final String KEY_DATE_END = "date.end";
        public static final String KEY_DATE = "date";

    }

    public static final class Preference
    {

        public final static String YEAR = "year";
        public final static String REFRESH_TIME = "auto.refresh.time";
        public final static String GENERAL = "general";

        public static final class Keys
        {

            public final static String REFRESH_TIME_CONTACT = "chatUser";
            //===========================================================================================
            public static final String GENERAL_SELECTED_STUDENT_ID = "student.selected";
        }
    }

    public static final class NotificationTag
    {

        public static final String CHAT = "samim.chat";


        public static final String SAMIM_SERVICE = "ir.hfj.samim.service";

    }


    public static final class NotificationId
    {

        public final static int SAMIM_SERVICE = 1000;
        public final static int SAMIM_UPDATE_APP = 2000;
    }


    public static final class DownloadManager
    {

        public static final String ACTION_DOWNLOAD = "download";
        public static final String ACTION_GET_STATE = "state";
        public static final String ACTION_CANCEL = "cancel";


        public static final String PARAM_ID = "id";
        public static final String PARAM_TAG = "tag";
        public static final String PARAM_URL = "url";
        public static final String PARAM_TITLE = "periodTitle";
        public static final String PARAM_DES = "des";

        public static final String PARAM_PROCESS = "process";
        public static final String PARAM_MESSAGE = "message";
        public static final String PARAM_STATE = "state";


    }

}

